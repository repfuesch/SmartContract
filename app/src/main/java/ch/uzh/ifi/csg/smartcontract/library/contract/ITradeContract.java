package ch.uzh.ifi.csg.smartcontract.library.contract;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.csg.smartcontract.library.async.promise.SimplePromise;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.UserProfile;

/**
 *  Interface that declares common methods for all contract implementations.
 *  It declares methods to access local contract information
 *  and remote accessors to access information that is stored on the smart contract on the
 *  blockchain.
 */

public interface ITradeContract extends IContractObservable
{
    //LOCAL FIELDS AND METHODS

    /**
     * Returns the address of the contract
     * @return
     */
    String getContractAddress();

    /**
     * Returns the UserProfile associated with the contract
     * @return
     */
    UserProfile getUserProfile();

    /**
     * Sets the UserProfile for this contract
     * @param profile
     */
    void setUserProfile(UserProfile profile);

    /**
     * Returns
     * @return
     */
    Map<String, String> getImages();

    /**
     * Returns the type of this contract
     * @return
     */
    ContractType getContractType();

    /**
     * Indicates that contract stores stores only the hash of its content attributes
     * on the blockchain.
     *
     * @return
     */
    boolean isLightContract();

    /**
     * Returns a JSON-representation of this contract. This method is used to serialize the
     * contract.
     *
     * @return
     */
    String toJson();


    //REMOTE ACCESSORS AND METHODS

    /**
     * Executes the "abort" function of the smart contract
     * @return
     */
    SimplePromise<String> abort();

    /**
     * Returns the image signatures from the smart contract
     *
     * @return
     */
    SimplePromise<List<String>> getImageSignatures();

    /**
     * Returns the address of the seller from the smart contract
     * @return
     */
    SimplePromise<String> getSeller();

    /**
     * Returns the price in wei from the smart contract
     * @return
     */
    SimplePromise<BigInteger> getPrice();

    /**
     * returns the deposit in wei from the smart contract
     *
     * @return
     */
    SimplePromise<BigInteger> getDeposit();

    /**
     * returns the title of the smart contract
     *
     * @return
     */
    SimplePromise<String> getTitle();

    /**
     * Returns the description of the smart contract
     *
     * @return
     */
    SimplePromise<String> getDescription();

    /**
     * Returns the address of the buyer from the smart contract
     * @return
     */
    SimplePromise<String> getBuyer();

    /**
     * Returns the current state of the smart contract
     * @return
     */
    SimplePromise<ContractState> getState();

    /**
     * Returns an indicator of whether to verify the identities of the parties
     * @return
     */
    SimplePromise<Boolean> getVerifyIdentity();

    /**
     * Returns the hash of the content attributes
     * @return
     */
    SimplePromise<String> getContentHash();

    /**
     * Verifies if the content hash of the local contract matches the hash stored on the blockchain
     * This method returns always 'true' when "isLightContract" returns 'false'
     * @return
     */
    SimplePromise<Boolean> verifyContent();
}
