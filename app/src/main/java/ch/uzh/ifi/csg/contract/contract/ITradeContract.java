package ch.uzh.ifi.csg.contract.contract;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;

/**
 *  Interface that declares common methods for all contract implementations.
 *  It declares methods used to store/manipulate local contract information
 *  and remote accessors to access information that is stored on the smart contract.
 */

public interface ITradeContract extends IContractObservable
{
    //local fields and methods
    String getContractAddress();
    UserProfile getUserProfile();
    void setUserProfile(UserProfile profile);
    void addImage(String signature, String filename);
    Map<String, String> getImages();
    ContractType getContractType();
    boolean isLightContract();
    String toJson();

    //remote transaction methods
    SimplePromise<String> abort();

    //remote getters for smart contract attributes
    SimplePromise<List<String>> getImageSignatures();
    SimplePromise<String> getSeller();
    SimplePromise<BigInteger> getPrice();
    SimplePromise<BigInteger> getDeposit();
    SimplePromise<String> getTitle();
    SimplePromise<String> getDescription();
    SimplePromise<String> getBuyer();
    SimplePromise<ContractState> getState();
    SimplePromise<Boolean> getVerifyIdentity();
    SimplePromise<String> getContentHash();
    SimplePromise<Boolean> verifyContent();
}
