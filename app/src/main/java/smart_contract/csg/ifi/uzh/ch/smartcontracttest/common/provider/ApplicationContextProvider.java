package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionManager;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.P2PBuyerService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.P2PSellerService;

/**
 * Created by flo on 27.04.17.
 */

public interface ApplicationContextProvider
{
    ServiceProvider getServiceProvider();
    SettingProvider getSettingProvider();
    TransactionManager getTransactionManager();
    P2PSellerService getP2PSellerService();
    P2PBuyerService getP2PBuyerService();
}
