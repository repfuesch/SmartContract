package ch.uzh.ifi.csg.smartcontract.app.common;

import android.content.Context;
import ch.uzh.ifi.csg.smartcontract.app.common.ActivityChangedListener;
import ch.uzh.ifi.csg.smartcontract.app.common.AppContext;
import ch.uzh.ifi.csg.smartcontract.app.common.broadcast.BroadCastService;
import ch.uzh.ifi.csg.smartcontract.app.common.message.MessageService;
import ch.uzh.ifi.csg.smartcontract.app.common.permission.PermissionProvider;
import ch.uzh.ifi.csg.smartcontract.app.common.service.ServiceProvider;
import ch.uzh.ifi.csg.smartcontract.app.common.setting.SettingProvider;
import ch.uzh.ifi.csg.smartcontract.app.common.transaction.TransactionHandler;
import ch.uzh.ifi.csg.smartcontract.app.p2p.service.P2PBuyerService;
import ch.uzh.ifi.csg.smartcontract.app.p2p.service.P2PSellerService;

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
