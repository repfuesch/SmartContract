package ch.uzh.ifi.csg.smartcontract.library.service.account;

import java.util.List;

import ch.uzh.ifi.csg.smartcontract.library.datamodel.Account;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.UserProfile;

/**
 * Interface to manage accounts on the local file system
 */
public interface AccountManager
{
    /**
     * Returns a list of all accounts stored on the local file system
     * @return a list of accounts
     */
    List<Account> getAccounts();

    /**
     * Adds an account
     *
     * @param account
     */
    void addAccount(Account account);

    /**
     * Returns an account belonging to the provided accountId or null if no such account exists
     *
     * @param accountId
     * @return the account or null
     */
    Account getAccount(String accountId);

    /**
     * Saves the profile for the account referenced by the provided accountId
     *
     * @param accountId
     * @param profile
     */
    void saveAccountProfile(String accountId, UserProfile profile);
}
