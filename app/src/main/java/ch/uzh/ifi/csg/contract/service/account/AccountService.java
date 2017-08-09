package ch.uzh.ifi.csg.contract.service.account;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.datamodel.Account;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;

/**
 * Service interface to manage Ethereum accounts
 */
public interface AccountService {

    /**
     * Returns a list of available accounts
     * @return a list of accounts
     */
    SimplePromise<List<Account>> getAccounts();

    /**
     * Creates a new Ethereum account for the provided alias and password
     *
     * @param alias: the name of the account
     * @param password: the password for the wallet file of the account
     * @return the created account
     */
    SimplePromise<Account> createAccount(String alias, String password);

    /**
     * Import an existing Ethereum account by its walletFile and creates a new Account with the
     * provided alias.
     *
     * @param alias: the name that should be used for the account
     * @param password: the password belonging to the referenced wallet file
     * @param walletFile: path to the wallet file on the external storage of the device
     *
     * @return the created account
     */
    SimplePromise<Account> importAccount(final String alias, final String password, final String walletFile);

    /**
     * Unlocks an account by its password.
     *
     * @param account: the account to unlock
     * @param password: the password of the account
     * @return 'true' if the password matches or 'false' otherwise
     */
    SimplePromise<Boolean> unlockAccount(Account account, String password);

    /**
     * Locks the currently unlocked Ethereum account.
     */
    void lockAccount();

    /**
     * retuns the account balance for the referenced account.
     *
     * @param accountId: id of the referenced account
     * @return the account balance for the account
     */
    SimplePromise<BigInteger> getAccountBalance(String accountId);

    /**
     * Returns the UserProfile for the referenced account or null if no such account exists.
     *
     * @param accountId: the referenced account
     * @return the UserProfile for the refernced account
     */
    UserProfile getAccountProfile(String accountId);

    /**
     * Saves a UserProfile for the referenced account.
     *
     * @param accountId: the referenced account
     * @param profile: the UserProfile to store
     */
    void saveAccountProfile(String accountId, UserProfile profile);
}
