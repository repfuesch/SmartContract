package ch.uzh.ifi.csg.contract.contract;

import java.math.BigInteger;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.event.IContractObservable;
import ch.uzh.ifi.csg.contract.service.account.UserProfile;

public interface IPurchaseContract extends IContractObservable {
	
	String getContractAddress();
    UserProfile getUserProfile();
    void setUserProfile(UserProfile profile);

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
