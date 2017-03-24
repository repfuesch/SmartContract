package ch.uzh.ifi.csg.contract.service.account;


import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.methods.response.NewAccountIdentifier;
import org.web3j.protocol.parity.methods.response.PersonalListAccounts;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 * Parity implementation of the AccountService. To use this service, the personal interface has to
 * be exposed over http on the host eth client. This is inherently unsafe. This service should only
 * be used for debugging purposes.
 */

public class ParityAccountService implements AccountService {

    private Parity parity;

    public ParityAccountService(Parity parity)
    {
        this.parity = parity;
    }

    @Override
    public SimplePromise<List<Account>> getAccounts() {

        return Async.toPromise(new Callable<List<Account>>()
        {
            @Override
            public List<Account> call() throws Exception {
                PersonalListAccounts listAccounts = parity.personalListAccounts().send();
                List<String> remoteAccounts = listAccounts.getAccountIds();
                List<Account> accountList = new ArrayList<Account>();
                for(String accId : remoteAccounts)
                {
                    accountList.add(new Account(accId, "alias", ""));
                }
                return accountList;
            }
        });
    }

    @Override
    public SimplePromise<Account> createAccount(final String alias, final String password) {
        return Async.toPromise(new Callable<Account>() {
            @Override
            public Account call() throws Exception {
                NewAccountIdentifier accountId = parity.personalNewAccount(password).send();
                return new Account(accountId.getAccountId(), alias, "");
            }
        });
    }

    @Override
    public SimplePromise<Boolean> unlockAccount(final Account account, final String password) {

        return Async.toPromise(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                PersonalUnlockAccount unlockAcc = parity.personalUnlockAccount(account.getId(), password, BigInteger.valueOf(Integer.MAX_VALUE)).send();
                return unlockAcc.accountUnlocked();
            }
        });
    }
}
