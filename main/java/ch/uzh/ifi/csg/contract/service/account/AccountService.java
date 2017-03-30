package ch.uzh.ifi.csg.contract.service.account;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 * Created by flo on 17.02.17.
 */

public interface AccountService {

    SimplePromise<List<Account>> getAccounts();
    SimplePromise<Account> createAccount(String alias, String password);
    SimplePromise<Boolean> unlockAccount(Account account, String password);
    SimplePromise<BigDecimal> getAccountBalance(String account);

}
