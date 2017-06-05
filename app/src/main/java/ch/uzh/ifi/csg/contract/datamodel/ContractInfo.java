package ch.uzh.ifi.csg.contract.datamodel;

import java.util.Map;

import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;

/**
 * Class used to represent a TradeContract for storage on the local filesystem
 */

public class ContractInfo
{
    private ContractType contractType;
    private ContractState lastState;
    private String contractAddress;
    private UserProfile userProfile;
    private Map<String, String> images;

    public ContractInfo(ContractType contractType, ContractState lastState, String contractAddress) {
        this.lastState = lastState;
        this.contractAddress = contractAddress;
        this.contractType = contractType;
    }

    public ContractInfo(ContractType contractType, ContractState lastState, String contractAddress, UserProfile userProfile, Map<String, String> imageMap) {
        this(contractType, lastState, contractAddress);
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

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public Map<String, String> getImages() {
        return images;
    }

    public ContractType getContractType() {
        return contractType;
    }
}
