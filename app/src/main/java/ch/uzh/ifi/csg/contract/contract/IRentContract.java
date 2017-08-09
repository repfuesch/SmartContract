package ch.uzh.ifi.csg.contract.contract;

import java.math.BigInteger;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 * Interface that declares transaction methods and accessors for all rent contract implementations
 */

public interface IRentContract extends ITradeContract {


    /**
     * Executes the "returnItem" transaction function of the smart contract
     * @return
     */
    SimplePromise<String> returnItem();
    /**
     * Executes the "reclaimItem" transaction function of the smart contract
     * @return
     */
    SimplePromise<String> reclaimItem();
    /**
     * Executes the "rentItem" transaction function of the smart contract
     * @return
     */
    SimplePromise<String> rentItem();



    /**
     * Returns the current renting fee for the item
     *
     * @return
     */
    SimplePromise<BigInteger> getRentingFee();
}
