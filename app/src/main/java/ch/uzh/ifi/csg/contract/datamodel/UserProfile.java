package ch.uzh.ifi.csg.contract.datamodel;


import ezvcard.VCard;

/**
 * Created by flo on 31.03.17.
 */

public class UserProfile {

    private VCard vCard;
    private String profileImagePath;
    private boolean isVerified;

    public UserProfile() {
    }

    public VCard getVCard() {
        return vCard;
    }

    public void setVCard(VCard vCard) {
        this.vCard = vCard;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }
}
