package ch.uzh.ifi.csg.smartcontract.library.datamodel;

import android.util.Log;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.uzh.ifi.csg.smartcontract.library.contract.ContractType;
import ch.uzh.ifi.csg.smartcontract.library.util.BinaryUtil;

/**
 * Class used to represent a TradeContract for storage on the local filesystem
 *
 */
public class ContractInfo implements Serializable
{
    private static final long serialVersionUID = 7526471155622776147L;

    private ContractType contractType;
    private String contractAddress;
    private String title;
    private String description;
    private boolean verifyIdentity;
    private boolean isLightContract;
    private UserProfile userProfile;
    private Map<String, String> images;

    public ContractInfo(ContractType contractType, String contractAddress) {
        this.contractAddress = contractAddress;
        this.contractType = contractType;
        this.images = new LinkedHashMap<>();
        this.userProfile = new UserProfile();
    }

    public ContractInfo(ContractType contractType, String contractAddress, String title, String description, boolean verifyIdentity, boolean isLightContract) {
        this(contractType, contractAddress);

        this.title = title;
        this.description = description;
        this.verifyIdentity = verifyIdentity;
        this.isLightContract = isLightContract;
    }

    /**
     * Calculates and returns the SHA-256 hash of the title, description, verifyIdentity and image
     * attributes of this contract.
     *
     * @return the SHA-256 hash of the content attributes
     */
    public String getContentHash()
    {
        ArrayList<Byte> byteList = new ArrayList<>();
        byteList.addAll(BinaryUtil.toByteList(title.getBytes()));
        byteList.addAll(BinaryUtil.toByteList(description.getBytes()));
        byteList.add((byte) (verifyIdentity ? 1 : 0 ));
        for(String imageSig : images.keySet())
        {
            byteList.addAll(BinaryUtil.toByteList(imageSig.getBytes()));
        }

        MessageDigest digest=null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            Log.e("ContractInfo", "SHA-256 hash not found", e1);
        }

        digest.reset();
        String hex = BinaryUtil.bin2hex(digest.digest(BinaryUtil.toByteArray(byteList)));
        return hex;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public void setImages(Map<String, String> images) {
        this.images = images;
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

    public boolean isVerifyIdentity() {
        return verifyIdentity;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public boolean isLightContract() {
        return isLightContract;
    }
}
