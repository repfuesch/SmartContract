package ch.uzh.ifi.csg.contract.contract;

public interface IContractObservable 
{
	void addObserver(IContractObserver observer);
	void removeObserver(IContractObserver observer);
}
