package ch.uzh.ifi.csg.contract.service.account;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.parity.Parity;

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

    public Web3AccountService(Web3j web3)
    {
        this.web3 = web3;
    }

    /**
     * Returns the account balance in ether for the specified account
     *
     * @param account: the account id
     * @return the amount of ether for this account
     */
    @Override
    public SimplePromise<BigDecimal> getAccountBalance(final String account) {
        return Async.toPromise(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() throws Exception {
                EthGetBalance balance = web3.ethGetBalance(account, DefaultBlockParameterName.LATEST).send();
                BigInteger balanceWei = balance.getBalance();
                return Web3.toEther(balanceWei);
            }
        });
    }
}
