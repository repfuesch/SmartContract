package ch.uzh.ifi.csg.contract.service.account;


import ezvcard.VCard;

/**
 * Created by flo on 31.03.17.
 */

public class UserProfile {

    private VCard vCard;

    public UserProfile() {

        this.vCard = new VCard();
    }

    public VCard getVCard() {
        return vCard;
    }

    public void setVCard(VCard vCard) {
        this.vCard = vCard;
    }
}
