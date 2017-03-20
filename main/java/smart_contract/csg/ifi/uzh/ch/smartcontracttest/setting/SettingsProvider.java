package smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import java.math.BigInteger;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.AppContext;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.login.LoginDialogFragment;

/**
 * Created by flo on 20.03.17.
 */

public class SettingsProvider extends BroadcastReceiver implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String ACTION_SETTINGS_CHANGED = "ch.uzh.ifi.csg.smart_contract.settings";

    private final static SettingsProvider instance;

    static {
        instance = new SettingsProvider();
    }

    public static SettingsProvider getInstance()
    {
        return instance;
    }

    private String host;
    private int port;
    private String selectedAccount;
    private int accountUnlockTime;
    private BigInteger gasPrice;
    private BigInteger gasLimit;
    private int transactionAttempts;
    private int transactionSleepDuration;

    public SettingsProvider()
    {
        PreferenceManager.setDefaultValues(AppContext.getContext(), R.xml.preferences, false);
        LocalBroadcastManager.getInstance(AppContext.getContext()).registerReceiver(this, new IntentFilter(LoginDialogFragment.ACTION_ACCOUNT_CHANGED));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(AppContext.getContext());
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        setSetting(sharedPref, SettingsActivity.KEY_PREF_CLIENT_HOST);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_CLIENT_PORT);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_ACCOUNT_UNLOCK_TIME);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_TRANSACTION_GAS_PRICE);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_TRANSACTION_GAS_LIMIT);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_TRANSACTION_ATTEMPTS);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_TRANSACTION_SLEEP_DURATION);
        ServiceProvider.getInstance().initServices(this);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.getAction().equals(LoginDialogFragment.ACTION_ACCOUNT_CHANGED))
        {
            String account = intent.getStringExtra(LoginDialogFragment.MESSAGE_ACCOUNT_CHANGED);
            selectedAccount = account;

            ServiceProvider.getInstance().initServices(this);
            LocalBroadcastManager.getInstance(AppContext.getContext()).sendBroadcast(new Intent(ACTION_SETTINGS_CHANGED));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s)
    {
        setSetting(sharedPreferences, s);
        ServiceProvider.getInstance().initServices(this);
        LocalBroadcastManager.getInstance(AppContext.getContext()).sendBroadcast(new Intent(ACTION_SETTINGS_CHANGED));
    }

    private void setSetting(SharedPreferences preferences, String key)
    {
        switch(key)
        {
            case SettingsActivity.KEY_PREF_CLIENT_HOST:
                host = preferences.getString(SettingsActivity.KEY_PREF_CLIENT_HOST, "");
                break;
            case SettingsActivity.KEY_PREF_CLIENT_PORT:
                port = Integer.valueOf(preferences.getString(SettingsActivity.KEY_PREF_CLIENT_PORT, ""));
                break;
            case SettingsActivity.KEY_PREF_ACCOUNT_UNLOCK_TIME:
                accountUnlockTime = Integer.valueOf(preferences.getString(SettingsActivity.KEY_PREF_ACCOUNT_UNLOCK_TIME, ""));
                break;
            case SettingsActivity.KEY_PREF_TRANSACTION_GAS_PRICE:
                gasPrice = BigInteger.valueOf(Long.valueOf(preferences.getString(SettingsActivity.KEY_PREF_TRANSACTION_GAS_PRICE, "")));
                break;
            case SettingsActivity.KEY_PREF_TRANSACTION_GAS_LIMIT:
                gasLimit = BigInteger.valueOf(Long.valueOf(preferences.getString(SettingsActivity.KEY_PREF_TRANSACTION_GAS_LIMIT, "")));
                break;
            case SettingsActivity.KEY_PREF_TRANSACTION_ATTEMPTS:
                transactionAttempts = Integer.valueOf(preferences.getString(SettingsActivity.KEY_PREF_TRANSACTION_ATTEMPTS, ""));
                break;
            case SettingsActivity.KEY_PREF_TRANSACTION_SLEEP_DURATION:
                transactionSleepDuration = Integer.valueOf(preferences.getString(SettingsActivity.KEY_PREF_TRANSACTION_SLEEP_DURATION, ""));
                break;
            default:
                break;
        }
    }

    public int getAccountUnlockTime() {
        return accountUnlockTime;
    }

    public String getHost() {
        return host;
    }

    public int getTransactionSleepDuration() {
        return transactionSleepDuration;
    }

    public int getTransactionAttempts() {
        return transactionAttempts;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public String getSelectedAccount() {
        return selectedAccount;
    }

    public int getPort() {
        return port;
    }
}
