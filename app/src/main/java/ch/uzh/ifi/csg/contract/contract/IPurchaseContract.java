package ch.uzh.ifi.csg.contract.contract;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

public interface IPurchaseContract extends ITradeContract {

    SimplePromise<String> confirmReceived();
    SimplePromise<String> confirmPurchase();
}
