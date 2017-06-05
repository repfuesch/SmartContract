package ch.uzh.ifi.csg.contract.contract;

import java.math.BigInteger;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 * Created by flo on 03.06.17.
 */

public interface IRentContract extends ITradeContract {

    SimplePromise<String> returnItem();
    SimplePromise<String> reclaimItem();
    SimplePromise<String> rentItem();
    SimplePromise<BigInteger> getRentingFee();
    SimplePromise<TimeUnit> getTimeUnit();
}
