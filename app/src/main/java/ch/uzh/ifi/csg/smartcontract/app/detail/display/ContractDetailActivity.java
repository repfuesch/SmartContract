package ch.uzh.ifi.csg.smartcontract.app.detail.display;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TabHost;
import ch.uzh.ifi.csg.smartcontract.app.common.ActivityBase;
import ch.uzh.ifi.csg.smartcontract.app.common.permission.PermissionProvider;
import ch.uzh.ifi.csg.smartcontract.app.overview.ContractOverviewActivity;
import ch.uzh.ifi.csg.smartcontract.app.qrcode.QrScanningActivity;
import ch.uzh.ifi.csg.smartcontract.app.R;
import ch.uzh.ifi.csg.smartcontract.app.profile.ProfileFragment;
import ch.uzh.ifi.csg.smartcontract.app.p2p.dialog.P2pExportDialog;
import ch.uzh.ifi.csg.smartcontract.library.async.promise.DoneCallback;
import ch.uzh.ifi.csg.smartcontract.library.contract.ContractState;
import ch.uzh.ifi.csg.smartcontract.library.contract.ContractType;
import ch.uzh.ifi.csg.smartcontract.library.contract.IContractObserver;
import ch.uzh.ifi.csg.smartcontract.library.contract.ITradeContract;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.UserProfile;
import ch.uzh.ifi.csg.smartcontract.library.service.serialization.GsonSerializationService;

/**
 * Activity that contains a  {@link ContractDetailFragment} instance to display the details of a
 * {@link ITradeContract} instance. The concrete fragment type depends on the
 * {@link #EXTRA_CONTRACT_TYPE} provided in the start intent and the {@link #EXTRA_CONTRACT_ADDRESS}
 * references the contract to load.
 *
 * If the contract requires identity verification and the contract contains a {@link UserProfile},
 * then this profile is displayed in the {@link ProfileFragment}
 *
 * The activity also contains provides the UI to export a contract over Wi-Fi
 * ({@link #onExportContractClick}) and to scan the profile of another party
 * ({@link #onScanQrImageClick}
 *
 */
public class ContractDetailActivity extends ActivityBase implements IContractObserver, P2pExportDialog.P2pExportListener, ProfileFragment.ProfileDataChangedListener {

    public final static String EXTRA_CONTRACT_ADDRESS = "ch.uzh.ifi.csg.smart_contract.address";
    public final static String EXTRA_CONTRACT_TYPE = "ch.uzh.ifi.csg.smart_contract.type";

    public static final int SCAN_PROFILE_INFO_REQUEST = 1;

    private ContractDetailFragment detailFragment;
    private ProfileFragment profileFragment;
    private ITradeContract contract;
    private String contractAddress;
    private ContractType contractType;

    private TabHost tabHost;
    private TabHost.TabSpec profileViewSpec;
    private ImageButton exportButton;
    private ImageButton scanProfileButton;

    @Override
    protected void onStart() {
        super.onStart();

        FragmentManager fm = getFragmentManager();

        if(fm.findFragmentByTag("Details") != null)
            return;

        //select correct details fragment for contract type
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

        //add fragments to tab content
        fm.beginTransaction()
                .add(android.R.id.tabcontent, profileFragment, "Profile")
                .add(android.R.id.tabcontent, detailFragment, "Details")
                .commit();

        fm.executePendingTransactions();

        fm.findFragmentByTag("Profile");
        fm.findFragmentByTag("Details");

        init(contractAddress, contractType);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_contract_detail);

        exportButton = (ImageButton)findViewById(R.id.action_export_contract);
        scanProfileButton = (ImageButton)findViewById(R.id.action_scan_profile);

        Intent intent = getIntent();
        contractAddress = intent.getStringExtra(EXTRA_CONTRACT_ADDRESS);
        contractType = (ContractType) intent.getSerializableExtra(EXTRA_CONTRACT_TYPE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //unsubscribe from the contract
        if(contract != null)
            contract.removeObserver(this);
    }

    @Override
    protected void onConnectionLost() {
        super.onConnectionLost();

        //go back to overview when connection lost
        Intent intent = new Intent(this, ContractOverviewActivity.class);
        startActivity(intent);
    }

