package smart_contract.csg.ifi.uzh.ch.smartcontract.common.provider;

import android.content.Context;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.ActivityChangedListener;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.AppContext;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.broadcast.BroadCastService;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.message.MessageService;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.permission.PermissionProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.setting.SettingProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.transaction.TransactionHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontract.p2p.service.P2PBuyerService;
import smart_contract.csg.ifi.uzh.ch.smartcontract.p2p.service.P2PSellerService;

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
