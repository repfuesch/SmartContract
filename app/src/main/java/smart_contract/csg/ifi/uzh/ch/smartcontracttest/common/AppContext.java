package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.broadcast.BroadCastService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.broadcast.LocalBroadcastService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.message.MessageService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.message.MessageServiceImpl;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.permission.PermissionProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.permission.PermissionProviderImpl;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContext;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.setting.SettingProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.setting.SettingProviderImpl;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionManager;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionManagerImpl;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.EthServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.connection.WifiConnectionManager;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.P2PBuyerService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.P2PBuyerServiceImpl;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.P2PSellerService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.P2PSellerServiceImpl;

/**
 * Custom application class for holding and accessing the EthServiceProvider and SettingProviderImpl instances
 */

public class AppContext extends Application implements ApplicationContext
{
    private SettingProviderImpl settingsProvider;
    private EthServiceProvider ethServiceProvider;
    private TransactionManagerImpl transactionManager;
    private BroadcastReceiver broadcastReceiver;
    private P2PSellerServiceImpl p2PSellerService;
    private P2PBuyerServiceImpl p2PBuyerService;
    private WifiConnectionManager wifiManager;
    private PermissionProviderImpl permissionProvider;
    private BroadCastService broadCastService;
    private MessageServiceImpl messageService;
    private List<ActivityChangedListener> activityChangedListeners;

    @Override
    public void onCreate() {
        super.onCreate();

        activityChangedListeners = new ArrayList<>();
        broadcastReceiver = new BroadCastReceiver();

        messageService = new MessageServiceImpl();
        permissionProvider = PermissionProviderImpl.create();
        broadCastService = LocalBroadcastService.create(this);
        transactionManager = TransactionManagerImpl.create(broadCastService, messageService);
        broadCastService.registerReceiver(broadcastReceiver, new IntentFilter(SettingProviderImpl.ACTION_SETTINGS_CHANGED));
        settingsProvider = SettingProviderImpl.create(this);
        ethServiceProvider = EthServiceProvider.create(this);
        ethServiceProvider.initServices(settingsProvider);

        WifiP2pManager p2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel p2pChannel = p2pManager.initialize(this, getMainLooper(), null);
        wifiManager = WifiConnectionManager.create(p2pManager, p2pChannel);

        p2PBuyerService = new P2PBuyerServiceImpl(wifiManager);
        p2PSellerService = new P2PSellerServiceImpl(wifiManager);

        activityChangedListeners.add(wifiManager);
        activityChangedListeners.add(permissionProvider);
        activityChangedListeners.add(messageService);
    }

    @Override
    public SettingProvider getSettingProvider()
    {
        return settingsProvider;
    }

    @Override
    public ServiceProvider getServiceProvider()
    {
        return ethServiceProvider;
    }

    @Override
    public TransactionManager getTransactionManager()
    {
        return transactionManager;
    }

    @Override
    public P2PSellerService getP2PSellerService() {
        return p2PSellerService;
    }

    @Override
    public P2PBuyerService getP2PBuyerService() {
        return p2PBuyerService;
    }

    @Override
    public PermissionProvider getPermissionProvider() {
        return permissionProvider;
    }

    @Override
    public BroadCastService getBroadCastService() { return broadCastService; }

    @Override
    public MessageService getMessageService() {
        return null;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onActivityResumed(ActivityBase activity) {
        for(ActivityChangedListener listener : activityChangedListeners)
        {
            listener.onActivityResumed(activity);
        }
    }

    @Override
    public void onActivityStopped(ActivityBase activity) {
        for(ActivityChangedListener listener : activityChangedListeners)
        {
            listener.onActivityStopped(activity);
        }
    }

    private class BroadCastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getAction().equals(SettingProviderImpl.ACTION_SETTINGS_CHANGED))
            {
                ethServiceProvider.initServices(settingsProvider);
            }
        }
    }
}

