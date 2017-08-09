package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider;

import android.content.Context;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityChangedListener;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.AppContext;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.broadcast.BroadCastService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.message.MessageService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.permission.PermissionProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.setting.SettingProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.P2PBuyerService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.P2PSellerService;

/**
 * Interface that provides globally accessible objects.
 *
 * see {@link AppContext}
 */
public interface ApplicationContext extends ActivityChangedListener
{
    ServiceProvider getServiceProvider();
    SettingProvider getSettingProvider();
    TransactionHandler getTransactionManager();
    P2PSellerService getP2PSellerService();
    P2PBuyerService getP2PBuyerService();
    PermissionProvider getPermissionProvider();
    BroadCastService getBroadCastService();
    MessageService getMessageService();
    Context getContext();
}
