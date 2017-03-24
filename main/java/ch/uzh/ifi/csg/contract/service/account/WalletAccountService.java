package ch.uzh.ifi.csg.contract.service.account;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 * Wallet implementation of the AccountService.
 * This implementation uses the WalletUtil of web3j to decrypt a local wallet file to obtain the
 * private key of an account. This is service is secure and should be used in a non-debug
 * environment.
 * Note: Decryption of wallet file is very slow and therefore, this service should not be used for
 * debugging purposes.
 */

public class WalletAccountService implements AccountService {

    private AccountManager accountManager;
    private String walletDirectory;
    private boolean useFullEncryption;
    private CredentialProvider credentialProvider;

    public WalletAccountService(AccountManager accountManager, CredentialProvider credentialProvider, String walletDirectory, boolean useFullEncryption)
    {
        this.accountManager = accountManager;
        this.walletDirectory = walletDirectory;
        this.useFullEncryption = useFullEncryption;
        this.credentialProvider = credentialProvider;
    }

    @Override
    public SimplePromise<List<Account>> getAccounts()
    {
        return Async.toPromise(new Callable<List<Account>>() {
            @Override
            public List<Account> call() throws Exception {
                return accountManager.getAccounts();
            }
        });
    }

    @Override
    public SimplePromise<Account> createAccount(final String alias, final String password) {

        return Async.toPromise(new Callable<Account>() {
            @Override
            public Account call() throws Exception {
                File walletDir = new File(walletDirectory);
                if(!walletDir.exists())
                {
                    if(!walletDir.createNewFile())
                        throw new IOException("Cannot create Wallet directory!");
                }

                String walletFile = WalletUtils.generateNewWalletFile(password, walletDir, useFullEncryption);
                String path = walletDir.getAbsolutePath();
                Credentials credentials = WalletUtils.loadCredentials(password, path + "/" + walletFile);
                Account newAccount = new Account(credentials.getAddress(), alias, walletFile);
                accountManager.getAccounts().add(newAccount);
                accountManager.save();

                return newAccount;
            }
        });
    }

    @Override
    public SimplePromise<Boolean> unlockAccount(final Account account, final String password)
    {
        return Async.toPromise(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try{
                    Credentials credentials = WalletUtils.loadCredentials(password, walletDirectory + "/" + account.getWalletFile());
                    credentialProvider.setCredentials(credentials);

                }catch(CipherException ex)
                {
                    return false;
                }

                return true;
            }
        });
    }
}
