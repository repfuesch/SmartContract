package smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.account.AccountActivity;
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
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.title_contract_overview);

        if(SettingsProvider.getInstance().getSelectedAccount().isEmpty())
        {
            //navigate to account activity when no account selected
            Intent accountIntent = new Intent(this, AccountActivity.class);
            startActivity(accountIntent);
        }else{
            loadContractList();
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_contract_overview;
    }

    public void onAddButtonClick(View view)
    {
        DialogFragment newFragment = new AddContractDialogFragment();
        newFragment.show(getSupportFragmentManager(), "loadContractFragment");
    }

    @Override
    protected void onContractCreated(String contractAddress) {
        super.onContractCreated(contractAddress);

        listFragment.loadContract(contractAddress);
    }

    @Override
    public void onAddContract(String contractAddress)
    {
        listFragment.loadContract(contractAddress);
    }

    @Override
    public void onCreateContract() {
        Intent intent = new Intent(this, ContractCreateActivity.class);
        startActivity(intent);
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
