package ch.uzh.ifi.csg.contract.service.contract;

import ch.uzh.ifi.csg.contract.contract.ContractState;

/**
 * Created by flo on 06.03.17.
 */

public class ContractInfo
{
    private ContractState lastState;
    private String contractAddress;

    public ContractInfo(ContractState lastState, String contractAddress) {
        this.lastState = lastState;
        this.contractAddress = contractAddress;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public ContractState getLastState() {
        return lastState;
    }

    public void setLastState(ContractState lastState) {
        this.lastState = lastState;
    }

}
