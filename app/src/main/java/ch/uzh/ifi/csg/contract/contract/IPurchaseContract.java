package ch.uzh.ifi.csg.contract.contract;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 * Interface that declares transaction methods used for all Purchase contract implementations
 */
public interface IPurchaseContract extends ITradeContract {

    SimplePromise<String> confirmReceived();
    SimplePromise<String> confirmPurchase();
}
