package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.TabHost;

import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile.ProfileFragment;

public class ContractCreateActivity extends ActivityBase implements ProfileFragment.OnProfileVerifiedListener, ContractDeployFragment.OnProfileVerificationRequestedListener {

    public static final String CONTRACT_TYPE_EXTRA = "ch.uzh.ifi.csg.smart_contract.type";
    public static final int SCAN_CONTRACT_INFO_REQUEST = 1;

    private ContractDeployFragment deployFragment;
    private ProfileFragment profileFragment;
    private TabHost.TabSpec profileTabSpec;
    private TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_contract_create);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ContractType type = (ContractType) getIntent().getSerializableExtra(CONTRACT_TYPE_EXTRA);
        switch(type)
        {
            case Purchase:
                deployFragment = new PurchaseContractDeployFragment();
                break;
            case Rent:
                deployFragment = new RentContractDeployFragment();
                break;
            default:
                throw new IllegalArgumentException("Contract to deploy must be specified!");
        }

        fragmentTransaction.add(android.R.id.tabcontent, deployFragment, "DeployFragment");
        fragmentTransaction.addToBackStack("DeployFragment");
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
        //
        profileFragment = (ProfileFragment) getFragmentManager().findFragmentById(R.id.fragment_contact_info);
        profileFragment.setMode(ProfileFragment.ProfileMode.Verify);

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
        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        //Tab 1
        TabHost.TabSpec spec = tabHost.newTabSpec("General");
        spec.setContent(getFragmentManager().findFragmentByTag("DeployFragment").getId());
        spec.setIndicator("", getResources().getDrawable(R.drawable.ic_tab_general_info));
        tabHost.addTab(spec);

        //Tab2
        profileTabSpec = tabHost.newTabSpec("Contact");
        profileTabSpec.setIndicator("", getResources().getDrawable(R.drawable.ic_tab_contact_info));
        profileTabSpec.setContent(R.id.fragment_contact_info);
    }

    private void addProfileTab()
    {
        if(tabHost.getTabWidget().getTabCount() == 1)
            tabHost.addTab(profileTabSpec);
    }

    private void removeProfileTab()
    {
        if(tabHost.getTabWidget().getTabCount() > 1)
            tabHost.getTabWidget().removeView(tabHost.getTabWidget().getChildTabViewAt(1));
    }

    @Override
    public void onProfileVerified(UserProfile profile)
    {
        deployFragment.verifyIdentity(profile);
        tabHost.setCurrentTabByTag("General");
    }

    @Override
    public void onProfileVerificationEnabled(boolean isRequested) {
        if(!isRequested)
        {
            removeProfileTab();
        }
    }

    @Override
    public void onProfileVerificationRequested(UserProfile profile) {
        addProfileTab();
        profileFragment.setProfileInformation(profile);
        tabHost.setCurrentTabByTag("Contact");
    }

}
