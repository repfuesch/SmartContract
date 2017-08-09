package ch.uzh.ifi.csg.contract.contract;

/**
 * Interface to subscribe and unsubscribe observers to events of a contract
 */
public interface IContractObservable 
{
    /**
     * Adds a contract observer
     *
     * @param observer
     */
	void addObserver(IContractObserver observer);

    /**
     * Removed a contract observer
     *
     * @param observer
     */
	void removeObserver(IContractObserver observer);
}
