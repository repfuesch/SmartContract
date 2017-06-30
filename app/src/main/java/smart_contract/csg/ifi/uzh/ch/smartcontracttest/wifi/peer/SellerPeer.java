package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.common.FileManager;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.serialization.SerializationService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service.ContractInfoListener;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service.UserProfileListener;

/**
 * Created by flo on 18.06.17.
 */

public class SellerPeer implements TradingPeer, UserProfileListener, ContractInfoListener
{
    private WifiSellerCallback callback;
    private SerializationService serializationService;
    private boolean useIdentification;
    private SellerPeer.WifiServerState state;
    private OnTradingPeerStoppedHandler stoppedHandler;
    private TradingClient client;
    private Integer port;
    private UserProfile buyerProfile;

    private ScheduledFuture mainTask;
    private Thread sendProfileTask;
    private Thread sendContractTask;

    public SellerPeer(SerializationService serializationService, WifiSellerCallback callback, TradingClient client, OnTradingPeerStoppedHandler stoppedHandler, boolean useIdentification, Integer port)
    {
        this.serializationService = serializationService;
        this.callback = callback;
        this.useIdentification = useIdentification;
        this.stoppedHandler = stoppedHandler;
        this.client = client;
        this.port = port;
    }

    public void start()
    {
        mainTask = Async.getExecutorService().schedule(new Runnable() {
            @Override
            public void run() {
                /**
                 * Create a buyerServer socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                boolean running = true;
                ServerSocket serverSocket = null;
                try{

                    if(port == null)
                    {
                        serverSocket = new ServerSocket(0);
                        //send connection request to buyer peer first
                        client.sendConnectionRequest(new ConnectionRequest(serverSocket.getLocalPort()));
                        //send the connection configuration to the buyers device. If this is successful, we notify the callback
                        //that the buyer is ready to receive the data
                        ConnectionConfig config = new ConnectionConfig(useIdentification);
                        client.sendConnectionConfiguration(config);

                        if(useIdentification)
                        {
                            state = WifiServerState.ExpectUserProfile;
                        }else{
                            callback.onContractInfoRequested(SellerPeer.this);
                        }

                    }else{
                        //wait such that other peer can detect the free local port
                        state = WifiServerState.ExpectConnectionRequest;
                        Thread.sleep(3000);
                        serverSocket = new ServerSocket(port);
                    }

                    serverSocket.setReuseAddress(true);
                    Socket clientSocket = null;

                    while(running)
                    {
                        if(clientSocket != null && !clientSocket.isClosed())
                            clientSocket.close();

                        clientSocket = serverSocket.accept();
                        InputStream inputStream = clientSocket.getInputStream();

                        switch(state)
                        {
                            case ExpectConnectionRequest:
                                String connectionString = convertStreamToString(inputStream);
                                ConnectionRequest connectionRequest = serializationService.deserialize(connectionString, new TypeToken<ConnectionRequest>(){}.getType());
                                client.setHost(clientSocket.getInetAddress().getHostAddress());
                                client.setPort(connectionRequest.getListeningPort());
                                //send the connection configuration to the buyers device. If this is successful, we notify the callback
                                //that the buyer is ready to receive the data
                                ConnectionConfig config = new ConnectionConfig(useIdentification);
                                client.sendConnectionConfiguration(config);
                                if(useIdentification)
                                {
                                    state = WifiServerState.ExpectUserProfile;
                                }else{
                                    callback.onContractInfoRequested(SellerPeer.this);
                                }
                                break;
                            case ExpectUserProfile:
                                if(buyerProfile == null) {
                                    //We are the first time in this state
                                    String jsonString = convertStreamToString(inputStream);
                                    buyerProfile = serializationService.deserialize(jsonString, new TypeToken<UserProfile>(){}.getType());
                                    if(buyerProfile.getProfileImagePath() != null)
                                        break;
                                }else{
                                    //We receive the profile image of the seller
                                    File tempFile = FileManager.createTemporaryFile("image", "jpg");
                                    FileManager.copyInputStreamToFile(inputStream, tempFile);
                                    buyerProfile.setProfileImagePath(tempFile.getAbsolutePath());
                                }

                                callback.onUserProfileReceived(buyerProfile);
                                callback.onUserProfileRequested(SellerPeer.this);
                                break;
                            case ExpectTransmissionConfirmed:
                                String confirmedString = convertStreamToString(inputStream);
                                serializationService.deserialize(confirmedString, new TypeToken<TransmissionConfirmedResponse>(){}.getType());
                                callback.onTransmissionConfirmed();
                                running = false;
                        }
                    }

                } catch (Exception e) {
                    //todo:error handling based on states
                    callback.onWifiResponse(new WifiResponse(false, e, "An error occurred while communicating with the other peer."));
                    stop();
                }
                finally {
                    try {
                        serverSocket.close();
                        stop();
                    } catch (IOException e) {
                        //todo: log
                        e.printStackTrace();
                    }
                }
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    public void stop()
    {
        if(!mainTask.isDone())
            mainTask.cancel(true);

        if(sendProfileTask != null)
            sendProfileTask.interrupt();

        if(sendContractTask != null)
            sendContractTask.interrupt();

        stoppedHandler.OnTradingPeerStopped();
    }

    @Override
    public void onContractInfoReceived(final ContractInfo contractInfo) {

        sendContractTask = new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    state = WifiServerState.ExpectTransmissionConfirmed;
                    client.sendContract(contractInfo);
                }catch(IOException ex)
                {
                    stop();
                }
            }
        });

        sendContractTask.start();

        /*
        Async.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                state = WifiServerState.ExpectTransmissionConfirmed;
                client.sendContract(contractInfo);

                //send all file of the contract to the buyer
                for(String file : contractInfo.getImages().values())
                {
                    client.sendFile(file);
                }

                return null;
            }
        }).fail(new FailCallback() {
                    @Override
                    public void onFail(Throwable result) {
                        stop();
                    }
                });*/
    }

    @Override
    public void onUserProfileReceived(final UserProfile profile) {

        sendProfileTask = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    client.sendProfile(profile);
                    callback.onContractInfoRequested(SellerPeer.this);
                }catch(IOException ex)
                {
                    //todo:handle error
                    stop();
                }
            }
        });

        sendProfileTask.start();

        /*
        Async.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                client.sendProfile(profile);
                callback.onContractInfoRequested(SellerPeer.this);
                return null;
            }
        }).fail(new FailCallback() {
            @Override
            public void onFail(Throwable result) {
                stop();
            }
        });*/
    }

    private enum WifiServerState
    {
        ExpectConnectionRequest,
        ExpectUserProfile,
        ExpectTransmissionConfirmed
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
