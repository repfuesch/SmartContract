package smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.account.AccountActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.qrcode.QrScanningActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create.ContractCreateActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display.ContractDetailActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsProvider;

public class ContractOverviewActivity extends ActivityBase implements AddContractDialogFragment.AddContractDialogListener
{
    private static final int SCAN_CONTRACT_ADDRESS_REQUEST = 1;

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

            //if(ServiceProvider.getInstance().getConnectionService().hasConnection())
           // {
                loadContractList();
           // }
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

    public void onScanButtonClick(View view)
    {
        Intent intent = new Intent(this, QrScanningActivity.class);
        intent.setAction(QrScanningActivity.ACTION_SCAN_CONTRACT);
        startActivityForResult(
                intent,
                SCAN_CONTRACT_ADDRESS_REQUEST
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        switch (requestCode) {
            case SCAN_CONTRACT_ADDRESS_REQUEST:
                if(intent == null)
                    return;

                String contractAddress = intent.getStringExtra(QrScanningActivity.MESSAGE_SCAN_DATA);
                if(!ensureContract(contractAddress))
                    return;

                listFragment.loadContract(contractAddress);
                Intent detailIntent = new Intent(this, ContractDetailActivity.class);
                detailIntent.putExtra(ContractDetailActivity.MESSAGE_SHOW_CONTRACT_DETAILS, contractAddress);
                startActivity(detailIntent);
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    protected void onContractCreated(String contractAddress) {
        super.onContractCreated(contractAddress);

        listFragment.loadContract(contractAddress);
    }

    private boolean ensureContract(String address)
    {
        Boolean isContract = ServiceProvider.getInstance().getContractService().isContract(address).get();
        if(!isContract)
        {
            showMessage("Cannot add contract " + address + " because it is not found on the blockchain");
            return false;
        }

        return true;
    }

    @Override
    public void onAddContract(String contractAddress)
    {
        if(!ensureContract(contractAddress))
            return;

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
