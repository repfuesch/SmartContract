package ch.uzh.ifi.csg.contract.service.contract;

import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.service.account.UserProfile;

/**
 * Created by flo on 06.03.17.
 */

public class ContractInfo
{
    private ContractState lastState;
    private String contractAddress;
    private UserProfile userProfile;

    public ContractInfo(ContractState lastState, String contractAddress) {
        this.lastState = lastState;
        this.contractAddress = contractAddress;
    }

    public ContractInfo(ContractState lastState, String contractAddress, UserProfile userProfile) {
        this(lastState, contractAddress);
        this.userProfile = userProfile;
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

    public UserProfile getUserProfile() {
        return userProfile;
    }
}
