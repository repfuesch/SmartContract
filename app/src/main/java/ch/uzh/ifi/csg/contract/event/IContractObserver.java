package ch.uzh.ifi.csg.contract.event;

public interface IContractObserver 
{
	void contractStateChanged(String event, Object value);
}
