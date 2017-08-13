package ch.uzh.ifi.csg.smartcontract.library.service.account;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.math.BigInteger;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.smartcontract.library.async.Async;
import ch.uzh.ifi.csg.smartcontract.library.async.promise.SimplePromise;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.UserProfile;

/**
 * Base class for all {@link AccountService} implementations. It uses the {@link Web3j} client
 * to obtain the account balance for an account and the {@link AccountManager} to set and retrieve
 * the profile of an account.
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

    @Override
    public SimplePromise<BigInteger> getAccountBalance(final String account)
    {
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
        UserProfile profile = accountManager.getAccount(accountId).getProfile();
        if (profile == null)
        {
            return new UserProfile();
        }


        return profile;
    }

    @Override
    public void saveAccountProfile(String accountId, UserProfile profile)
    {
        accountManager.saveAccountProfile(accountId, profile);
    }
}
