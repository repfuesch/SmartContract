package ch.uzh.ifi.csg.smartcontract.library.contract;

import ch.uzh.ifi.csg.smartcontract.library.async.promise.SimplePromise;

/**
 * Interface that declares transaction methods for purchase contract implementations
 */
public interface IPurchaseContract extends ITradeContract
{
    /**
     * Executes the "confirmReceive" transaction function on a smart contract
     *
     * @return
     */
    SimplePromise<String> confirmReceived();

    /**
     *Executes the "confirmPurchase" transaction function of a smart contract
     *
     * @return
     */
    SimplePromise<String> confirmPurchase();
}
