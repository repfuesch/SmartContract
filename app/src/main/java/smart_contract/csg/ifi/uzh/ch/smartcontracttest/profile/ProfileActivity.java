package smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile;

import android.os.Bundle;

import ch.uzh.ifi.csg.contract.common.ImageHelper;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.permission.PermissionProvider;

public class ProfileActivity extends ActivityBase {

    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileFragment = (ProfileFragment) getFragmentManager().findFragmentById(R.id.vCard_fragment);
        profileFragment.setMode(ProfileFragment.ProfileMode.Edit);
        profileFragment.loadAccountProfileInformation();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onSettingsChanged() {
        profileFragment.loadAccountProfileInformation();
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
}
