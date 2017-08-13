package ch.uzh.ifi.csg.smartcontract.library.service.account;


import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.methods.response.NewAccountIdentifier;
import org.web3j.protocol.parity.methods.response.PersonalListAccounts;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import ch.uzh.ifi.csg.smartcontract.library.async.Async;
import ch.uzh.ifi.csg.smartcontract.library.async.promise.SimplePromise;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.Account;

/**
 * Parity implementation of the AccountService. To use this service, the personal interface has to
 * be exposed over the RPC-HTTP API on Ethereum client. This is inherently unsafe. This service should only
 * be used for debugging purposes.
 */
public class ParityAccountService extends Web3AccountService{

    private Parity parity;
    private AccountManager accountManager;

    public ParityAccountService(Parity parity, AccountManager accountManager)
    {
        super(parity, accountManager);
        this.parity = parity;
        this.accountManager = accountManager;
    }

    @Override
    public SimplePromise<List<Account>> getAccounts() {

        return Async.toPromise(new Callable<List<Account>>()
        {
            @Override
            public List<Account> call() throws Exception {

                //retrieve the accounts on the data directory of the Ethereum client
                PersonalListAccounts listAccounts = parity.personalListAccounts().send();

                List<String> remoteAccounts = listAccounts.getAccountIds();
                List<Account> accountList = new ArrayList<Account>();
                List<Account> newAccounts = new ArrayList<Account>();
                for(String accId : remoteAccounts)
                {
                    boolean persisted = false;
                    for(Account account : accountManager.getAccounts())
                    {
                        if(account.getId().equals(accId))
                        {
                            persisted = true;
                            break;
                        }
                    }

                    Account account = new Account(accId, "alias", "");
                    if(!persisted)
                    {
                        newAccounts.add(account);
                    }

                    accountList.add(account);
                }

                for(Account acc : newAccounts)
                    accountManager.addAccount(acc);

                return accountList;
            }
        });
    }

    @Override
    public SimplePromise<Account> createAccount(final String alias, final String password) {
        return Async.toPromise(new Callable<Account>() {
            @Override
            public Account call() throws Exception {
                //create account on the remote Ethereum client
                NewAccountIdentifier accountId = parity.personalNewAccount(password).send();
                Account newAccount =  new Account(accountId.getAccountId(), alias, "");
                accountManager.addAccount(newAccount);
                return newAccount;
            }
        });
    }

    @Override
    public SimplePromise<Account> importAccount(String alias, String password, String walletFile) {
        return createAccount(alias, password);
    }

    @Override
    public SimplePromise<Boolean> unlockAccount(final Account account, final String password) {

        return Async.toPromise(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                //unlocks the account on the Ethereum client
                PersonalUnlockAccount unlockAcc = parity.personalUnlockAccount(account.getId(), password, BigInteger.valueOf(Integer.MAX_VALUE)).send();
                if(unlockAcc.hasError())
                    return false;

                return unlockAcc.accountUnlocked();
            }
        });
    }

    /***
     * Cannot be implemented because there does not exist a method to lock an unlocked account on
     * the parity interface!
     */
    @Override
    public void lockAccount() {
    }

}
