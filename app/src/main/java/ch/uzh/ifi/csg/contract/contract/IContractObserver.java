package ch.uzh.ifi.csg.contract.contract;

public interface IContractObserver 
{
	void contractStateChanged(String event, Object value);
}
