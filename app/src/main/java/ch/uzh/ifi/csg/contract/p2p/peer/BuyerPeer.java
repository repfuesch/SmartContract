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
 * Buyer implementation of the {@link Peer} interface. Waits for the contract details of the seller
 * and returns the local UserProfile if requested.
 */
public class BuyerPeer extends PeerBase implements UserProfileListener {

    private P2pBuyerCallback callback;
    private ContractInfo contractInfo;

    public BuyerPeer(SerializationService serializationService, P2pBuyerCallback callback, Integer port, String host)
    {
        super(serializationService, callback,  port, host);
        this.callback = callback;
    }

    @Override
    protected void startProtocol()
    {
        awaitContractInfo();
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
                    //We receive additional image files belonging to the contract and save them
                    //first in a temporary file
                    File tempFile = FileUtil.createTemporaryFile("image", "jpg");
                    readFile(inputStream, tempFile);
                    contractInfo.getImages().put(sig, tempFile.getAbsolutePath());
                }
            }

            if(contractInfo.getUserProfile().getProfileImagePath() != null)
            {
                callback.onP2pInfoMessage("Receiving profile image");

                //receive and store profile image
                File tempFile = FileUtil.createTemporaryFile("image", "jpg");
                readFile(inputStream, tempFile);
                contractInfo.getUserProfile().setProfileImagePath(tempFile.getAbsolutePath());
            }

            callback.onContractInfoReceived(contractInfo);

            if(contractInfo.getUserProfile().getVCard() == null)
            {
                stop();
                callback.onP2pInfoMessage("Transmission complete");
                callback.onTransmissionComplete();
            }else{
                //request and wait for the UserProfile from the callback
                callback.onUserProfileRequested(this);
            }
        }
        catch(IOException e)
        {
            onError(e, "An error occurred during communication with the other peer");
        }
    }

    @Override
    public void onUserProfileReceived(final UserProfile profile)
    {
        //run in background thread because it would otherwise block the UI thread
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
                onError(result, "An error occurred during communication with the other peer");
            }
        }).done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void result) {
                stop();
                callback.onP2pInfoMessage("Transmission complete");
                callback.onTransmissionComplete();
            }
        });
    }
}
