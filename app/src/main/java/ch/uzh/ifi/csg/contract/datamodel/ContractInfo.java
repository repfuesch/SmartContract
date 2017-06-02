package ch.uzh.ifi.csg.contract.datamodel;

import java.util.Map;

import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;

/**
 * Created by flo on 06.03.17.
 */

public class ContractInfo
{
    private ContractState lastState;
    private String contractAddress;
    private UserProfile userProfile;
    private Map<String, String> images;

    public ContractInfo(ContractState lastState, String contractAddress) {
        this.lastState = lastState;
        this.contractAddress = contractAddress;
    }

    public ContractInfo(ContractState lastState, String contractAddress, UserProfile userProfile, Map<String, String> imageMap) {
        this(lastState, contractAddress);
        this.userProfile = userProfile;
        this.images = imageMap;
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

    public Map<String, String> getImages() {
        return images;
    }
}
