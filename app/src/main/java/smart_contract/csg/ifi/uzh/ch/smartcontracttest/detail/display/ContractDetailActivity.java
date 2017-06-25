package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
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
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.qrcode.QrScanningActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile.ProfileFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.dialog.ContractExportDialog;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.dialog.ContractImportDialog;

public class ContractDetailActivity extends ActivityBase implements IContractObserver, ProfileFragment.OnProfileVerifiedListener, ContractExportDialog.ContractExportListener {

    public final static String EXTRA_CONTRACT_ADDRESS = "ch.uzh.ifi.csg.smart_contract.address";
    public final static String EXTRA_CONTRACT_TYPE = "ch.uzh.ifi.csg.smart_contract.type";

    private static final int SCAN_PROFILE_INFO_REQUEST = 1;

    private ContractDetailFragment detailFragment;
    private ProfileFragment contactFragment;
    private ITradeContract contract;

    private TabHost tabHost;
    private TabHost.TabSpec profileViewSpec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_contract_detail);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String contractAddress = intent.getStringExtra(EXTRA_CONTRACT_ADDRESS);
        ContractType contractType = (ContractType) intent.getSerializableExtra(EXTRA_CONTRACT_TYPE);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

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

        fragmentTransaction.add(android.R.id.tabcontent, detailFragment, "DetailFragment");
        fragmentTransaction.addToBackStack("DetailFragment");
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();

        contactFragment = (ProfileFragment) getFragmentManager().findFragmentById(R.id.fragment_contact_info);
        contactFragment.setMode(ProfileFragment.ProfileMode.ReadOnly);

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
                                detailFragment.init(resolved);
                                if(detailFragment.needsIdentityVerification())
                                {
                                    addProfileTab();
                                    if(!contract.getUserProfile().isVerified())
                                    {
                                        contactFragment.setMode(ProfileFragment.ProfileMode.Verify);
                                    }else{
                                        contactFragment.setProfileInformation(contract.getUserProfile());
                                        contactFragment.setMode(ProfileFragment.ProfileMode.ReadOnly);
                                    }
                                }else{
                                    removeProfileTab();
                                }

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
        spec.setContent(getFragmentManager().findFragmentByTag("DetailFragment").getId());
        spec.setIndicator("", getResources().getDrawable(R.drawable.ic_tab_general_info));
        tabHost.addTab(spec);

        //Tab2
        profileViewSpec = tabHost.newTabSpec("Contact");
        profileViewSpec.setIndicator("", getResources().getDrawable(R.drawable.ic_tab_contact_info));
        profileViewSpec.setContent(R.id.fragment_contact_info);
    }

    private void addProfileTab()
    {
        if(tabHost.getTabWidget().getTabCount() == 1)
            tabHost.addTab(profileViewSpec);
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
                addProfileTab();
                contactFragment.setProfileInformation(profile);
                tabHost.setCurrentTabByTag("Contact");
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
        tabHost.setCurrentTabByTag("General");
        contract.setUserProfile(profile);
        contactFragment.setMode(ProfileFragment.ProfileMode.ReadOnly);
        getServiceProvider().getContractService().saveContract(contract, getSettingProvider().getSelectedAccount());
        detailFragment.identityVerified();
    }

    public void onExportContractClick(View view)
    {
        DialogFragment exportFragment = new ContractExportDialog();
        Bundle args = new Bundle();
        args.putBoolean(ContractExportDialog.MESSAGE_IDENTIFICATION_USED, detailFragment.isVerifyIdentity());
        args.putSerializable(ContractExportDialog.MESSAGE_CONTRACT_DATA, new ContractInfo(contract.getContractType(), contract.getContractAddress(), new UserProfile(), contract.getImages()));
        exportFragment.setArguments(args);
        exportFragment.show(getSupportFragmentManager(), "importDialogFragment");
    }

    @Override
    public void onContractDataExchanged(UserProfile buyerProfile) {
        if(buyerProfile != null)
        {
            addProfileTab();
            contactFragment.setProfileInformation(buyerProfile);
            tabHost.setCurrentTabByTag("Contact");
        }
    }

    @Override
    public void onContractDialogCanceled() {
    }
}
