package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TabHost;

import org.jdeferred.Promise;

import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.event.IContractObserver;
import ch.uzh.ifi.csg.contract.service.account.UserProfile;
import ezvcard.Ezvcard;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.QrScanningActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile.ProfileFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsProvider;

public class ContractDetailActivity extends ActivityBase implements IContractObserver, ProfileFragment.OnProfileVerifiedListener {

    public final static String ACTION_SHOW_CONTRACT_DETAILS = "ch.uzh.ifi.csg.smart_contract.detail";
    public final static String MESSAGE_SHOW_CONTRACT_DETAILS = "ch.uzh.ifi.csg.smart_contract.detail.address";
    private static final int SCAN_PROFILE_INFO_REQUEST = 1;

    private ContractGeneralInfoFragment generalInfoFragment;
    private ProfileFragment contactFragment;
    private IPurchaseContract contract;

    private TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_contract_detail);

        Intent intent = getIntent();
        String contractAddress = intent.getStringExtra(MESSAGE_SHOW_CONTRACT_DETAILS);
        generalInfoFragment = (ContractGeneralInfoFragment) getFragmentManager().findFragmentById(R.id.general_info);
        contactFragment = (ProfileFragment) getFragmentManager().findFragmentById(R.id.fragment_contact_info);
        contactFragment.setReadOnly();
        contactFragment.enableVerification();

        initTabHost();

        LoadContract(contractAddress);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        contract.removeObserver(this);
    }

    private void LoadContract(String contractAddress)
    {
        ServiceProvider.getInstance().getContractService().loadContract(contractAddress, SettingsProvider.getInstance().getSelectedAccount()).always(new AlwaysCallback<IPurchaseContract>() {
            @Override
            public void onAlways(Promise.State state, final IPurchaseContract resolved, Throwable rejected) {

                if(rejected != null)
                {
                    handleError(rejected);
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contract = resolved;
                        generalInfoFragment.setContract(resolved);
                        generalInfoFragment.updateView();
                        contactFragment.setProfileInformation(contract.getUserProfile());
                        resolved.addObserver(ContractDetailActivity.this);
                    }
                });
            }
        });
    }

    private void initTabHost()
    {
        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        //Tab 1
        TabHost.TabSpec spec = tabHost.newTabSpec("General");
        spec.setContent(R.id.general_info);
        spec.setIndicator("", getResources().getDrawable(R.drawable.ic_tab_general_info));
        tabHost.addTab(spec);

        //Tab2
        TabHost.TabSpec spec2 = tabHost.newTabSpec("Contact");
        spec2.setIndicator("", getResources().getDrawable(R.drawable.ic_tab_contact_info));
        spec2.setContent(R.id.fragment_contact_info);
        tabHost.addTab(spec2);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_contract_detail2;
    }

    public void onScanQrImageClick(View view)
    {
        Intent intent = new Intent(this, QrScanningActivity.class);
        intent.setAction(QrScanningActivity.ACTION_SCAN_PROFILE);
        startActivityForResult(
                intent,
                SCAN_PROFILE_INFO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        switch (requestCode)
        {
            case SCAN_PROFILE_INFO_REQUEST:
                if(intent == null)
                    return;

                String vCardString = intent.getStringExtra(QrScanningActivity.MESSAGE_SCAN_DATA);
                UserProfile profile = new UserProfile();
                profile.setVCard(Ezvcard.parse(vCardString).first());
                contactFragment.setProfileInformation(profile);
                tabHost.setCurrentTabByTag("Contact");
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    protected void onSettingsChanged() {
        LoadContract(contract.getContractAddress());
    }

    @Override
    public void contractStateChanged(String event, Object value)
    {
        generalInfoFragment.updateView();
    }

    @Override
    public void onProfileVerified(UserProfile profile)
    {
        generalInfoFragment.verifyIdentity();
        tabHost.setCurrentTabByTag("General");
        contract.setUserProfile(profile);
        ServiceProvider.getInstance().getContractService().saveContract(contract, SettingsProvider.getInstance().getSelectedAccount());
    }
}
