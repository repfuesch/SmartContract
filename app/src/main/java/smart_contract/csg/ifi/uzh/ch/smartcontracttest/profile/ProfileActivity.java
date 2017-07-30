package smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile;

import android.content.Intent;
import android.os.Bundle;

import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.util.ImageHelper;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.account.AccountActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.permission.PermissionProvider;

public class ProfileActivity extends ActivityBase implements ProfileFragment.ProfileDataChangedListener {

    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getAppContext().getSettingProvider().getSelectedAccount().isEmpty())
        {
            Intent intent = new Intent(this, AccountActivity.class);
            startActivity(intent);
        }

        profileFragment = (ProfileFragment) getFragmentManager().findFragmentById(R.id.profile_fragment);
        profileFragment.setMode(ProfileFragment.ProfileMode.Edit);
        loadAccountProfile();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onSettingsChanged()
    {
        loadAccountProfile();
    }

    private void loadAccountProfile()
    {
        String selectedAccount = getAppContext().getSettingProvider().getSelectedAccount();
        UserProfile userProfile = getAppContext().getServiceProvider().getAccountService().getAccountProfile(selectedAccount);

        profileFragment.setProfileInformation(userProfile);
    }

    @Override
    protected void onPermissionGranted(String permission) {
        super.onPermissionGranted(permission);

        if(permission.equals(PermissionProvider.READ_STORAGE))
        {
            ImageHelper.openFile(profileFragment);
        }else if(permission.equals(PermissionProvider.CAMERA))
        {
            ImageHelper.makePicture(profileFragment);
        }
    }

    @Override
    public void onProfileDataChanged(UserProfile profile)
    {
        String accountId = getAppContext().getSettingProvider().getSelectedAccount();
        getAppContext().getServiceProvider().getAccountService().saveAccountProfile(accountId, profile);
    }
}
