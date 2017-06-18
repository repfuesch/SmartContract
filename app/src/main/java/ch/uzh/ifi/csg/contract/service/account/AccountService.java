package ch.uzh.ifi.csg.contract.service.account;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.datamodel.Account;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;

/**
 * Created by flo on 17.02.17.
 */

public interface AccountService {

    SimplePromise<List<Account>> getAccounts();
    SimplePromise<Account> createAccount(String alias, String password);
    SimplePromise<Boolean> unlockAccount(Account account, String password);
    SimplePromise<BigInteger> getAccountBalance(String account);
    UserProfile getAccountProfile(String account);
    void saveAccountProfile(String accountId, UserProfile profile);

}
