package ch.uzh.ifi.csg.smartcontract.app.detail.create;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import ch.uzh.ifi.csg.smartcontract.library.contract.ITradeContract;
import ch.uzh.ifi.csg.smartcontract.library.util.ImageHelper;
import ch.uzh.ifi.csg.smartcontract.library.contract.ContractType;
import ch.uzh.ifi.csg.smartcontract.app.common.ActivityBase;
import ch.uzh.ifi.csg.smartcontract.app.R;
import ch.uzh.ifi.csg.smartcontract.app.common.permission.PermissionProvider;
import ch.uzh.ifi.csg.smartcontract.app.overview.ContractOverviewActivity;

/**
 * Activity to define and deploy new {@link ITradeContract} instances. It creates a concrete
 * {@link ContractDeployFragment} depending on the {@link ContractType} provided in its create
 * Intent.
 */
public class ContractCreateActivity extends ActivityBase  {

    public static final String CONTRACT_TYPE_EXTRA = "ch.uzh.ifi.csg.smart_contract.type";

    private ContractDeployFragment deployFragment;

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

    @Override
    protected void onConnectionLost() {
        super.onConnectionLost();
        Intent intent = new Intent(this, ContractOverviewActivity.class);
        startActivity(intent);
    }

    private void addFragment(FragmentManager fragmentManager, Fragment fragment)
    {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.content, deployFragment, ContractDeployFragment.TAG);
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
    }

    /**
     * Executed after permissions for external storage or camera access have been granted by the
     * user
     *
     * @param permission
     */
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
