package ch.uzh.ifi.csg.smartcontract.library.service.account;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;

/**
 * Wrapper class that encapsulates the WalletUtil functionality.
 */
public class WalletWrapper {

    public String generateNewWalletFile(String password, String walletDirectory, boolean useFullEncryption) throws Exception
    {
        File walletDir = new File(walletDirectory);
        if(!walletDir.exists())
        {
            if(!walletDir.mkdirs())
                throw new IOException("Cannot create wallet directory!");
        }

        if(useFullEncryption)
        {
            return WalletUtils.generateFullNewWalletFile(password, walletDir);
        }else{
            return WalletUtils.generateLightNewWalletFile(password, walletDir);
        }
    }

    public Credentials loadCredentials(String password, String walletPath) throws IOException, CipherException
    {
        return WalletUtils.loadCredentials(password, walletPath);
    }
}
