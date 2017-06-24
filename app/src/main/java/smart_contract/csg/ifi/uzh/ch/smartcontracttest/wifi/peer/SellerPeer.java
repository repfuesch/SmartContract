package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.FailCallback;
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

    private ScheduledFuture task;

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
        task = Async.getExecutorService().schedule(new Runnable() {
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
                        Thread.sleep(1000);
                        serverSocket = new ServerSocket(port);
                        state = WifiServerState.ExpectConnectionRequest;
                    }

                    serverSocket.setReuseAddress(true);

                    while(running)
                    {
                        Socket clientSocket = serverSocket.accept();
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
                                String jsonString = convertStreamToString(inputStream);
                                UserProfile userProfile = serializationService.deserialize(jsonString, new TypeToken<UserProfile>(){}.getType());
                                callback.onUserProfileReceived(userProfile);
                                callback.onUserProfileRequested(SellerPeer.this);
                                break;
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
        if(!task.isDone())
            task.cancel(true);

        stoppedHandler.OnTradingPeerStopped();
    }

    @Override
    public void onContractInfoReceived(final ContractInfo contractInfo) {
        Async.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                client.sendContract(contractInfo);
                stop();

                return null;
            }
        }).fail(new FailCallback() {
                    @Override
                    public void onFail(Throwable result) {
                        stop();
                    }
                });
    }

    @Override
    public void onUserProfileReceived(final UserProfile profile) {
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
        });
    }

    private enum WifiServerState
    {
        ExpectConnectionRequest,
        ExpectUserProfile
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
