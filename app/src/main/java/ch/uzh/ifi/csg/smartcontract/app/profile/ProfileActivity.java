package ch.uzh.ifi.csg.smartcontract.app.profile;

import android.content.Intent;
import android.os.Bundle;

import ch.uzh.ifi.csg.smartcontract.app.R;
import ch.uzh.ifi.csg.smartcontract.app.account.AccountActivity;
import ch.uzh.ifi.csg.smartcontract.app.common.ActivityBase;
import ch.uzh.ifi.csg.smartcontract.app.common.permission.PermissionProvider;
import ch.uzh.ifi.csg.smartcontract.library.contract.ITradeContract;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.Account;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.UserProfile;
import ch.uzh.ifi.csg.smartcontract.library.util.ImageHelper;

/**
 * Activity that contains a {@link ProfileFragment} to display {@link UserProfile} objects of an
 * {@link Account} or an {@link ITradeContract} instance.
 */
public class ProfileActivity extends ActivityBase implements ProfileFragment.ProfileDataChangedListener {

    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.title_profile);

        if(getAppContext().getSettingProvider().getSelectedAccount().isEmpty())
        {
            //go back the AccountActivity when no account is unlocked
            Intent intent = new Intent(this, AccountActivity.class);
            startActivity(intent);
            return;
        }

        //init fragment
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

    /**
     * Loads an displays a profile of an {@link Account}
     */
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

    /**
     * see {@link ProfileFragment.ProfileDataChangedListener}
     * @param profile
     */
    @Override
    public void onProfileDataChanged(UserProfile profile)
    {
        //save profile for account
        String accountId = getAppContext().getSettingProvider().getSelectedAccount();
        getAppContext().getServiceProvider().getAccountService().saveAccountProfile(accountId, profile);
    }
}
