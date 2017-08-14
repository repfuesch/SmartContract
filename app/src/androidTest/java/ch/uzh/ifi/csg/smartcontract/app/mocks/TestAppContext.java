package ch.uzh.ifi.csg.smartcontract.app.mocks;

import android.app.Application;
import android.content.Context;

import ch.uzh.ifi.csg.smartcontract.app.common.ActivityBase;
import ch.uzh.ifi.csg.smartcontract.app.common.broadcast.BroadCastService;
import ch.uzh.ifi.csg.smartcontract.app.common.message.MessageService;
import ch.uzh.ifi.csg.smartcontract.app.common.permission.PermissionProvider;
import ch.uzh.ifi.csg.smartcontract.app.common.ApplicationContext;
import ch.uzh.ifi.csg.smartcontract.app.common.service.ServiceProvider;
import ch.uzh.ifi.csg.smartcontract.app.common.setting.SettingProvider;
import ch.uzh.ifi.csg.smartcontract.app.common.transaction.TransactionHandler;
import ch.uzh.ifi.csg.smartcontract.app.p2p.service.P2PBuyerService;
import ch.uzh.ifi.csg.smartcontract.app.p2p.service.P2PSellerService;

import static org.mockito.Mockito.mock;

/**
 * Android {@link Application} class that implements the {@link ApplicationContext} interface
 * to provide mocked service objects
 */
public class TestAppContext extends Application implements ApplicationContext
{
    private static ServiceProvider serviceProvider;
    public static SettingProvider SettingProvider;
    public static TransactionHandler TransactionHandler;
    public static P2PSellerService SellerService;
    public static P2PBuyerService BuyerService;
    public static PermissionProvider PermissionProvider;
    public static BroadCastService BroadCastService;
    public static MessageService MessageService;

    public void initMocks()
    {
        serviceProvider = new TestServiceProvider();
        SettingProvider = mock(SettingProvider.class);
        TransactionHandler = mock(TransactionHandler.class);
        SellerService = mock(P2PSellerService.class);
        BuyerService = mock(P2PBuyerService.class);
        PermissionProvider = mock(PermissionProvider.class);
        BroadCastService = mock(BroadCastService.class);
        MessageService = mock(MessageService.class);
    }

    @Override
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    @Override
    public SettingProvider getSettingProvider() { return SettingProvider; }

    @Override
    public TransactionHandler getTransactionManager() {
        return TransactionHandler;
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
