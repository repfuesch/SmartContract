package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.contract.IContractObserver;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ezvcard.Ezvcard;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.qrcode.QrScanningActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile.ProfileFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.dialog.P2pExportDialog;

public class ContractDetailActivity extends ActivityBase implements IContractObserver, P2pExportDialog.P2pExportListener {

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
    private LinearLayout tabContentWrapper;
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

        //add fragments
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

        tabContentWrapper = (LinearLayout)findViewById(R.id.tabcontent_wrapper);
        exportButton = (ImageButton)findViewById(R.id.action_export_contract);
        scanProfileButton = (ImageButton)findViewById(R.id.action_scan_profile);

        Intent intent = getIntent();
        contractAddress = intent.getStringExtra(EXTRA_CONTRACT_ADDRESS);
        contractType = (ContractType) intent.getSerializableExtra(EXTRA_CONTRACT_TYPE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        contract.removeObserver(this);
    }

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
                                if(detailFragment.getSeller().equals(getAppContext().getSettingProvider().getSelectedAccount()) && detailFragment.getState().equals(ContractState.Created))
                                {
                                    exportButton.setVisibility(View.VISIBLE);
                                }else{
                                    exportButton.setVisibility(View.GONE);
                                }

                                if(detailFragment.needsIdentityVerification() && contract.getUserProfile().getVCard() == null)
                                {
                                    scanProfileButton.setVisibility(View.VISIBLE);
                                    getAppContext().getMessageService().showMessage("You must first scan the user profile of your contract partner to interact with the contract!");
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

        //hack to get details Tab loaded at the beginning
        tabHost.setCurrentTab(1);
        tabHost.setCurrentTab(0);

        if(!detailFragment.needsIdentityVerification() || contract.getUserProfile().getVCard() == null)
            removeProfileTab();
    }

    private View getTabIndicator(Context context, String title, int icon, String viewTag) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
        ImageView iv = (ImageView) view.findViewById(R.id.image_view);
        iv.setImageResource(icon);
        TextView tv = (TextView) view.findViewById(R.id.text_view);
        tv.setText(title);
        tv.setTag(viewTag);
        return view;
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
                tabHost.addTab(profileViewSpec);
                tabHost.setCurrentTabByTag("Profile");

                profileFragment.setProfileInformation(profile);
                scanProfileButton.setVisibility(View.GONE);
                contract.setUserProfile(profile);
                profileFragment.setMode(ProfileFragment.ProfileMode.ReadOnly);
                getAppContext().getServiceProvider().getContractService().saveContract(contract, getAppContext().getSettingProvider().getSelectedAccount());
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

    public void onExportContractClick(View view)
    {
        DialogFragment exportFragment = new P2pExportDialog();
        Bundle args = new Bundle();
        args.putBoolean(P2pExportDialog.MESSAGE_IDENTIFICATION_USED, detailFragment.needsIdentityVerification());
        args.putSerializable(P2pExportDialog.MESSAGE_CONTRACT_DATA, new ContractInfo(contract.getContractType(), contract.getContractAddress(), new UserProfile(), contract.getImages()));
        exportFragment.setArguments(args);
        exportFragment.show(getSupportFragmentManager(), "importDialogFragment");
    }

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
}
