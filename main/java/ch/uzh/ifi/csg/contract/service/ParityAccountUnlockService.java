package ch.uzh.ifi.csg.contract.service;


import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.ParityFactory;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.setting.EthSettings;
import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 * Created by flo on 17.02.17.
 */

public class ParityAccountUnlockService implements AccountUnlockService {

    private Parity parity;

    public ParityAccountUnlockService(Parity parity)
    {
        this.parity = parity;
    }

    @Override
    public SimplePromise<List<String>> getAccounts() {

        return Async.toPromise(new Callable<List<String>>()
        {
            @Override
            public List<String> call() throws Exception {
                EthAccounts accounts = parity.ethAccounts().send();
                return accounts.getAccounts();
            }
        });
    }

    @Override
    public SimplePromise<Boolean> unlockAccount(final String account, final String password) {

        return Async.toPromise(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                PersonalUnlockAccount unlockAcc = parity.personalUnlockAccount(account, password, BigInteger.valueOf(Integer.MAX_VALUE)).send();
                return unlockAcc.accountUnlocked();
            }
        });
    }
}
