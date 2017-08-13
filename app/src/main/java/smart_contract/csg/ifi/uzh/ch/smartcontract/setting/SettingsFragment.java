package smart_contract.csg.ifi.uzh.ch.smartcontract.setting;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import java.util.ArrayList;
import java.util.List;
import smart_contract.csg.ifi.uzh.ch.smartcontract.R;

/**
 * A simple {@link PreferenceFragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        //Load list preferences
        ListPreference walletEncryptionList = (ListPreference) findPreference(SettingsActivity.KEY_PREF_ACCOUNT_WALLET_ENCRYPTION_STRENGTH);
        List<String> encryptionStrengths = new ArrayList<>();
        encryptionStrengths.add("WEAK");
        encryptionStrengths.add("STRONG");
        walletEncryptionList.setEntryValues(encryptionStrengths.toArray(new String[2]));
        walletEncryptionList.setEntries(encryptionStrengths.toArray(new String[2]));

        ListPreference accountManagementOptionList = (ListPreference) findPreference(SettingsActivity.KEY_PREF_ACCOUNT_MANAGEMENT);
        List<String> managementOptions = new ArrayList<>();
        managementOptions.add("LOCAL");
        managementOptions.add("REMOTE");
        accountManagementOptionList.setEntryValues(managementOptions.toArray(new String[2]));
        accountManagementOptionList.setEntries(managementOptions.toArray(new String[2]));
    }
}
