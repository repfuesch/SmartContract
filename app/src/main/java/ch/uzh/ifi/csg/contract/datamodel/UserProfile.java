package ch.uzh.ifi.csg.contract.datamodel;


import java.math.BigInteger;

import ezvcard.VCard;

/**
 * Created by flo on 31.03.17.
 */

public class UserProfile {

    private VCard vCard;
    private String profileImagePath;

    public UserProfile() {
    }

    public VCard getVCard() {
        return vCard;
    }

    public void setVCard(VCard vCard) {
        this.vCard = vCard;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }
}