    /**
     * loads the contract with the details provided in the start Intent and initializes the tab host
     *
     * @param contractAddress
     * @param type
     */
    private void init(String contractAddress, ContractType type)
    {
        getAppContext().getServiceProvider().getContractService().loadContract(type, contractAddress, getAppContext().getSettingProvider().getSelectedAccount())
                .done(new DoneCallback<ITradeContract>() {
                    @Override
                    public void onDone(final ITradeContract resolved) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                contract = resolved;
                                contract.addObserver(ContractDetailActivity.this);
                                initTabHost();
                            }
                        });
                    }
                });
    }


    private void initProfileFragment()
    {
        if(contract == null)
            return;

        if(this.detailFragment.needsIdentityVerification())
        {
            if(contract.getUserProfile().getVCard() != null)
            {
                profileFragment.setProfileInformation(contract.getUserProfile());
                profileFragment.setMode(ProfileFragment.ProfileMode.ReadOnly);
            }
        }
    }

    private void initDetailFragment()
    {
        if(contract == null)
            return;

        detailFragment.init(contract)
                .done(new DoneCallback<Void>() {
                    @Override
                    public void onDone(Void result) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //only show export button when the contract is in the created state and we are the seller
                                if(detailFragment.getSeller().equals(getAppContext().getSettingProvider().getSelectedAccount()) && detailFragment.getState().equals(ContractState.Created))
                                {
                                    exportButton.setVisibility(View.VISIBLE);
                                }else{
                                    exportButton.setVisibility(View.GONE);
                                }

                                //only show scan profile button when identity verification needed and profile of the contract is not set
                                if(detailFragment.needsIdentityVerification() && contract.getUserProfile().getVCard() == null)
                                {
                                    scanProfileButton.setVisibility(View.VISIBLE);
                                }else{
                                    scanProfileButton.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                });
    }

    private void initTabHost()
    {
        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        /** Defining Tab Change Listener event. This is invoked when tab is changed */
        TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {

                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                ft.detach(profileFragment);
                ft.detach(detailFragment);

                if(tabId.equalsIgnoreCase("Profile")){
                    ft.attach(profileFragment);
                }else{
                    ft.attach(detailFragment);
                }

                ft.commit();
                fm.executePendingTransactions();

                if(tabId.equalsIgnoreCase("Profile")){
                    initProfileFragment();
                }else{
                    initDetailFragment();
                }
            }
        };

        tabHost.setOnTabChangedListener(tabChangeListener);

        //set detail tab details
        TabHost.TabSpec spec = tabHost.newTabSpec("Details");
        spec.setContent(getFragmentManager().findFragmentByTag("Details").getId());
        spec.setIndicator("", getResources().getDrawable(R.drawable.ic_tab_general_info));
        tabHost.addTab(spec);

        //set profile tab details
        profileViewSpec = tabHost.newTabSpec("Profile");
        profileViewSpec.setContent(getFragmentManager().findFragmentByTag("Profile").getId());
        profileViewSpec.setIndicator("", getResources().getDrawable(R.drawable.ic_tab_contact_info));
        tabHost.addTab(profileViewSpec);

        //hack to get the details tab loaded at the beginning
        tabHost.setCurrentTab(1);
        tabHost.setCurrentTab(0);

        //remove profile tab if identity must not be verified of when the profile is not set on the contract
        if(!detailFragment.needsIdentityVerification() || contract.getUserProfile().getVCard() == null)
            removeProfileTab();
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

    /**
     * Starts the {@link QrScanningActivity} to scan a {@link UserProfile} of another party
     * @param view
     */
    public void onScanQrImageClick(View view)
    {
        PermissionProvider permissionProvider = getAppContext().getPermissionProvider();

        //check camera permission
        if(!permissionProvider.hasPermission(PermissionProvider.CAMERA))
        {
            permissionProvider.requestPermission(PermissionProvider.CAMERA);
            return;
        }

        startScanActivity();
    }

    private void startScanActivity()
    {
        Intent intent = new Intent(this, QrScanningActivity.class);
        intent.setAction(QrScanningActivity.ACTION_SCAN_PROFILE);
        startActivityForResult(
                intent,
                SCAN_PROFILE_INFO_REQUEST);
    }

    @Override
    protected void onPermissionGranted(String permission) {
        super.onPermissionGranted(permission);

        if(permission.equals(PermissionProvider.CAMERA))
        {
            startScanActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        switch (requestCode)
        {
            case SCAN_PROFILE_INFO_REQUEST:
                if(intent == null)
                    return;

                //Create a new UserProfile from the VCard received from the QrScanningActivity
                String data = intent.getStringExtra(QrScanningActivity.MESSAGE_PROFILE_DATA);
                UserProfile profile = new GsonSerializationService().deserialize(data, UserProfile.class);

                //Add profile tab
                tabHost.addTab(profileViewSpec);
                tabHost.setCurrentTabByTag("Profile");

                //Save the UserProfile for the contract
                contract.setUserProfile(profile);
                getAppContext().getServiceProvider().getContractService().saveContract(contract, getAppContext().getSettingProvider().getSelectedAccount());

                //init profile fragment
                profileFragment.setProfileInformation(profile);
                scanProfileButton.setVisibility(View.GONE);
                profileFragment.setMode(ProfileFragment.ProfileMode.ReadOnly);
                detailFragment.identityVerified();
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

    /**
     * Opens the {@link P2pExportDialog} to export the contract.
     *
     * @param view
     */
    public void onExportContractClick(View view)
    {
        DialogFragment exportFragment = new P2pExportDialog();
        Bundle args = new Bundle();
        args.putString(P2pExportDialog.MESSAGE_CONTRACT_DATA, contract.toJson());
        exportFragment.setArguments(args);
        exportFragment.show(getSupportFragmentManager(), "importDialogFragment");
    }

    /**
     * see {@link P2pExportDialog.P2pExportListener}
     *
     * @param buyerProfile
     */
    @Override
    public void onContractDataExchanged(UserProfile buyerProfile) {
        if(buyerProfile != null)
        {
            tabHost.addTab(profileViewSpec);
            tabHost.setCurrentTabByTag("Profile");
            contract.setUserProfile(buyerProfile);
            getAppContext().getServiceProvider().getContractService().saveContract(contract, getAppContext().getSettingProvider().getSelectedAccount());
            profileFragment.setProfileInformation(buyerProfile);
            profileFragment.setMode(ProfileFragment.ProfileMode.ReadOnly);
            detailFragment.identityVerified();
        }
    }

    @Override
    public void onContractDialogCanceled() {
    }

    /**
     * see {@link ProfileFragment.ProfileDataChangedListener}
     *
     * @param profile
     */
    @Override
    public void onProfileDataChanged(UserProfile profile)
    {
        //save UserProfile for contract
        String account = getAppContext().getSettingProvider().getSelectedAccount();
        contract.setUserProfile(profile);
        getAppContext().getServiceProvider().getContractService().saveContract(contract, account);
    }
}
