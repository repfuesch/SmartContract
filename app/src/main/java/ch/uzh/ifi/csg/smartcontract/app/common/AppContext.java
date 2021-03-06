package ch.uzh.ifi.csg.smartcontract.app.common;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.csg.smartcontract.app.common.broadcast.BroadCastService;
import ch.uzh.ifi.csg.smartcontract.app.common.broadcast.LocalBroadcastService;
import ch.uzh.ifi.csg.smartcontract.app.common.message.MessageService;
import ch.uzh.ifi.csg.smartcontract.app.common.message.MessageServiceImpl;
import ch.uzh.ifi.csg.smartcontract.app.common.permission.PermissionProvider;
import ch.uzh.ifi.csg.smartcontract.app.common.permission.PermissionProviderImpl;
import ch.uzh.ifi.csg.smartcontract.app.common.service.ServiceProvider;
import ch.uzh.ifi.csg.smartcontract.app.common.setting.SettingProvider;
import ch.uzh.ifi.csg.smartcontract.app.common.setting.SettingProviderImpl;
import ch.uzh.ifi.csg.smartcontract.app.common.transaction.TransactionHandler;
import ch.uzh.ifi.csg.smartcontract.app.common.transaction.TransactionHandlerImpl;
import ch.uzh.ifi.csg.smartcontract.app.common.service.EthServiceProvider;
import ch.uzh.ifi.csg.smartcontract.app.p2p.connection.WifiConnectionManager;
import ch.uzh.ifi.csg.smartcontract.app.p2p.service.P2PBuyerService;
import ch.uzh.ifi.csg.smartcontract.app.p2p.service.P2PBuyerServiceImpl;
import ch.uzh.ifi.csg.smartcontract.app.p2p.service.P2PSellerService;
import ch.uzh.ifi.csg.smartcontract.app.p2p.service.P2PSellerServiceImpl;

/**
 * Custom application class that initializes, manages and provides all globally accessible
 * Service- and Provider objects.
 */
public class AppContext extends Application implements ApplicationContext
{
    private SettingProvider settingsProvider;
    private EthServiceProvider ethServiceProvider;
    private TransactionHandler transactionManager;
    private BroadcastReceiver broadcastReceiver;
    private P2PSellerService p2PSellerService;
    private P2PBuyerService p2PBuyerService;
    private WifiConnectionManager wifiManager;
    private PermissionProvider permissionProvider;
    private BroadCastService broadCastService;
    private MessageService messageService;
    private List<ActivityChangedListener> activityChangedListeners;

    /**
     * Initializes objects when Application is created
     */
    @Override
    public void onCreate() {
        super.onCreate();

        activityChangedListeners = new ArrayList<>();
        broadcastReceiver = new BroadCastReceiver();

        messageService = new MessageServiceImpl();
        permissionProvider = new PermissionProviderImpl();
        broadCastService = new LocalBroadcastService(this);
        broadCastService.registerReceiver(broadcastReceiver, new IntentFilter(SettingProviderImpl.ACTION_SETTINGS_CHANGED));
        settingsProvider = new SettingProviderImpl(this);
        ethServiceProvider = new EthServiceProvider(this);
        ethServiceProvider.initServices(settingsProvider);
        transactionManager = new TransactionHandlerImpl(this);

        WifiP2pManager p2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel p2pChannel = p2pManager.initialize(this, getMainLooper(), null);
        wifiManager = new WifiConnectionManager(p2pManager, p2pChannel);

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
    public TransactionHandler getTransactionManager()
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
        return messageService;
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
                //re-initialize all Ethereum services when the settings have changed
                ethServiceProvider.initServices(settingsProvider);
            }
        }
    }
}

