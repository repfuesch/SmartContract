package ch.uzh.ifi.csg.contract.contract;

/**
 * Interface implemented by all TradeContract implementations that contain events.
 * It declares methods to add and remove an IContractObserver to/from a contract
 */
public interface IContractObservable 
{
	void addObserver(IContractObserver observer);
	void removeObserver(IContractObserver observer);
}
