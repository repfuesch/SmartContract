package ch.uzh.ifi.csg.contract.p2p.peer;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.FailCallback;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.serialization.SerializationService;
import ch.uzh.ifi.csg.contract.util.FileUtil;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.ContractInfoListener;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.UserProfileListener;

/**
 * Created by flo on 30.06.17.
 */

public class SellerPeer extends PeerBase implements ContractInfoListener
{
    private P2pSellerCallback callback;

    public SellerPeer(SerializationService serializationService, P2pSellerCallback callback, Integer port, String host)
    {
        super(serializationService, callback, port, host);
        this.callback = callback;
    }

    @Override
    protected void startProtocol() {
        callback.onContractInfoRequested(SellerPeer.this);
    }

    protected void awaitProfileInfo() throws IOException
    {
        callback.onP2pInfoMessage("Receiving the profile of the other peer");
        String jsonString = readString(inputStream);
        userProfile = serializationService.deserialize(jsonString, new TypeToken<UserProfile>(){}.getType());
        if(userProfile.getProfileImagePath() != null)
        {
            callback.onP2pInfoMessage("Receiving profile image");
            File tempFile = FileUtil.createTemporaryFile("image", "jpg");
            readFile(inputStream, tempFile);
            userProfile.setProfileImagePath(tempFile.getAbsolutePath());
        }

        callback.onUserProfileReceived(userProfile);
    }

    @Override
    public void onContractInfoReceived(final ContractInfo contractInfo) {

        Async.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                callback.onP2pInfoMessage("Sending contract information");
                client.sendContract(contractInfo);
                if(contractInfo.getUserProfile().getVCard() != null)
                {
                    awaitProfileInfo();
                }
                return null;
            }
        }).fail(new FailCallback() {
            @Override
            public void onFail(Throwable result) {
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
