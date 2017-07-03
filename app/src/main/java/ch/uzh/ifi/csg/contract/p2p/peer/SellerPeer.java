package ch.uzh.ifi.csg.contract.p2p.peer;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.FailCallback;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.serialization.SerializationService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.ContractInfoListener;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.UserProfileListener;

/**
 * Created by flo on 30.06.17.
 */

public class SellerPeer extends PeerBase implements UserProfileListener, ContractInfoListener
{
    private P2pSellerCallback callback;
    private boolean useIdentity;

    public SellerPeer(SerializationService serializationService, P2pSellerCallback callback, OnPeerStoppedHandler stoppedHandler, Integer port, String host, boolean useIdentity)
    {
        super(serializationService, callback, stoppedHandler, port, host);
        this.useIdentity = useIdentity;
        this.callback = callback;
    }

    @Override
    protected void startProtocol() {

        try{
            client.sendConnectionConfiguration(new ConnectionConfig(useIdentity));
            if(useIdentity)
            {
                awaitProfileInfo();
            }else{
                callback.onContractInfoRequested(SellerPeer.this);
            }
        }catch(IOException e)
        {
            callback.onP2pErrorMessage("An error occurred during communication with the other peer");
            //todo:log
        }
    }

    protected void awaitProfileInfo()
    {
        try{
            super.awaitProfileInfo();
            callback.onUserProfileRequested(this);
        }catch(IOException ex)
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
                callback.onP2pInfoMessage("Waiting for local contract information");
                callback.onContractInfoRequested(SellerPeer.this);
            }
        });
    }

    @Override
    public void onContractInfoReceived(final ContractInfo contractInfo) {

        Async.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                callback.onP2pInfoMessage("Sending contract information");
                client.sendContract(contractInfo);
                return null;
            }
        }).fail(new FailCallback() {
            @Override
            public void onFail(Throwable result) {
                callback.onP2pErrorMessage("An error occurred during communication with the other peer");
                //todo:log
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
        try {
            callback.onP2pInfoMessage("Waiting for transmission acknowledgement");
            String jsonString = readString(inputStream);
            serializationService.deserialize(jsonString, new TypeToken<TransmissionConfirmedResponse>() {}.getType());
            callback.onTransmissionConfirmed();
            stop();
        }catch(IOException ex)
        {
            callback.onP2pErrorMessage("An error occurred during communication with the other peer");
            //todo:log
        }
    }
}
