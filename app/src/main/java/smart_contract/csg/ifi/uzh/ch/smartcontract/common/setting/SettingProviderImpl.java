package smart_contract.csg.ifi.uzh.ch.smartcontract.common.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.math.BigInteger;

import smart_contract.csg.ifi.uzh.ch.smartcontract.R;
import smart_contract.csg.ifi.uzh.ch.smartcontract.account.AccountActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.provider.ApplicationContext;
import smart_contract.csg.ifi.uzh.ch.smartcontract.setting.SettingsActivity;

/**
 * Implementation of the {@link SettingProvider} interface. Initializes settings from
 * the SharedPreferences of the application. Updates settings when the SharedPreferences change
 * or when the unlocked account changes.
 */
public class SettingProviderImpl extends BroadcastReceiver implements SettingProvider, SharedPreferences.OnSharedPreferenceChangeListener
{
    public static final String ACTION_SETTINGS_CHANGED = "ch.uzh.ifi.csg.smart_contract.settings";

    private final ApplicationContext appContext;
    private String host;
    private int port;
    private String selectedAccount = "";
    private BigInteger gasPrice;
    private BigInteger gasLimit;
    private int transactionAttempts;
    private int transactionSleepDuration;
    private String walletFileEncryptionStrength;
    private String walletFileDirectory;
    private String localAccountDirectory;
    private String remoteAccountDirectory;
    private String imageDirectory;
    private int hostPollingInterval;
    private boolean useRemoteAccounts;

    public SettingProviderImpl(ApplicationContext appContext)
    {
        this.appContext = appContext;
        PreferenceManager.setDefaultValues(appContext.getContext(), R.xml.preferences, false);

        appContext.getBroadCastService().registerReceiver(this, new IntentFilter(AccountActivity.ACTION_ACCOUNT_CHANGED));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(appContext.getContext());
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        //Init from SharedPreferences
        setSetting(sharedPref, SettingsActivity.KEY_PREF_CLIENT_HOST);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_CLIENT_PORT);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_CLIENT_POLLING_INTERVAL);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_ACCOUNT_MANAGEMENT);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_ACCOUNT_WALLET_ENCRYPTION_STRENGTH);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_TRANSACTION_GAS_PRICE);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_TRANSACTION_GAS_LIMIT);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_TRANSACTION_ATTEMPTS);
        setSetting(sharedPref, SettingsActivity.KEY_PREF_TRANSACTION_SLEEP_DURATION);

        //Init static settings
        localAccountDirectory = appContext.getContext().getFilesDir().getAbsolutePath() + File.separator + "accounts";
        ensureDirectory(localAccountDirectory);
        remoteAccountDirectory = appContext.getContext().getFilesDir().getAbsolutePath() + File.separator + "accounts_remote";
        ensureDirectory(remoteAccountDirectory);
        imageDirectory = appContext.getContext().getFilesDir().getAbsolutePath() + File.separator + "images";
        ensureDirectory(imageDirectory);
        walletFileDirectory = appContext.getContext().getFilesDir().getAbsolutePath() + File.separator + "walletFiles";
        ensureDirectory(walletFileDirectory);
    }

    private void ensureDirectory(String dir_path)
    {
        File dir = new File(dir_path);
        if(!dir.exists())
            dir.mkdir();
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.getAction().equals(AccountActivity.ACTION_ACCOUNT_CHANGED))
        {
            String account = intent.getStringExtra(AccountActivity.MESSAGE_ACCOUNT_CHANGED);
            selectedAccount = account;

            appContext.getBroadCastService().sendBroadcast(new Intent(ACTION_SETTINGS_CHANGED));
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s)
    {
        setSetting(sharedPreferences, s);
        appContext.getBroadCastService().sendBroadcast(new Intent(ACTION_SETTINGS_CHANGED));
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
            case SettingsActivity.KEY_PREF_ACCOUNT_MANAGEMENT:
                useRemoteAccounts = preferences.getString(SettingsActivity.KEY_PREF_ACCOUNT_MANAGEMENT, "").equalsIgnoreCase("remote");
                break;
            case SettingsActivity.KEY_PREF_CLIENT_POLLING_INTERVAL:
                hostPollingInterval = Integer.valueOf(preferences.getString(SettingsActivity.KEY_PREF_CLIENT_POLLING_INTERVAL, ""));
                break;
            default:
                break;
        }
    }

    public String getWalletFileDirectory(){return walletFileDirectory; }

    @Override
    public boolean useStrongWalletFileEncryption() {
        return walletFileEncryptionStrength.equalsIgnoreCase("strong");
    }

    @Override
    public int getHostPollingInterval() {
        return hostPollingInterval;
    }

    @Override
    public boolean useRemoteAccountManagement() {
        return useRemoteAccounts;
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

    public String getAccountDirectory()
    {
        if(useRemoteAccounts)
        {
            return remoteAccountDirectory;
        }else{
            return localAccountDirectory;
        }
    }

    public String getImageDirectory()
    {
        return imageDirectory;
    }
}
