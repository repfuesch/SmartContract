package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.serialization.SerializationService;

/**
 * Created by flo on 18.06.17.
 */

public class SellerPeer implements TradingPeer
{
    private WifiSellerCallback callback;
    private SerializationService serializationService;
    private boolean useIdentification;
    private SellerPeer.WifiServerState state;
    private OnTradingPeerStoppedHandler stoppedHandler;
    private TradingClient client;
    private int port;

    private ScheduledFuture task;

    public SellerPeer(SerializationService serializationService, WifiSellerCallback callback, TradingClient client, OnTradingPeerStoppedHandler stoppedHandler, boolean useIdentification, int port)
    {
        this.serializationService = serializationService;
        this.callback = callback;
        this.useIdentification = useIdentification;
        state = WifiServerState.ExpectConnectionRequest;
        this.stoppedHandler = stoppedHandler;
        this.client = client;
        this.port = port;

        if(client.getHost() == null)
        {
            state = WifiServerState.ExpectConnectionRequest;
        }else{
            state = WifiServerState.ExpectUserProfile;
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
                boolean running = true;
                ServerSocket serverSocket = null;
                try{

                    if(state != WifiServerState.ExpectConnectionRequest)
                    {
                        client.sendConnectionRequest(new ConnectionRequest());
                        //send the connection configuration to the buyers device. If this is successful, we notify the callback
                        //that the buyer is ready to receive the data
                        ConnectionConfig config = new ConnectionConfig(useIdentification);
                        client.sendConnectionConfiguration(config);
                    }

                    serverSocket = new ServerSocket(port);
                    serverSocket.setReuseAddress(true);

                    while(running)
                    {
                        Socket clientSocket = serverSocket.accept();
                        InputStream inputStream = clientSocket.getInputStream();

                        switch(state)
                        {
                            case ExpectConnectionRequest:
                                String connectionString = convertStreamToString(inputStream);
                                serializationService.deserialize(connectionString, new TypeToken<ConnectionRequest>(){}.getType());
                                client.setHost(clientSocket.getInetAddress());
                                //send the connection configuration to the buyers device. If this is successful, we notify the callback
                                //that the buyer is ready to receive the data
                                ConnectionConfig config = new ConnectionConfig(useIdentification);
                                client.sendConnectionConfiguration(config);
                                if(useIdentification)
                                {
                                    state = WifiServerState.ExpectUserProfile;
                                }else{
                                    client.sendProfile(callback.getUserProfile());
                                    client.sendContract(callback.getContractInfo());
                                    running = false;
                                }
                                break;
                            case ExpectUserProfile:
                                String jsonString = convertStreamToString(inputStream);
                                UserProfile userProfile = serializationService.deserialize(jsonString, new TypeToken<UserProfile>(){}.getType());
                                callback.onUserProfileReceived(userProfile);
                                client.sendProfile(callback.getUserProfile());
                                client.sendContract(callback.getContractInfo());
                                running = false;
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

    private enum WifiServerState
    {
        ExpectConnectionRequest,
        ExpectUserProfile
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private static void writeJson(Object obj, OutputStream os, SerializationService serializationService) throws IOException {
        String data = serializationService.serialize(obj);
        os.write(data.getBytes());
    }
}
