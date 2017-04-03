package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;

import ch.uzh.ifi.csg.contract.service.account.UserProfile;
import ezvcard.Ezvcard;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.QrScanningActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.ContractOverviewActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile.ProfileFragment;

public class ContractCreateActivity extends ActivityBase implements ProfileFragment.OnProfileVerifiedListener {

    private static final int SCAN_CONTRACT_INFO_REQUEST = 1;

    private ContractDeployFragment deployFragment;
    private ProfileFragment contactFragment;
    private TabHost tabhost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_contract_create);

        deployFragment = (ContractDeployFragment) getFragmentManager().findFragmentById(R.id.fragment_contract_create);
        contactFragment = (ProfileFragment) getFragmentManager().findFragmentById(R.id.fragment_contact_info);
        contactFragment.setReadOnly();
        contactFragment.enableVerification();

        initTabHost();
    }


    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_contract_detail;
    }

    @Override
    protected void onSettingsChanged() {
    }

    private void initTabHost()
    {
        tabhost = (TabHost)findViewById(R.id.tabHost);
        tabhost.setup();

        //Tab 1
        TabHost.TabSpec spec = tabhost.newTabSpec("General");
        spec.setContent(R.id.fragment_contract_create);
        spec.setIndicator("", getResources().getDrawable(R.drawable.ic_tab_general_info));
        tabhost.addTab(spec);

        //Tab2
        TabHost.TabSpec spec2 = tabhost.newTabSpec("Contact");
        spec2.setIndicator("", getResources().getDrawable(R.drawable.ic_tab_contact_info));
        spec2.setContent(R.id.fragment_contact_info);
        tabhost.addTab(spec2);
    }

    public void onScanQrImageClick(View view)
    {
        Intent intent = new Intent(this, QrScanningActivity.class);
        intent.setAction(QrScanningActivity.ACTION_SCAN_CONTRACT);
        startActivityForResult(
                intent,
                SCAN_CONTRACT_INFO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        switch (requestCode)
        {
            case SCAN_CONTRACT_INFO_REQUEST:
                if(intent == null)
                    return;

                String vCardString = intent.getStringExtra(QrScanningActivity.MESSAGE_SCAN_DATA);
                UserProfile profile = new UserProfile();
                profile.setVCard(Ezvcard.parse(vCardString).first());
                contactFragment.setProfileInformation(profile);
                tabhost.setCurrentTabByTag("Contact");
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    public void onDeployContractButtonClick(final View view)
    {
        deployFragment.deployContract();
    }

    public void onCancelContractButtonClick(View view)
    {
        Intent intent = new Intent(this, ContractOverviewActivity.class);
        startActivity(intent);
    }

    @Override
    public void onProfileVerified(UserProfile profile)
    {
        deployFragment.verifyIdentity(profile);
        tabhost.setCurrentTabByTag("General");
    }
}
