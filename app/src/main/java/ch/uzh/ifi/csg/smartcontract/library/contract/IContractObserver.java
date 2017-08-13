package ch.uzh.ifi.csg.smartcontract.library.contract;

/**
 * Interface that must be implemented by classes that wants to receive updates from a contract.
 *
 * see {@link IContractObservable}
 * see {@link TradeContract#addObserver(IContractObserver)}
 */
public interface IContractObserver 
{
	void contractStateChanged(String event, Object value);
}
