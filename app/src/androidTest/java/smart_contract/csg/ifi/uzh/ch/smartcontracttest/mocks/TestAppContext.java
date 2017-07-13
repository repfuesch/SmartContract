package smart_contract.csg.ifi.uzh.ch.smartcontracttest.mocks;

import android.app.Application;
import android.content.Context;
import android.os.Message;

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
    private static SettingProvider settingProvider = mock(SettingProvider.class);
    private static TransactionManager transactionManager = mock(TransactionManager.class);
    private static P2PSellerService sellerService = mock(P2PSellerService.class);
    private static P2PBuyerService buyerService = mock(P2PBuyerService.class);
    private static PermissionProvider permissionProvider = mock(PermissionProvider.class);
    private static BroadCastService broadCastService = mock(BroadCastService.class);
    private static MessageService messageService = mock(MessageService.class);

    @Override
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    @Override
    public SettingProvider getSettingProvider() { return settingProvider; }

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public P2PSellerService getP2PSellerService() {
        return sellerService;
    }

    @Override
    public P2PBuyerService getP2PBuyerService() {
        return buyerService;
    }

    @Override
    public PermissionProvider getPermissionProvider() {
        return permissionProvider;
    }

    @Override
    public BroadCastService getBroadCastService() {
        return broadCastService;
    }

    @Override
    public MessageService getMessageService() { return messageService; }

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
