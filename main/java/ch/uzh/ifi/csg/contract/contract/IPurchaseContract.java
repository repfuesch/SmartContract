package ch.uzh.ifi.csg.contract.contract;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.event.IContractObservable;

public interface IPurchaseContract extends IContractObservable {
	
	String getContractAddress();
    SimplePromise<String> seller();
    SimplePromise<String> abort();
    SimplePromise<Integer> value();
    SimplePromise<String> title();
    SimplePromise<String> description();
    SimplePromise<String> buyer();
    SimplePromise<String> confirmReceived();
    SimplePromise<ContractState> state();
    SimplePromise<String> confirmPurchase();
	
}
