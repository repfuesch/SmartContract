package smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link PreferenceFragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment {

    private static final int PICKFILE_RESULT_CODE = 1;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        Preference filePicker = (Preference) findPreference("pref_account_wallet_directory");
        filePicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                openFile();
                return true;
            }
        });

        ListPreference walletEncryptionList = (ListPreference) findPreference("pref_account_wallet_encryption_strength");
        List<String> encryptionStrengths = new ArrayList<>();
        encryptionStrengths.add("WEAK");
        encryptionStrengths.add("STRONG");
        walletEncryptionList.setEntryValues(encryptionStrengths.toArray(new String[2]));
        walletEncryptionList.setEntries(encryptionStrengths.toArray(new String[2]));
    }

    public void openFile() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DIRECTORY");
        // if you want any file type, you can skip next line
        sIntent.putExtra("CONTENT_TYPE", "*/*");
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (getActivity().getPackageManager().resolveActivity(sIntent, 0) != null){
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open Folder");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { intent});
        }
        else {
            chooserIntent = Intent.createChooser(intent, "Open Folder");
        }

        try {
            startActivityForResult(chooserIntent, PICKFILE_RESULT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity().getApplicationContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //get the new value from Intent data
        switch(requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    String filePath = data.getData().getPath();
                    SharedPreferences preferences =  getPreferenceManager().getSharedPreferences();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("pref_account_wallet_directory", filePath);
                    editor.commit();
                }
                break;
        }
    }
}
