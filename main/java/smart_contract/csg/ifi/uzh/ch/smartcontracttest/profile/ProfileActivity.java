package smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile;

import android.os.Bundle;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;

public class ProfileActivity extends ActivityBase {

    private ProfileFragment vCardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vCardFragment = (ProfileFragment) getFragmentManager().findFragmentById(R.id.vCard_fragment);
        vCardFragment.setMode(ProfileFragment.ProfileMode.Edit);
        vCardFragment.loadAccountProfileInformation();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onSettingsChanged() {
        vCardFragment.loadAccountProfileInformation();
    }
}
