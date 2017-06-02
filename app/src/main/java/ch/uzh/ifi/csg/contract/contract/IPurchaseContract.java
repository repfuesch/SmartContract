package ch.uzh.ifi.csg.contract.contract;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.event.IContractObservable;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;

public interface IPurchaseContract extends IContractObservable {
	
	String getContractAddress();

    UserProfile getUserProfile();
    void setUserProfile(UserProfile profile);
    void addImage(String signature, String filename);
    Map<String, String> getImages();

    SimplePromise<List<String>> getImageSignatures();
    SimplePromise<String> setImageSignatures(List<String> imageSignatures);
    SimplePromise<String> seller();
    SimplePromise<String> abort();
    SimplePromise<BigInteger> value();
    SimplePromise<String> title();
    SimplePromise<String> description();
    SimplePromise<String> buyer();
    SimplePromise<String> confirmReceived();
    SimplePromise<ContractState> state();
    SimplePromise<String> confirmPurchase();
    SimplePromise<Boolean> verifyIdentity();
	
}
