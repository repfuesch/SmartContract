package smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import org.jdeferred.Promise;
import org.web3j.tx.Contract;

import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.account.AccountActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.qrcode.QrScanningActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create.ContractCreateActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display.ContractDetailActivity;

import static ch.uzh.ifi.csg.contract.contract.ContractType.*;

public class ContractOverviewActivity extends ActivityBase implements AddContractDialogFragment.AddContractDialogListener
{
    private static final int SCAN_CONTRACT_ADDRESS_REQUEST = 1;

    private ContractListFragment listFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.title_contract_overview);

        if(getSettingProvider().getSelectedAccount().isEmpty())
        {
            //navigate to account activity when no account selected
            Intent accountIntent = new Intent(this, AccountActivity.class);
            startActivity(accountIntent);
        }else
        {
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
    protected void onActivityResult(int requestCode, int resultCode, final Intent intent)
    {
        switch (requestCode) {
            case SCAN_CONTRACT_ADDRESS_REQUEST:
                if(intent == null)
                    return;

                final String contractAddress = intent.getStringExtra(QrScanningActivity.MESSAGE_CONTRACT_ADDRESS);
                ensureContract(contractAddress).then(new DoneCallback<Boolean>() {
                    @Override
                    public void onDone(Boolean result) {
                        final ContractType type = (ContractType) intent.getSerializableExtra(QrScanningActivity.MESSAGE_CONTRACT_TYPE);

                        listFragment.loadContract(type, contractAddress);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent detailIntent = new Intent(ContractOverviewActivity.this, ContractDetailActivity.class);
                                detailIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, contractAddress);
                                detailIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, type);
                                startActivity(detailIntent);
                            }
                        });
                    }
                });

                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    protected void onContractCreated(String contractAddress, ContractType type) {
        super.onContractCreated(contractAddress, type);

        listFragment.loadContract(type, contractAddress);
    }

    private SimplePromise<Boolean> ensureContract(final String address)
    {
        return getServiceProvider().getContractService().isContract(address)
                .always(new AlwaysCallback<Boolean>() {
                    @Override
                    public void onAlways(Promise.State state, Boolean resolved, Throwable rejected) {
                        if(rejected != null)
                        {
                            showMessage("Cannot add contract " + address + " because it is not found on the blockchain");
                        }
                    }
                });
    }

    @Override
    public void onAddContract(final String contractAddress, final ContractType type)
    {
        ensureContract(contractAddress).then(new DoneCallback<Boolean>() {
            @Override
            public void onDone(Boolean result) {
                listFragment.loadContract(type, contractAddress);
            }
        });
    }

    @Override
    public void onCreateContract(ContractType type)
    {
        Intent intent = new Intent(this, ContractCreateActivity.class);
        intent.putExtra(ContractCreateActivity.CONTRACT_TYPE_EXTRA, type);
        startActivity(intent);
    }

    @Override
    protected void onSettingsChanged()
    {
        loadContractList();
    }

    private void loadContractList()
    {
        listFragment = (ContractListFragment) getFragmentManager().findFragmentById(R.id.purchase_list_fragment);
        listFragment.loadContractsForAccount(getSettingProvider().getSelectedAccount());
    }

}
