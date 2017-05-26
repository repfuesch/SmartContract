package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.SettingProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionManager;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionManagerImpl;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.EthServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.EthSettingProvider;

/**
 * Custom application class for holding and accessing the EthServiceProvider and EthSettingProvider instances
 */

public class AppContext extends Application
{
    private EthSettingProvider settingsProvider;
    private EthServiceProvider ethServiceProvider;
    private TransactionManagerImpl transactionManager;
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        broadcastReceiver = new BroadCastReceiver();
        broadcastManager = LocalBroadcastManager.getInstance(this);

        settingsProvider = EthSettingProvider.create(this);
        ethServiceProvider = EthServiceProvider.create(this);
        ethServiceProvider.initServices(settingsProvider);
        transactionManager = TransactionManagerImpl.create(broadcastManager);

        broadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(EthSettingProvider.ACTION_SETTINGS_CHANGED));
    }

    public SettingProvider getSettingsProvider()
    {
        return settingsProvider;
    }

    public ServiceProvider getServiceProvider()
    {
        return ethServiceProvider;
    }

    public TransactionManager getTransactionManager()
    {
        return transactionManager;
    }

    private class BroadCastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getAction().equals(EthSettingProvider.ACTION_SETTINGS_CHANGED))
            {
                ethServiceProvider.initServices(settingsProvider);
            }
        }
    }
}

