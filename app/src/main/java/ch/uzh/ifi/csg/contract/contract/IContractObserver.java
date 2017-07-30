package ch.uzh.ifi.csg.contract.contract;

/**
 * Interface that must be implemented by classes that receive updates for a specific contract
 */
public interface IContractObserver 
{
	void contractStateChanged(String event, Object value);
}
