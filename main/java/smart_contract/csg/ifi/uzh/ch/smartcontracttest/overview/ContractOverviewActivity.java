package smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.ContractCreateActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.account.LoginDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsProvider;

public class ContractOverviewActivity extends ActivityBase implements AddContractDialogFragment.AddContractDialogListener
{
    private PurchaseContractFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        if(SettingsProvider.getInstance().getSelectedAccount() == null)
        {
            DialogFragment loginDialogFragment = new LoginDialogFragment();
            loginDialogFragment.show(getSupportFragmentManager(), "loginDialogFragment");
        }else{
            loadContractList();
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_contract_overview;
    }

    public void onCreateContractButtonClick(View view)
    {
        Intent intent = new Intent(this, ContractCreateActivity.class);
        startActivity(intent);
    }

    public void onLoadContractButtonClick(View view)
    {
        DialogFragment newFragment = new AddContractDialogFragment();
        newFragment.show(getSupportFragmentManager(), "loadContractFragment");
    }

    @Override
    protected void onContractCreated(String contractAddress) {
        listFragment.loadContract(contractAddress);
    }

    @Override
    public void onAddContract(DialogFragment dialog, String contractAddress)
    {
        listFragment.loadContract(contractAddress);
    }

    @Override
    public void onContractDialogCanceled(DialogFragment dialog) {
    }

    @Override
    protected void onSettingsChanged()
    {
        loadContractList();
    }

    private void loadContractList()
    {
        listFragment = (PurchaseContractFragment) getFragmentManager().findFragmentById(R.id.purchase_list_fragment);
        listFragment.loadContractsForAccount(SettingsProvider.getInstance().getSelectedAccount());
    }

}
