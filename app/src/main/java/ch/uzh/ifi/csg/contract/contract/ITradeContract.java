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
    //local fields and methods
    String getContractAddress();
    UserProfile getUserProfile();
    void setUserProfile(UserProfile profile);
    void addImage(String signature, String filename);
    Map<String, String> getImages();
    ContractType getContractType();

    //remote transaction methods
    SimplePromise<String> setImageSignatures(List<String> imageSignatures);
    SimplePromise<String> abort();

    //remote getters for smart contract attributes
    List<String> getImageSignatures() throws Exception;
    String getSeller() throws Exception;
    BigInteger getPrice() throws Exception;
    BigInteger getDeposit() throws Exception;
    String getTitle() throws Exception;
    String getDescription() throws Exception;
    String getBuyer() throws Exception;
    ContractState getState() throws Exception;
    Boolean getVerifyIdentity() throws Exception;
}
