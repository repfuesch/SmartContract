package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

import android.content.pm.ConfigurationInfo;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.FailCallback;
import ch.uzh.ifi.csg.contract.common.FileManager;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.serialization.SerializationService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service.UserProfileListener;

/**
 * Created by flo on 18.06.17.
 */

public class BuyerPeer implements TradingPeer, UserProfileListener {

    private BuyerPeerState state;
    private WifiBuyerCallback callback;
    private TradingClient client;
    private SerializationService serializationService;
    private OnTradingPeerStoppedHandler stoppedHandler;
    private Integer port;

    private ContractInfo contractInfo;
    private UserProfile sellerProfile;

    private ScheduledFuture task;

    public BuyerPeer(SerializationService serializationService, WifiBuyerCallback callback, TradingClient client, OnTradingPeerStoppedHandler stoppedHandler, Integer port)
    {
        this.serializationService = serializationService;
        this.state = BuyerPeerState.ExpectConnectionConfig;
        this.callback = callback;
        this.client = client;
        this.stoppedHandler = stoppedHandler;
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
                        //send connection request to seller peer first
                        client.sendConnectionRequest(new ConnectionRequest(serverSocket.getLocalPort()));
                        state = BuyerPeerState.ExpectConnectionConfig;

                    }else{
                        //wait such that other peer can detect the free local port
                        Thread.sleep(1000);
                        serverSocket = new ServerSocket(port);
                        state = BuyerPeerState.ExpectConnectionRequest;
                    }

                    serverSocket.setReuseAddress(true);

                    while(running)
                    {
                        Socket socketClient = serverSocket.accept();
                        InputStream inputStream = socketClient.getInputStream();

                        switch(state)
                        {
                            case ExpectConnectionRequest:
                                String connRequestString = convertStreamToString(inputStream);
                                ConnectionRequest request = serializationService.deserialize(connRequestString, new TypeToken<ConnectionRequest>(){}.getType());
                                InetAddress peerAddress = socketClient.getInetAddress();
                                client.setHost(peerAddress.getHostAddress());
                                client.setPort(request.getListeningPort());
                                state = BuyerPeerState.ExpectConnectionConfig;
                                break;
                            case ExpectConnectionConfig:
                                String configString = convertStreamToString(inputStream);
                                ConnectionConfig config = serializationService.deserialize(configString, new TypeToken<ConnectionConfig>(){}.getType());
                                if(config.isIdentificationUsed())
                                {
                                    callback.onUserProfileRequested(BuyerPeer.this);
                                    state = BuyerPeerState.ExpectSellerProfile;
                                }else{
                                    state = BuyerPeerState.ExpectContractInfo;
                                }
                                break;
                            case ExpectSellerProfile:

                                if(sellerProfile == null) {
                                    //We are the first time in this state
                                    String jsonString = convertStreamToString(inputStream);
                                    sellerProfile = serializationService.deserialize(jsonString, new TypeToken<UserProfile>(){}.getType());
                                    if(sellerProfile.getProfileImagePath() != null)
                                        return;
                                }else{
                                    //We receive the profile image of the seller
                                    File tempFile = FileManager.createTemporaryFile("image", "jpg");
                                    FileManager.copyInputStreamToFile(inputStream, tempFile);
                                    sellerProfile.setProfileImagePath(tempFile.getAbsolutePath());
                                }

                                callback.onUserProfileReceived(sellerProfile);
                                state = BuyerPeerState.ExpectContractInfo;
                                break;
                            case ExpectContractInfo:

                                if(contractInfo == null)
                                {
                                    //We are the first time in this state
                                    String jsonString2 = convertStreamToString(inputStream);
                                    contractInfo = serializationService.deserialize(jsonString2, new TypeToken<ContractInfo>(){}.getType());

                                    //reset the image paths received in the image map
                                    for(String sig : contractInfo.getImages().keySet())
                                    {
                                        contractInfo.getImages().put(sig, null);
                                    }

                                    if(contractInfo.getImages().size() != 0)
                                        break;

                                }else{
                                    //We receive additional image files belonging to the contract and save them in a temporary file
                                    File tempFile = FileManager.createTemporaryFile("image", "jpg");
                                    FileManager.copyInputStreamToFile(inputStream, tempFile);

                                    int count = 0;
                                    for(String sig : contractInfo.getImages().keySet())
                                    {
                                        count++;
                                        if(contractInfo.getImages().get(sig) == null)
                                        {
                                            contractInfo.getImages().put(sig, tempFile.getAbsolutePath());
                                            break;
                                        }
                                    }

                                    if(count != contractInfo.getImages().size())
                                        break;
                                }

                                callback.onContractInfoReceived(contractInfo);
                                client.sendTransmissionConfirmed(new TransmissionConfirmedResponse());
                                running = false;
                                break;
                        }
                    }

                } catch (Exception e) {
                    //todo:exception handling
                    callback.onWifiResponse(new WifiResponse(false, e, "An error occurred while communicating with the other peer."));
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

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    public void onUserProfileReceived(final UserProfile profile) {

        Async.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                client.sendProfile(profile);
                return null;
            }
        }).fail(new FailCallback() {
            @Override
            public void onFail(Throwable result) {
                //todo:handle error
                stop();
            }
        });
    }

    private enum BuyerPeerState
    {
        ExpectConnectionRequest,
        ExpectConnectionConfig,
        ExpectSellerProfile,
        ExpectContractInfo
    }
}
