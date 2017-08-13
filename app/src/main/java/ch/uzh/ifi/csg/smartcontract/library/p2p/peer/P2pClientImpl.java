package ch.uzh.ifi.csg.smartcontract.library.p2p.peer;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.csg.smartcontract.library.datamodel.ContractInfo;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.UserProfile;
import ch.uzh.ifi.csg.smartcontract.library.service.serialization.SerializationService;

/**
 * Implements the {@link P2pClient} interface.
 * Serializes ContractInfo- and UserProfile objects and writes them to the outputStream provided in
 * the constructor.
 *
 */
public class P2pClientImpl implements P2pClient {

    private SerializationService serializationService;
    private DataOutputStream outputStream;

    public P2pClientImpl(SerializationService serializationService, DataOutputStream outputStream)
    {
        this.outputStream = outputStream;
        this.serializationService = serializationService;
    }

    @Override
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

        if(contractInfo.getUserProfile().getProfileImagePath() != null)
        {
            sendFile(contractInfo.getUserProfile().getProfileImagePath());
        }
    }

    @Override
    public void sendProfile(final UserProfile userProfile) throws IOException
    {
        String imagePath = userProfile.getProfileImagePath();
        sendJson(serializationService.serialize(userProfile));
        if(imagePath != null)
            sendFile(imagePath);
    }

    private void sendJson(String json) throws IOException
    {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        send(new ByteArrayInputStream(bytes), bytes.length);
    }

    private void sendFile(String path) throws IOException
    {
        File file = new File(path);
        send(new FileInputStream(file), file.length());
    }

    private void send(final InputStream inputStream, long len) throws IOException
    {
        int buflen;
        byte buf[] = new byte[1024];

        //first write length of the inputStream that the receivers knows how many bytes to read
        outputStream.writeLong(len);

        while ((buflen = inputStream.read(buf)) != -1) {
            outputStream.write(buf, 0, buflen);
        }

        outputStream.flush();
        inputStream.close();
    }
}
