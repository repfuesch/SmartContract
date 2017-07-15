package smart_contract.csg.ifi.uzh.ch.smartcontracttest.mocks;

import android.app.Application;
import android.content.Context;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.broadcast.BroadCastService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.message.MessageService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.permission.PermissionProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContext;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.setting.SettingProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionManager;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.P2PBuyerService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.P2PSellerService;

import static org.mockito.Mockito.mock;

/**
 * Created by flo on 13.07.17.
 */

public class TestAppContext extends Application implements ApplicationContext
{
    private static ServiceProvider serviceProvider = new TestServiceProvider();
    public static SettingProvider SettingProvider = mock(SettingProvider.class);
    public static TransactionManager TransactionManager = mock(TransactionManager.class);
    public static P2PSellerService SellerService = mock(P2PSellerService.class);
    public static P2PBuyerService BuyerService = mock(P2PBuyerService.class);
    public static PermissionProvider PermissionProvider = mock(PermissionProvider.class);
    public static BroadCastService BroadCastService = mock(BroadCastService.class);
    public static MessageService MessageService = mock(MessageService.class);

    @Override
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    @Override
    public SettingProvider getSettingProvider() { return SettingProvider; }

    @Override
    public TransactionManager getTransactionManager() {
        return TransactionManager;
    }

    @Override
    public P2PSellerService getP2PSellerService() {
        return SellerService;
    }

    @Override
    public P2PBuyerService getP2PBuyerService() {
        return BuyerService;
    }

    @Override
    public PermissionProvider getPermissionProvider() {
        return PermissionProvider;
    }

    @Override
    public BroadCastService getBroadCastService() {
        return BroadCastService;
    }

    @Override
    public MessageService getMessageService() { return MessageService; }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onActivityResumed(ActivityBase activity) {
    }

    @Override
    public void onActivityStopped(ActivityBase activity) {
    }
}
