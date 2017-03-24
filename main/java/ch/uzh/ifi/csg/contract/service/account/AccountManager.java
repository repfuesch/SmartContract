package ch.uzh.ifi.csg.contract.service.account;

import java.util.List;

/**
 * Created by flo on 24.03.17.
 */

public interface AccountManager
{
    List<Account> getAccounts();
    void save();
}
