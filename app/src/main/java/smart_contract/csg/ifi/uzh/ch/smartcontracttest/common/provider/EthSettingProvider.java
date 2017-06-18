package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import java.math.BigInteger;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.account.AccountActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.AppContext;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsActivity;

/**
 * Created by flo on 20.03.17.
 */

public class EthSettingProvider extends BroadcastReceiver implements SettingProvider, SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String ACTION_SETTINGS_CHANGED = "ch.uzh.ifi.csg.smart_contract.settings";

    private static EthSettingProvider instance;

    public static EthSettingProvider create(AppContext context)
    {
        instance = new EthSettingProvider(context);
        return instance;
    }

    private final AppContext appContext;
    private String host;
    private int port;
    private String selectedAccount = "";
    private int accountUnlockTime;
    private BigInteger gasPrice;
    private BigInteger gasLimit;
    private int transactionAttempts;
    private int transactionSleepDuration;
    private String walletFileEncryptionStrength;
    private String walletFileDirectory;

    public EthSettingProvider(AppContext appContext)
    {
        this.appContext = appContext;
        PreferenceManager.setDefaultValues(appContext, R.xml.preferences, false);
        LocalBroadcastManager.getInstance(appContext).registerReceiver(this, new IntentFilter(AccountActivity.ACTION_ACCOUNT_CHANGED));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        setSetting(sharedPref, SettingsActivity.KEY_PREF_CLIENT_HOST);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_CLIENT_PORT);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_ACCOUNT_WALLET_ENCRYPTION_STRENGTH);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_ACCOUNT_WALLET_FILE_DIRECTORY);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_TRANSACTION_GAS_PRICE);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_TRANSACTION_GAS_LIMIT);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_TRANSACTION_ATTEMPTS);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_TRANSACTION_SLEEP_DURATION);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.getAction().equals(AccountActivity.ACTION_ACCOUNT_CHANGED))
        {
            String account = intent.getStringExtra(AccountActivity.MESSAGE_ACCOUNT_CHANGED);
            selectedAccount = account;

            LocalBroadcastManager.getInstance(appContext).sendBroadcast(new Intent(ACTION_SETTINGS_CHANGED));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s)
    {
        setSetting(sharedPreferences, s);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(new Intent(ACTION_SETTINGS_CHANGED));
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
            case SettingsActivity.KEY_PREF_ACCOUNT_WALLET_ENCRYPTION_STRENGTH:
                walletFileEncryptionStrength = preferences.getString(SettingsActivity.KEY_PREF_ACCOUNT_WALLET_ENCRYPTION_STRENGTH, "");
                break;
            case SettingsActivity.KEY_PREF_ACCOUNT_WALLET_FILE_DIRECTORY:
                walletFileDirectory = preferences.getString(SettingsActivity.KEY_PREF_ACCOUNT_WALLET_FILE_DIRECTORY, "");
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

    public String getWalletFileEncryptionStrength() {
        return walletFileEncryptionStrength;
    }

    public String getWalletFileDirectory(){return walletFileDirectory;}

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

    public String getAccountDirectory()
    {
        return appContext.getApplicationContext().getFilesDir() + "/accounts_remote";
    }

    public String getProfileImageDirectory()
    {
        return appContext.getApplicationContext().getFilesDir() + "/profile_images";
    }
}
