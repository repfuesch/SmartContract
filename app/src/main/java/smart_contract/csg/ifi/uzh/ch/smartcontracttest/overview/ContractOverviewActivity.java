package smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import org.jdeferred.Promise;

import java.util.List;

import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.FailCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.account.AccountActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.qrcode.QrScanningActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create.ContractCreateActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display.ContractDetailActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.dialog.P2pImportDialog;

public class ContractOverviewActivity extends ActivityBase implements AddContractDialogFragment.AddContractDialogListener, P2pImportDialog.P2pImportListener
{
    private static final int SCAN_CONTRACT_ADDRESS_REQUEST = 1;

    private ContractListFragment listFragment;
    private LinearLayout bodyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.title_contract_overview);

        if(getAppContext().getSettingProvider().getSelectedAccount().isEmpty())
        {
            //navigate to account activity when no account selected
            Intent accountIntent = new Intent(this, AccountActivity.class);
            startActivity(accountIntent);
        }else{
            bodyView = (LinearLayout)findViewById(R.id.overview_body);

            if(getAppContext().getServiceProvider().getConnectionService().hasConnection())
            {
                loadContractList();
            }
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_contract_overview;
    }

    public void onImportFromDeviceButtonClick(View view)
    {
        DialogFragment importFragment = new P2pImportDialog();
        importFragment.show(getSupportFragmentManager(), "importDialogFragment");
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
                        getAppContext().getServiceProvider().getContractService().saveContract(contractAddress, type, getAppContext().getSettingProvider().getSelectedAccount());
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
        return getAppContext().getServiceProvider().getContractService().isContract(address)
                .fail(new FailCallback() {
                    @Override
                    public void onFail(Throwable result) {
                        getAppContext().getMessageService().handleError(result);
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

    @Override
    protected void onConnectionLost() {
        super.onConnectionLost();

        bodyView.setEnabled(false);
    }

    @Override
    protected void onConnectionEstablished() {
        super.onConnectionEstablished();

        bodyView.setEnabled(true);
        loadContractList();
    }

    private void loadContractList()
    {
        listFragment = (ContractListFragment) getFragmentManager().findFragmentById(R.id.purchase_list_fragment);
        listFragment.loadContractsForAccount(getAppContext().getSettingProvider().getSelectedAccount());
    }

    @Override
    public void onContractDataReceived(final ContractInfo contractInfo)
    {
        ensureContract(contractInfo.getContractAddress()).then(new DoneCallback<Boolean>() {
            @Override
            public void onDone(Boolean result) {
                listFragment.loadContract(contractInfo.getContractType(), contractInfo.getContractAddress())
                        .always(new AlwaysCallback<ITradeContract>() {
                            @Override
                            public void onAlways(Promise.State state, ITradeContract resolved, Throwable rejected) {

                                //todo: make sure that Userprofile is not lost when contract cannot be loaded
                                if(resolved != null)
                                {
                                    resolved.setUserProfile(contractInfo.getUserProfile());
                                    for(String imgSig : contractInfo.getImages().keySet())
                                    {
                                        //copy the images into the correct application path
                                        resolved.addImage(imgSig, contractInfo.getImages().get(imgSig));
                                    }
                                    getAppContext().getServiceProvider().getContractService().saveContract(resolved, getAppContext().getSettingProvider().getSelectedAccount());
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void onContractDialogCanceled() {
    }
}
