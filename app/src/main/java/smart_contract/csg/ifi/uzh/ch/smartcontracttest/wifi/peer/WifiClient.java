package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.serialization.SerializationService;

/**
 * Created by flo on 18.06.17.
 */

public class WifiClient implements TradingClient
{
    private InetAddress host;
    private int port;
    private SerializationService serializationService;

    public WifiClient(int port, SerializationService serializationService)
    {
        this.port = port;
        this.serializationService = serializationService;
    }

    public WifiClient(InetAddress host, int port, SerializationService serializationService)
    {
        this(port, serializationService);
        this.host = host;
    }

    @Override
    public InetAddress getHost() {
        return host;
    }

    @Override
    public void waitConnectionAvailable() {
        boolean scanning=true;
        while(scanning)
        {
            try
            {
                Socket socket = new Socket();
                InetSocketAddress sa = new InetSocketAddress(this.host, this.port);
                socket.connect(sa, 500);
                scanning=false;
            }
            catch(ConnectException e)
            {
                System.out.println("Connect failed, waiting and trying again");
                try
                {
                    Thread.sleep(1000);//2 seconds
                }
                catch(InterruptedException ie){
                    ie.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setHost(InetAddress address)
    {
        this.host = address;
    }

    public void sendConnectionConfiguration(final ConnectionConfig config) throws IOException
    {
        sendJson(serializationService.serialize(config));
    }

    public void sendConnectionRequest(final ConnectionRequest request) throws IOException
    {
        sendJson(serializationService.serialize(request));
    }

    public void sendContract(final ContractInfo contractInfo) throws IOException
    {
        List<String> imageList = new ArrayList<>(contractInfo.getImages().size());
        for(String path : contractInfo.getImages().values())
        {
            imageList.add(path);
        }

        sendJson(serializationService.serialize(contractInfo));

        for(String path : imageList)
        {
            sendFile(path);
        }
    }

    @Override
    public void sendRequestResponse(RequestResponse requestResponse) throws IOException {
        sendJson(serializationService.serialize(requestResponse));
    }

    public void sendProfile(final UserProfile userProfile) throws IOException
    {
        String imagePath = userProfile.getProfileImagePath();
        sendJson(serializationService.serialize(userProfile));
        if(imagePath != null)
            sendFile(imagePath);
    }

    private void sendJson(String json) throws IOException
    {
        send(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
    }

    private void sendFile(String path) throws IOException
    {
        send(new FileInputStream(path));
    }

    private void send(final InputStream inputStream) throws IOException
    {
        int len;
        Socket socket = new Socket();
        byte buf[] = new byte[1024];

        /**
         * Create a client socket with the host,
         * port, and timeout information.
         */
        socket.bind(null);
        socket.connect((new InetSocketAddress(host, port)), 500);

        /**
         * Create a byte stream from a JPEG file and pipe it to the output stream
         * of the socket. This data will be retrieved by the buyerServer device.
         */
        OutputStream outputStream = socket.getOutputStream();
        while ((len = inputStream.read(buf)) != -1) {
            outputStream.write(buf, 0, len);
        }

        outputStream.close();
        inputStream.close();
    }
}
