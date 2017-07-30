package ch.uzh.ifi.csg.contract.contract;

import java.math.BigInteger;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 * Interface that declares transaction methods and accessors for all rent contract implementations
 */

public interface IRentContract extends ITradeContract {

    //transaction methods
    SimplePromise<String> returnItem();
    SimplePromise<String> reclaimItem();
    SimplePromise<String> rentItem();

    //accessors for remote fields
    SimplePromise<BigInteger> getRentingFee();
}
