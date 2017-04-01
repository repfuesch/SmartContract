package smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile;

import android.app.DialogFragment;
import android.app.Service;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import net.glxn.qrgen.core.scheme.VCard;

import ch.uzh.ifi.csg.contract.service.account.AccountProfile;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ImageDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.PurchaseContractRecyclerViewAdapter;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsProvider;

public class ProfileActivity extends ActivityBase {

    private ProfileVCardFragment vCardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vCardFragment = (ProfileVCardFragment) getFragmentManager().findFragmentById(R.id.vCard_fragment);
        loadProfileInformation();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_profile;
    }

    private void loadProfileInformation()
    {
        String selectedAccount = SettingsProvider.getInstance().getSelectedAccount();
        AccountProfile profile = ServiceProvider.getInstance().getAccountService().getAccountProfile(selectedAccount);
        vCardFragment.setProfileInformation(profile);
    }

    public void onSaveButtonClick(View view)
    {
        String selectedAccount = SettingsProvider.getInstance().getSelectedAccount();
        AccountProfile profileInfo = vCardFragment.getProfileInformation();
        ServiceProvider.getInstance().getAccountService().saveAccountProfile(selectedAccount, profileInfo);
    }

    public void onQrImageClick(View view)
    {
        DialogFragment imageDialog = new ImageDialogFragment();
        Bundle args = new Bundle();
        args.putString(ImageDialogFragment.MESSAGE_IMAGE_SOURCE, vCardFragment.getProfileInformation().getVCard().toString());
        args.putBoolean(ImageDialogFragment.MESSAGE_DISPLAY_QRCODE, true);
        imageDialog.setArguments(args);
        imageDialog.show(getFragmentManager(), "QrImageDialog");
    }

    public void onCancelButtonClick(View view)
    {
    }

    @Override
    protected void onSettingsChanged() {
        loadProfileInformation();
    }
}
