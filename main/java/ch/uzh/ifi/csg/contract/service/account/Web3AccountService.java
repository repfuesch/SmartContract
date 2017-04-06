package ch.uzh.ifi.csg.contract.service.account;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.common.Web3;

/**
 * Created by flo on 30.03.17.
 */

public abstract class Web3AccountService implements AccountService
{
    private Web3j web3;
    private AccountManager accountManager;

    public Web3AccountService(Web3j web3, AccountManager accountManager)
    {
        this.web3 = web3;
        this.accountManager = accountManager;
    }

    /**
     * Returns the account balance in ether for the specified account
     *
     * @param account: the account id
     * @return the amount of ether for this account
     */
    @Override
    public SimplePromise<BigInteger> getAccountBalance(final String account) {
        return Async.toPromise(new Callable<BigInteger>() {
            @Override
            public BigInteger call() throws Exception {
                EthGetBalance balance = web3.ethGetBalance(account, DefaultBlockParameterName.LATEST).send();
                BigInteger balanceWei = balance.getBalance();
                return balanceWei;
            }
        });
    }

    @Override
    public UserProfile getAccountProfile(String accountId)
    {
        for(Account acc : accountManager.getAccounts())
        {
            if(acc.getId().equals(accountId))
                return acc.getProfile();
        }

        return new UserProfile();
    }

    @Override
    public void saveAccountProfile(String accountId, UserProfile profile)
    {
        for(Account acc : accountManager.getAccounts())
        {
            if(acc.getId().equals(accountId))
            {
                acc.setProfile(profile);
                accountManager.save();
            }
        }
    }
}
c