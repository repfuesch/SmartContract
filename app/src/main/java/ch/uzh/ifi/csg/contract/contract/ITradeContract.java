package ch.uzh.ifi.csg.contract.contract;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.event.IContractObservable;

/**
 * Created by flo on 03.06.17.
 */

public interface ITradeContract extends IContractObservable
{
    String getContractAddress();
    UserProfile getUserProfile();
    void setUserProfile(UserProfile profile);
    void addImage(String signature, String filename);
    Map<String, String> getImages();
    ContractType getContractType();

    SimplePromise<List<String>> getImageSignatures();
    SimplePromise<String> setImageSignatures(List<String> imageSignatures);
    SimplePromise<String> getSeller();
    SimplePromise<BigInteger> getPrice();
    SimplePromise<BigInteger> getDeposit();
    SimplePromise<String> getTitle();
    SimplePromise<String> getDescription();
    SimplePromise<String> getBuyer();
    SimplePromise<ContractState> getState();
    SimplePromise<Boolean> getVerifyIdentity();

    SimplePromise<String> abort();
}
