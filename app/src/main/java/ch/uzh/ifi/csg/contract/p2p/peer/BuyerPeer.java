package ch.uzh.ifi.csg.contract.p2p.peer;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.FailCallback;
import ch.uzh.ifi.csg.contract.util.FileUtil;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.serialization.SerializationService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.UserProfileListener;

/**
 * Created by flo on 30.06.17.
 */

public class BuyerPeer extends PeerBase implements UserProfileListener {

    private P2pBuyerCallback callback;
    private ContractInfo contractInfo;

    public BuyerPeer(SerializationService serializationService, P2pBuyerCallback callback, OnPeerStoppedHandler stoppedHandler, Integer port, String host)
    {
        super(serializationService, callback, stoppedHandler, port, host);
        this.callback = callback;
    }

    @Override
    protected void startProtocol()
    {
        awaitConnectionConfig();
    }

    private void awaitConnectionConfig()
    {
        try{
            callback.onP2pInfoMessage("Reading connection configuration");
            String configString = readString(inputStream);
            ConnectionConfig config = serializationService.deserialize(configString, new TypeToken<ConnectionConfig>(){}.getType());
            if(config.isIdentificationUsed())
            {
                callback.onP2pInfoMessage("Waiting for local user profile");
                callback.onUserProfileRequested(this);
            }else{
                awaitContractInfo();
            }
        }catch(IOException ex)
        {
            callback.onP2pErrorMessage("An error occurred during communication with the other peer");
            //todo:log
        }
    }

    protected void awaitProfileInfo()
    {
        try{
            super.awaitProfileInfo();
            awaitContractInfo();
        }catch(IOException ex)
        {
            callback.onP2pErrorMessage("An error occurred during communication with the other peer");
            //todo:log
        }
    }

    private void awaitContractInfo()
    {
        try{
            callback.onP2pInfoMessage("Receiving contract information");
            String jsonString = readString(inputStream);
            contractInfo = serializationService.deserialize(jsonString, new TypeToken<ContractInfo>(){}.getType());

            //reset the image paths received in the image map
            for(String sig : contractInfo.getImages().keySet())
            {
                contractInfo.getImages().put(sig, null);
            }

            if(contractInfo.getImages().size() != 0)
            {
                callback.onP2pInfoMessage("Receiving contract images");
                for(String sig : contractInfo.getImages().keySet())
                {
                    //We receive additional image files belonging to the contract and save them in a temporary file
                    File tempFile = FileUtil.createTemporaryFile("image", "jpg");
                    readFile(inputStream, tempFile);
                    contractInfo.getImages().put(sig, tempFile.getAbsolutePath());
                }
            }

            callback.onContractInfoReceived(contractInfo);
            callback.onP2pInfoMessage("Transmission complete");
            client.sendTransmissionConfirmed(new TransmissionConfirmedResponse());
            stop();
        }
        catch(IOException e)
        {
            callback.onP2pErrorMessage("An error occurred during communication with the other peer");
            //todo:log
        }
    }

    @Override
    public void onUserProfileReceived(final UserProfile profile) {

        Async.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                callback.onP2pInfoMessage("Sending user profile");
                client.sendProfile(profile);
                return null;
            }
        }).fail(new FailCallback() {
            @Override
            public void onFail(Throwable result)
            {
                callback.onP2pErrorMessage("An error occurred during communication with the other peer");
                //todo:log
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
