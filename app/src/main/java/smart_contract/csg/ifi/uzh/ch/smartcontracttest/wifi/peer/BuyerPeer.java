package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

import android.content.pm.ConfigurationInfo;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.serialization.SerializationService;

/**
 * Created by flo on 18.06.17.
 */

public class BuyerPeer implements TradingPeer {

    private BuyerPeerState state;
    private WifiBuyerCallback callback;
    private TradingClient client;
    private SerializationService serializationService;
    private OnTradingPeerStoppedHandler stoppedHandler;
    private int port;

    private ContractInfo contractInfo;

    private ScheduledFuture task;

    public BuyerPeer(SerializationService serializationService, WifiBuyerCallback callback, TradingClient client, OnTradingPeerStoppedHandler stoppedHandler, int port)
    {
        this.serializationService = serializationService;
        this.state = BuyerPeerState.ExpectConnectionConfig;
        this.callback = callback;
        this.client = client;
        this.stoppedHandler = stoppedHandler;
        this.port = port;

        if(client.getHost() == null)
        {
            state = BuyerPeerState.ExpectConnectionRequest;
        }else{
            state = BuyerPeerState.ExpectConnectionConfig;
        }
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
                ServerSocket serverSocket = null;
                try{

                    if(state != BuyerPeerState.ExpectConnectionRequest)
                    {
                        client.waitConnectionAvailable();
                        //send connection request to seller peer first
                        client.sendConnectionRequest(new ConnectionRequest());
                    }

                    serverSocket = new ServerSocket(port);

                    while(true)
                    {
                        Socket socketClient = serverSocket.accept();

                        InputStream inputStream = socketClient.getInputStream();

                        switch(state)
                        {
                            case ExpectConnectionRequest:
                                String connRequestString = convertStreamToString(inputStream);
                                serializationService.deserialize(connRequestString, new TypeToken<ConnectionRequest>(){}.getType());
                                InetAddress peerAddress = socketClient.getInetAddress();
                                client.setHost(peerAddress);
                                state = BuyerPeerState.ExpectConnectionConfig;
                                break;
                            case ExpectConnectionConfig:
                                String configString = convertStreamToString(inputStream);
                                ConnectionConfig config = serializationService.deserialize(configString, new TypeToken<ConnectionConfig>(){}.getType());
                                if(config.isIdentificationUsed())
                                {
                                    client.sendProfile(callback.getUserProfile());
                                    state = BuyerPeerState.ExpectSellerProfile;
                                }else{
                                    state = BuyerPeerState.ExpectContractInfo;
                                }
                                client.sendRequestResponse(new RequestResponse(true));
                                break;
                            case ExpectSellerProfile:
                                String jsonString = convertStreamToString(inputStream);
                                UserProfile userProfile = serializationService.deserialize(jsonString, new TypeToken<UserProfile>(){}.getType());
                                callback.onUserProfileReceived(userProfile);
                                state = BuyerPeerState.ExpectContractInfo;
                                break;
                            case ExpectContractInfo:
                                String jsonString2 = convertStreamToString(inputStream);
                                ContractInfo contractInfo = serializationService.deserialize(jsonString2, new TypeToken<ContractInfo>(){}.getType());
                                callback.onContractInfoReceived(contractInfo);
                                stop();
                                break;
                        }

                    }

                } catch (Exception e) {
                    //todo:exception handling
                    callback.onWifiResponse(new WifiResponse(false, e, "Error"));
                    stop();
                    /*
                    switch(state)
                    {
                        case ExpectConnectionConfig:
                            break;
                        case ExpectSellerProfile:
                            callback.onProfileReceived(new WifiDataResponse<UserProfile>(false, e, null));
                            break;
                        case ExpectContractInfo:
                            callback.onContractInfoReceived(new WifiDataResponse<ContractInfo>(false, e, null));
                            break;
                    }
                    */
                }
                finally {
                    try {
                        if(serverSocket != null)
                            serverSocket.close();
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
        task.cancel(true);
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private enum BuyerPeerState
    {
        ExpectConnectionRequest,
        ExpectConnectionConfig,
        ExpectSellerProfile,
        ExpectContractInfo
    }
}
