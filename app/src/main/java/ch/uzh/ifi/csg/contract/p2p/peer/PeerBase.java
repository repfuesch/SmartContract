package ch.uzh.ifi.csg.contract.p2p.peer;

import android.util.Log;

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
import java.util.logging.Logger;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.util.FileUtil;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import ch.uzh.ifi.csg.contract.service.serialization.SerializationService;

/**
 * Base class for all {@link Peer} implementations.
 * Expects the port and hostname and {@link P2pCallback} in its constructor. It its "start" method,
 * it starts a new Task and establishes a Socket connection depending on its role in the Wi-Fi P2P
 * group.
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

    /**
     * Cancels the main task when it has not already finished
     */
    @Override
    public void stop()
    {
        if(!mainTask.isDone())
            mainTask.cancel(true);
    }

    /**
     * Start the concrete protocol implementation after the Socket connection has been established.
     *
     * see {@link BuyerPeer#startProtocol()}
     * see {@link SellerPeer#startProtocol()}
     *
     */
    protected abstract void startProtocol();

    /**
     * Tries to establish a socket connection  depending on the role in the Wi-Fi direct group.
     * The GroupOwner always opens a ServerSocket and the non-GroupOwner always opens a client
     * Socket.
     */
    private void waitForSocketConnection()
    {
        if(hostname == null)
        {
            /**
             * We are in the role of the Group Owner. We open a the ServerSocket and
             * wait until the other peer connects to us
             **/

            ServerSocket serverSocket = null;
            try{
                //wait such that other peer can detect the free local port
                Thread.sleep(3000);

                serverSocket = new ServerSocket(port);
                peerSocket = serverSocket.accept();
                outputStream = new DataOutputStream(peerSocket.getOutputStream());
                inputStream = new DataInputStream(peerSocket.getInputStream());
                client = new P2pClientImpl(new GsonSerializationService(), outputStream);

            }catch(Exception ex)
            {
                onError(ex, "An error occurred while opening the connection to the other peer");
            }
            finally {
                if(serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        Log.d("P2P", "IoException during socket close", e);
                    }
                }
            }

        }else{
            /**
             * We are not in the role of the Group owner. We open a Socket and connect to the
             * Group owner
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
                    Log.d("P2P", "InterruptedException during thread sleep", e);
                }
            } catch (IOException e)
            {
                onError(e, "An error occurred while opening the connection to the other peer");
            }
        }
    }

    protected void onError(Throwable exception, String message)
    {
        Log.e("P2P", message, exception);
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
