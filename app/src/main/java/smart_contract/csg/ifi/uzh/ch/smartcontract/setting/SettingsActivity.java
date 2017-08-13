package smart_contract.csg.ifi.uzh.ch.smartcontract.setting;

import android.content.SharedPreferences;
import android.os.Bundle;

import smart_contract.csg.ifi.uzh.ch.smartcontract.R;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.setting.SettingProvider;

/**
 * Activity that contains a {@link SettingsFragment} that displays the current
 * {@link SharedPreferences} of the application to the user and provides the interface to change
 * them.
 * It contains the keys of the preferences. Please refer to the {@link SettingProvider} for an
 * explanation of the individual settings of the application.
 */
public class SettingsActivity extends ActivityBase
{
    //Preference Keys
    public static final String KEY_PREF_CLIENT_HOST = "pref_client_host_key";
    public static final String KEY_PREF_CLIENT_PORT = "pref_client_port_key";
    public static final String KEY_PREF_CLIENT_POLLING_INTERVAL = "pref_client_polling_interval";
    public static final String KEY_PREF_ACCOUNT_MANAGEMENT = "pref_account_management";
    public static final String KEY_PREF_ACCOUNT_WALLET_ENCRYPTION_STRENGTH = "pref_account_wallet_encryption_strength";
    public static final String KEY_PREF_TRANSACTION_GAS_PRICE = "pref_transaction_gas_price_key";
    public static final String KEY_PREF_TRANSACTION_GAS_LIMIT = "pref_transaction_gas_limit_key";
    public static final String KEY_PREF_TRANSACTION_ATTEMPTS = "pref_transaction_attempts_key";
    public static final String KEY_PREF_TRANSACTION_SLEEP_DURATION = "pref_transaction_sleep_time_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.title_settings);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .add(R.id.settings_fragment, new SettingsFragment())
                .commit();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_settings;
    }

    @Override
    protected void onSettingsChanged() {
    }
}
