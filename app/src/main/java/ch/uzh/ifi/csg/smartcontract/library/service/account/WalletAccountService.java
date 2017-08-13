package ch.uzh.ifi.csg.smartcontract.library.service.account;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;

import java.util.List;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.smartcontract.library.async.Async;
import ch.uzh.ifi.csg.smartcontract.library.async.promise.SimplePromise;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.Account;

/**
 * Wallet implementation of the AccountService. It manages local account with the
 * {@link AccountManager}.
 * It uses the {@link WalletUtils} of web3j to decrypt a local wallet file to obtain the
 * {@link Credentials} for an account and stores them in the {@link CredentialProvider}.
 * This service is secure and should be used in a non-debug environment.
 * Note: Decryption of the wallet file is very slow and therefore, this service should not be used for
 * debugging purposes.
 */
public class WalletAccountService extends Web3AccountService
{
    private AccountManager accountManager;
    private String walletDirectory;
    private boolean useFullEncryption;
    private CredentialProvider credentialProvider;
    private WalletWrapper walletWrapper;

    public WalletAccountService(Web3j web3, AccountManager accountManager, CredentialProvider credentialProvider, String walletDirectory, boolean useFullEncryption, WalletWrapper walletWrapper)
    {
        super(web3, accountManager);
        this.accountManager = accountManager;
        this.walletDirectory = walletDirectory;
        this.useFullEncryption = useFullEncryption;
        this.credentialProvider = credentialProvider;
        this.walletWrapper = walletWrapper;
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
                String walletFile = walletWrapper.generateNewWalletFile(password, walletDirectory, useFullEncryption);
                Credentials credentials = walletWrapper.loadCredentials(password, walletDirectory + "/" + walletFile);
                Account newAccount = new Account(credentials.getAddress(), alias, walletFile);
                accountManager.addAccount(newAccount);
                credentialProvider.setCredentials(credentials);
                return newAccount;
            }
        });
    }

    @Override
    public SimplePromise<Account> importAccount(final String alias, final String password, final String walletFile) {
        return Async.toPromise(new Callable<Account>() {
            @Override
            public Account call() throws Exception {
                Credentials credentials = walletWrapper.loadCredentials(password, walletDirectory + "/" + walletFile);
                Account newAccount = new Account(credentials.getAddress(), alias, walletFile);
                accountManager.addAccount(newAccount);
                credentialProvider.setCredentials(credentials);
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
                    Credentials credentials = walletWrapper.loadCredentials(password, walletDirectory + "/" + account.getWalletFile());
                    credentialProvider.setCredentials(credentials);

                }catch(CipherException ex)
                {
                    return false;
                }

                return true;
            }
        });
    }

    @Override
    public void lockAccount() {
        credentialProvider.setCredentials(null);
    }
}
