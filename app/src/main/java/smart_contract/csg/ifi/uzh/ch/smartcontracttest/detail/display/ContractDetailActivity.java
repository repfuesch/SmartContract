package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;

import org.jdeferred.Promise;

import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.event.IContractObserver;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ezvcard.Ezvcard;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.qrcode.QrScanningActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile.ProfileFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.dialog.P2pExportDialog;

public class ContractDetailActivity extends ActivityBase implements IContractObserver, ProfileFragment.OnProfileVerifiedListener, P2pExportDialog.P2pExportListener {

    public final static String EXTRA_CONTRACT_ADDRESS = "ch.uzh.ifi.csg.smart_contract.address";
    public final static String EXTRA_CONTRACT_TYPE = "ch.uzh.ifi.csg.smart_contract.type";

    private static final int SCAN_PROFILE_INFO_REQUEST = 1;

    private ContractDetailFragment detailFragment;
    private ProfileFragment profileFragment;
    private ITradeContract contract;

    private TabHost tabHost;
    private TabHost.TabSpec profileViewSpec;
    private LinearLayout tabContentWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_contract_detail);

        tabContentWrapper = (LinearLayout)findViewById(R.id.tabcontent_wrapper);

        Intent intent = getIntent();
        String contractAddress = intent.getStringExtra(EXTRA_CONTRACT_ADDRESS);
        ContractType contractType = (ContractType) intent.getSerializableExtra(EXTRA_CONTRACT_TYPE);

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        profileFragment = new ProfileFragment();
        switch(contractType)
        {
            case Purchase:
                detailFragment = new PurchaseContractDetailFragment();
                break;
            case Rent:
                detailFragment = new RentContractDetailFragment();
                break;
            default:
                throw new IllegalArgumentException("No Fragment implementation for contract type '" + contractType.toString() + "' exists!");
        }

        ft.add(android.R.id.tabcontent, profileFragment, "Profile");
        ft.add(android.R.id.tabcontent, detailFragment, "Details");
        ft.commit();
        fm.executePendingTransactions();

        initTabHost();
        LoadContract(contractAddress, contractType);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        contract.removeObserver(this);
    }

    private void LoadContract(String contractAddress, ContractType type)
    {
        getServiceProvider().getContractService().loadContract(type, contractAddress, getSettingProvider().getSelectedAccount())
                .always(new AlwaysCallback<ITradeContract>() {
                    @Override
                    public void onAlways(Promise.State state, final ITradeContract resolved, Throwable rejected) {

                        if(rejected != null)
                        {
                            handleError(rejected);
                            return;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                contract = resolved;
                                contract.addObserver(ContractDetailActivity.this);
                                attachContract();
                            }
                        });
                    }
                });
    }

    private void attachContract()
    {
        if(contract == null)
            return;

        this.detailFragment.init(contract);

        if(this.detailFragment.needsIdentityVerification() && contract.getUserProfile().getVCard() != null)
        {
            profileFragment.setProfileInformation(contract.getUserProfile());
            if(!contract.getUserProfile().isVerified())
            {
                profileFragment.setMode(ProfileFragment.ProfileMode.Verify);
            }else{
                profileFragment.setMode(ProfileFragment.ProfileMode.ReadOnly);
            }
        }
    }

    private void initTabHost()
    {
        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        /** Defining Tab Change Listener event. This is invoked when tab is changed */
        TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {

                BusyIndicator.show(tabContentWrapper);

                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                if(profileFragment!=null)
                    ft.detach(profileFragment);

                if(detailFragment!=null)
                    ft.detach(detailFragment);

                if(tabId.equalsIgnoreCase("Profile")){
                    ft.attach(profileFragment);
                }else{
                    ft.attach(detailFragment);
                }

                ft.commit();
                fm.executePendingTransactions();
                BusyIndicator.hide(tabContentWrapper);

                attachContract();
            }
        };

        /** Setting tabchangelistener for the tab */
        tabHost.setOnTabChangedListener(tabChangeListener);

        //set detail tab
        TabHost.TabSpec spec = tabHost.newTabSpec("Details");
        spec.setContent(getFragmentManager().findFragmentByTag("Details").getId());
        spec.setIndicator("", getResources().getDrawable(R.drawable.ic_tab_general_info));
        tabHost.addTab(spec);

        //set profile tab
        profileViewSpec = tabHost.newTabSpec("Profile");
        profileViewSpec.setContent(getFragmentManager().findFragmentByTag("Profile").getId());
        profileViewSpec.setIndicator("", getResources().getDrawable(R.drawable.ic_tab_contact_info));
        tabHost.addTab(profileViewSpec);

        tabHost.setCurrentTabByTag("Profile");
        tabHost.setCurrentTabByTag("Details");
    }

    private void removeProfileTab()
    {
        if(tabHost.getTabWidget().getTabCount() > 1)
            tabHost.getTabWidget().removeView(tabHost.getTabWidget().getChildTabViewAt(1));
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

                String vCardString = intent.getStringExtra(QrScanningActivity.MESSAGE_PROFILE_DATA);
                UserProfile profile = new UserProfile();
                profile.setVCard(Ezvcard.parse(vCardString).first());
                profileFragment.setProfileInformation(profile);
                tabHost.setCurrentTabByTag("Profile");
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    protected void onSettingsChanged() {
        detailFragment.init(contract);
    }

    @Override
    public void contractStateChanged(String event, Object value)
    {
        detailFragment.init(contract);
    }

    @Override
    public void onProfileVerified(UserProfile profile)
    {
        detailFragment.verifyIdentity();
        tabHost.setCurrentTabByTag("Details");
        contract.setUserProfile(profile);
        profileFragment.setMode(ProfileFragment.ProfileMode.ReadOnly);
        getServiceProvider().getContractService().saveContract(contract, getSettingProvider().getSelectedAccount());
        detailFragment.identityVerified();
    }

    public void onExportContractClick(View view)
    {
        DialogFragment exportFragment = new P2pExportDialog();
        Bundle args = new Bundle();
        args.putBoolean(P2pExportDialog.MESSAGE_IDENTIFICATION_USED, detailFragment.isVerifyIdentity());
        args.putSerializable(P2pExportDialog.MESSAGE_CONTRACT_DATA, new ContractInfo(contract.getContractType(), contract.getContractAddress(), new UserProfile(), contract.getImages()));
        exportFragment.setArguments(args);
        exportFragment.show(getSupportFragmentManager(), "importDialogFragment");
    }

    @Override
    public void onContractDataExchanged(UserProfile buyerProfile) {
        if(buyerProfile != null)
        {
            tabHost.setCurrentTabByTag("Profile");
            contract.setUserProfile(buyerProfile);
            getServiceProvider().getContractService().saveContract(contract, getSettingProvider().getSelectedAccount());
            profileFragment.setProfileInformation(buyerProfile);
            profileFragment.setMode(ProfileFragment.ProfileMode.ReadOnly);
        }
    }

    @Override
    public void onContractDialogCanceled() {
    }
}
