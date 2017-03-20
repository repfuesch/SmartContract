package ch.uzh.ifi.csg.contract.service;

import java.util.List;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 * Created by flo on 17.02.17.
 */

public interface AccountUnlockService {

    SimplePromise<List<String>> getAccounts();
    SimplePromise<Boolean> unlockAccount(String account, String password);

}
