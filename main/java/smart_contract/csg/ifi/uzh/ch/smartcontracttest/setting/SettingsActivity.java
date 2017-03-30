package smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import ch.uzh.ifi.csg.contract.setting.EthSettings;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;

public class SettingsActivity extends ActivityBase
{
    public static final String KEY_PREF_CLIENT_HOST = "pref_client_host_key";
    public static final String KEY_PREF_CLIENT_PORT = "pref_client_port_key";
    public static final String KEY_PREF_ACCOUNT_UNLOCK_TIME = "pref_account_unlock_time_key";
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

        getBalanceView().setVisibility(View.GONE);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_settings;
    }

    @Override
    protected void onSettingsChanged() {
    }
}
