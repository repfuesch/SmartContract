package ch.uzh.ifi.csg.contract.service.account;

import java.util.List;

import ch.uzh.ifi.csg.contract.datamodel.Account;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;

/**
 * Created by flo on 24.03.17.
 */

public interface AccountManager
{
    List<Account> getAccounts();
    void addAccount(Account account);
    Account getAccount(String accountId);
    void saveAccountProfile(String accountId, UserProfile profile);
}
