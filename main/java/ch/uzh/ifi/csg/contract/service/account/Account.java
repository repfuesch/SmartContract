package ch.uzh.ifi.csg.contract.service.account;

/**
 * Class representing an ETH account
 */

public class Account
{
    private String id;
    private String label;
    private String walletFile;

    public Account(String id, String label, String walletFile) {
        this.id = id;
        this.label = label;
        this.walletFile = walletFile;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWalletFile() {
        return walletFile;
    }

    public void setWalletFile(String walletFile) {
        this.walletFile = walletFile;
    }
}
