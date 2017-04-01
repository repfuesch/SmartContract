package ch.uzh.ifi.csg.contract.service.account;


import net.glxn.qrgen.core.scheme.VCard;

/**
 * Created by flo on 31.03.17.
 */

public class AccountProfile {

    private VCard vCard;

    public AccountProfile() {

        this.vCard = new VCard();
    }

    public VCard getVCard() {
        return vCard;
    }

    public void setVCard(VCard vCard) {
        this.vCard = vCard;
    }
}
