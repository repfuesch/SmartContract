package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

import android.provider.ContactsContract;

import com.google.gson.reflect.TypeToken;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service.UserProfileListener;

/**
 * Created by flo on 30.06.17.
 */

public class BuyerPeerImpl implements  TradingPeer, UserProfileListener {
    private WifiBuyerCallback callback;
    private WifiClientImpl client;
    private SerializationService serializationService;
    private OnTradingPeerStoppedHandler stoppedHandler;
    private Integer port;
    private ScheduledFuture mainTask;

    private ContractInfo contractInfo;
    private UserProfile sellerProfile;

    private Socket peerSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private String hostname;

    public BuyerPeerImpl(SerializationService serializationService, WifiBuyerCallback callback, OnTradingPeerStoppedHandler stoppedHandler, Integer port, String host)
    {
        this.serializationService = serializationService;
        this.callback = callback;
        this.stoppedHandler = stoppedHandler;
        this.port = port;
        this.hostname = host;
    }

    @Override
    public void start() {
        mainTask = Async.getExecutorService().schedule(new Runnable() {
            @Override
            public void run() {

                waitForSocketConnection();
                awaitConnectionConfig();

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

    private void awaitConnectionConfig()
    {
        String configString = readString(inputStream);
        ConnectionConfig config = serializationService.deserialize(configString, new TypeToken<ConnectionConfig>(){}.getType());
        if(config.isIdentificationUsed())
        {
            callback.onUserProfileRequested(this);
        }else{
            awaitContractInfo();
        }
    }

    private void awaitProfileInfo()
    {
        try{
            String jsonString = readString(inputStream);
            sellerProfile = serializationService.deserialize(jsonString, new TypeToken<UserProfile>(){}.getType());
            if(sellerProfile.getProfileImagePath() != null)
            {
                File tempFile = FileManager.createTemporaryFile("image", "jpg");
                readFile(inputStream, tempFile);
                //FileManager.copyInputStreamToFile(inputStream, tempFile);
            }

            callback.onUserProfileReceived(sellerProfile);
            awaitContractInfo();

        }catch(IOException e)
        {
            //handle error
        }
    }

    private void awaitContractInfo()
    {
        try{
            String jsonString = readString(inputStream);
            contractInfo = serializationService.deserialize(jsonString, new TypeToken<ContractInfo>(){}.getType());

            //reset the image paths received in the image map
            for(String sig : contractInfo.getImages().keySet())
            {
                contractInfo.getImages().put(sig, null);
            }

            if(contractInfo.getImages().size() != 0)
            {
                for(String sig : contractInfo.getImages().keySet())
                {
                    //We receive additional image files belonging to the contract and save them in a temporary file
                    File tempFile = FileManager.createTemporaryFile("image", "jpg");
                    readFile(inputStream, tempFile);
                    contractInfo.getImages().put(sig, tempFile.getAbsolutePath());
                }
            }

            callback.onContractInfoReceived(contractInfo);
            client.sendTransmissionConfirmed(new TransmissionConfirmedResponse());
            stop();
        }
        catch(IOException e)
        {
            //todo:handle error
            callback.onWifiResponse(new WifiResponse(false, e, e.getLocalizedMessage()));
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
                awaitProfileInfo();
            }
        });
    }
}
