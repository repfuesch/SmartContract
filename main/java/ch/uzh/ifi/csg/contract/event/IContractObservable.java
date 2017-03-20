package ch.uzh.ifi.csg.contract.event;

public interface IContractObservable 
{
	void addObserver(IContractObserver observer);
	void deleteObserver(IContractObserver observer);
}
