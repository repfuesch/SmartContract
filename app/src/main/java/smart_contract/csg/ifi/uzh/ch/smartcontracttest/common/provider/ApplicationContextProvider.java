package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionManager;

/**
 * Created by flo on 27.04.17.
 */

public interface ApplicationContextProvider {

    ServiceProvider getServiceProvider();
    SettingProvider getSettingProvider();
    TransactionManager getTransactionManager();
}