package ch.uzh.ifi.csg.contract.p2p.peer;

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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.util.FileUtil;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import ch.uzh.ifi.csg.contract.service.serialization.SerializationService;

/**
 * Created by flo on 03.07.17.
 */

public abstract class PeerBase implements Peer {

    private Integer port;
    private String hostname;
    private ScheduledFuture mainTask;
    private P2pCallback callback;

    protected SerializationService serializationService;
    protected P2pClient client;
    protected UserProfile userProfile;
    protected Socket peerSocket;
    protected DataInputStream inputStream;
    protected DataOutputStream outputStream;

    public PeerBase(SerializationService serializationService, P2pCallback callback, Integer port, String host)
    {
        this.serializationService = serializationService;
        this.callback = callback;
        this.port = port;
        this.hostname = host;
    }


    @Override
    public void start() {
        mainTask = Async.getScheduledExecutorService().schedule(new Runnable() {
            @Override
            public void run() {

                waitForSocketConnection();
                startProtocol();

            }
        }, 0, TimeUnit.MILLISECONDS);

    }

    @Override
    public void stop()
    {
        if(!mainTask.isDone())
            mainTask.cancel(true);
    }

    protected abstract void startProtocol();

    private void waitForSocketConnection()
    {
        if(hostname == null)
        {
            /**
             * We are in the role of the Group Owner. We wait until the other peer connects to us
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
                client = new P2pClientImpl(new GsonSerializationService(), outputStream);

            }catch(Exception ex)
            {   //todo:log
                onError(ex, "An error occurred while opening the connection to the other peer");
            }
            finally {
                if(serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        //todo:log
                    }
                }
            }

        }else{
            /**
             * We are not in the role of the Group owner. We connect to the group owner
             **/
            try
            {
                peerSocket = new Socket();
                InetSocketAddress sa = new InetSocketAddress(hostname, port);
                peerSocket.connect(sa, 500);
                outputStream = new DataOutputStream(peerSocket.getOutputStream());
                inputStream = new DataInputStream(peerSocket.getInputStream());
                client = new P2pClientImpl(new GsonSerializationService(), outputStream);
            }
            catch(ConnectException | SocketTimeoutException e)
            {
                try
                {
                    Thread.sleep(1000);//2 seconds
                    waitForSocketConnection();
                }
                catch(InterruptedException ie){
                    //todo:log
                }
            } catch (IOException e) {

                onError(e, "An error occurred while opening the connection to the other peer");
            }
        }
    }

    protected void onError(Throwable exception, String message)
    {
        //todo:log exception
        callback.onP2pErrorMessage(message);
        stop();
    }

    protected String readString(DataInputStream inputStream) throws IOException
    {
        int len = (int)inputStream.readLong();
        byte[] buffer = new byte[len];
        inputStream.read(buffer, 0, len);
        String s = new String(buffer, "UTF-8");
        return s;
    }

    protected void readFile(DataInputStream inputStream, File file) throws IOException
    {
        byte buffer[] = new byte[1024];
        DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(file));
        long bytesLeft = inputStream.readLong();


        while (bytesLeft > 0) {
            int read = inputStream.read(buffer, 0, Math.min((int)bytesLeft, buffer.length));
            outputStream.write(buffer, 0, read);
            bytesLeft -= read;
        }

        outputStream.close();
    }

}
