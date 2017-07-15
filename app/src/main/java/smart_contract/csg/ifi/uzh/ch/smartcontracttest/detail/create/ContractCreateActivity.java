package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.TabHost;

import ch.uzh.ifi.csg.contract.util.ImageHelper;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.permission.PermissionProvider;

public class ContractCreateActivity extends ActivityBase  {

    public static final String CONTRACT_TYPE_EXTRA = "ch.uzh.ifi.csg.smart_contract.type";
    public static final int SCAN_CONTRACT_INFO_REQUEST = 1;

    private ContractDeployFragment deployFragment;
    private TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_contract_create);

        FragmentManager fragmentManager = getFragmentManager();
        Fragment deploy = fragmentManager.findFragmentByTag(ContractDeployFragment.TAG);

        if(deploy != null)
        {
            deployFragment = (ContractDeployFragment) deploy;
            addFragment(fragmentManager, deployFragment);
            return;
        }

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

        addFragment(fragmentManager, deployFragment);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_contract_detail;
    }

    @Override
    protected void onSettingsChanged() {
    }

    private void addFragment(FragmentManager fragmentManager, Fragment fragment)
    {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.content, deployFragment, ContractDeployFragment.TAG);
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
    }

    @Override
    protected void onPermissionGranted(String permission) {
        super.onPermissionGranted(permission);

        if(permission.equals(PermissionProvider.READ_STORAGE))
        {
            ImageHelper.openFile(deployFragment);
        }else if(permission.equals(PermissionProvider.CAMERA))
        {
            ImageHelper.makePicture(deployFragment);
        }
    }
}
