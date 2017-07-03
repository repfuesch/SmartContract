package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.FailCallback;
import ch.uzh.ifi.csg.contract.common.FileManager;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import ch.uzh.ifi.csg.contract.service.serialization.SerializationService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service.ContractInfoListener;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service.UserProfileListener;

/**
 * Created by flo on 30.06.17.
 */

public class SellerPeerImpl implements  TradingPeer, UserProfileListener, ContractInfoListener
{
    private WifiSellerCallback callback;
    private WifiClientImpl client;
    private SerializationService serializationService;
    private OnTradingPeerStoppedHandler stoppedHandler;
    private Integer port;
    private ScheduledFuture mainTask;

    private ContractInfo contractInfo;
    private UserProfile buyerProfile;

    private Socket peerSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private String hostname;
    private boolean useIdentity;

    public SellerPeerImpl(SerializationService serializationService, WifiSellerCallback callback, OnTradingPeerStoppedHandler stoppedHandler, Integer port, String host, boolean useIdentity)
    {
        this.serializationService = serializationService;
        this.callback = callback;
        this.stoppedHandler = stoppedHandler;
        this.port = port;
        this.hostname = host;
        this.useIdentity = useIdentity;
    }

    @Override
    public void start() {
        mainTask = Async.getExecutorService().schedule(new Runnable() {
            @Override
            public void run() {

                waitForSocketConnection();
                try {
                    client.sendConnectionConfiguration(new ConnectionConfig(useIdentity));
                } catch (IOException e) {
                    //todo: handler error
                    e.printStackTrace();
                }

                if(useIdentity)
                {
                    awaitProfileInfo();
                }else{
                    callback.onContractInfoRequested(SellerPeerImpl.this);
                }

            }
        }, 0, TimeUnit.MILLISECONDS);

    }

    private void waitForSocketConnection()
    {
        if(hostname == null)
        {
            /**
             * We are in the role of the Groupowner. We wait until the other peer connects
             **/

            ServerSocket serverSocket = null;
            try{
                //wait such that other peer can detect the free local port
                Thread.sleep(3000);
                boolean running = true;
                serverSocket = new ServerSocket(port);
                peerSocket = serverSocket.accept();
                outputStream = new DataOutputStream(peerSocket.getOutputStream());
                inputStream = new DataInputStream(peerSocket.getInputStream());
                client = new WifiClientImpl(new GsonSerializationService(), outputStream);

            }catch(Exception ex)
            { //todo: handle error
                stop();
            }
            finally {
                if(serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }else{
            /**
             * We are not in the role of the Groupowner. We connect to the group owner
             **/
            try
            {
                peerSocket = new Socket();
                InetSocketAddress sa = new InetSocketAddress(hostname, port);
                peerSocket.connect(sa, 500);
                outputStream = new DataOutputStream(peerSocket.getOutputStream());
                inputStream = new DataInputStream(peerSocket.getInputStream());
                client = new WifiClientImpl(new GsonSerializationService(), outputStream);
            }
            catch(ConnectException | SocketTimeoutException e)
            {
                try
                {
                    Thread.sleep(1000);//2 seconds
                    waitForSocketConnection();
                }
                catch(InterruptedException ie){
                    ie.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void awaitProfileInfo()
    {
        try{
            String jsonString = readString(inputStream);
            buyerProfile = serializationService.deserialize(jsonString, new TypeToken<UserProfile>(){}.getType());
            if(buyerProfile.getProfileImagePath() != null)
            {
                File tempFile = FileManager.createTemporaryFile("image", "jpg");
                readFile(inputStream, tempFile);
            }

            callback.onUserProfileReceived(buyerProfile);
            callback.onUserProfileRequested(this);

        }catch(IOException e)
        {
            //todo:handle error
        }
    }

    private String readString(DataInputStream inputStream)
    {
        try{
            int len = (int)inputStream.readLong();
            byte[] buffer = new byte[len];
            inputStream.read(buffer, 0, len);
            String s = new String(buffer, "UTF-8");
            return s;

        }catch (IOException e)
        {
            //todo: handle error
            callback.onWifiResponse(new WifiResponse(false, e, e.getLocalizedMessage()));
            return null;
        }
    }

    private void readFile(DataInputStream inputStream, File file)
    {
        byte buffer[] = new byte[1024];
        try{
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file));
            long bytesLeft = inputStream.readLong();


            while (bytesLeft > 0) {
                int read = inputStream.read(buffer, 0, Math.min((int)bytesLeft, buffer.length));
                outputStream.write(buffer, 0, read);
                bytesLeft -= read;
            }

            outputStream.close();
        }catch(IOException e)
        {
            //todo: handle error
            callback.onWifiResponse(new WifiResponse(false, e, e.getLocalizedMessage()));
        }
    }

    private static String convertStreamToString(DataInputStream is) {

        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public void stop()
    {
        if(!mainTask.isDone())
            mainTask.cancel(true);

        stoppedHandler.OnTradingPeerStopped();
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

            }
        }).done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void result) {
                //wait for seller profile
                callback.onContractInfoRequested(SellerPeerImpl.this);
            }
        });
    }

    @Override
    public void onContractInfoReceived(final ContractInfo contractInfo) {

        Async.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                client.sendContract(contractInfo);
                return null;
            }
        }).fail(new FailCallback() {
            @Override
            public void onFail(Throwable result) {

            }
        }).done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void result) {
                //wait for seller profile
                awaitTransmissionConfirmed();
            }
        });
    }

    private void awaitTransmissionConfirmed()
    {
        String jsonString = readString(inputStream);
        serializationService.deserialize(jsonString, new TypeToken<TransmissionConfirmedResponse>(){}.getType());
        callback.onTransmissionConfirmed();
        stop();
    }
}
