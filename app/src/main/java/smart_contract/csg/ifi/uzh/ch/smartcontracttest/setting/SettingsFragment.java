package smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import java.util.ArrayList;
import java.util.List;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

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

        ListPreference walletEncryptionList = (ListPreference) findPreference("pref_account_wallet_encryption_strength");
        List<String> encryptionStrengths = new ArrayList<>();
        encryptionStrengths.add("WEAK");
        encryptionStrengths.add("STRONG");
        walletEncryptionList.setEntryValues(encryptionStrengths.toArray(new String[2]));
        walletEncryptionList.setEntries(encryptionStrengths.toArray(new String[2]));
    }
}
