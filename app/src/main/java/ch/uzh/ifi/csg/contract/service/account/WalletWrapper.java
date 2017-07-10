package ch.uzh.ifi.csg.contract.service.account;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;

/**
 * Wrapper class for WalletUtil functionality
 */

public class WalletWrapper {

    public String generateNewWalletFile(String password, String walletDirectory, boolean useFullEncryption) throws Exception
    {
        File walletDir = new File(walletDirectory);
        if(!walletDir.exists())
        {
            if(!walletDir.createNewFile())
                throw new IOException("Cannot create wallet directory!");
        }

        return WalletUtils.generateNewWalletFile(password, walletDir, useFullEncryption);
    }

    public Credentials loadCredentials(String password, String walletPath) throws Exception
    {
        return WalletUtils.loadCredentials(password, walletPath);
    }
}
