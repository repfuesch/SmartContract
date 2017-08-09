package smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import org.jdeferred.Promise;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.contract.PurchaseContract;
import ch.uzh.ifi.csg.contract.contract.RentContract;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import ch.uzh.ifi.csg.contract.service.serialization.SerializationService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.list.ContractListFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.qrcode.QrScanningActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create.ContractCreateActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display.ContractDetailActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.dialog.P2pImportDialog;

/**
 * Activity that display a list of {@link ITradeContract} instances in its
 * {@link ContractListFragment}.
 * It contains logic to import contracts by scanning of QR-codes
 * ({@link #onScanButtonClick} and through Wi-Fi direct ({@link #onImportFromDeviceButtonClick}.
 * It also reacts to created contracts ({@link #onContractCreated} and adds them to the list.
 *
 */
public class ContractOverviewActivity extends ActivityBase implements AddContractDialogFragment.AddContractDialogListener, P2pImportDialog.P2pImportListener
{
    private static final int SCAN_CONTRACT_ADDRESS_REQUEST = 1;

    private ContractListFragment listFragment;
    private LinearLayout bodyView;
    private RelativeLayout overviewMenu;
    private TextView infoTextView;
    private LinearLayout contractListWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.title_contract_overview);

        bodyView = (LinearLayout)findViewById(R.id.overview_body);
        overviewMenu = (RelativeLayout)findViewById(R.id.overview_menu);
        infoTextView = (TextView)findViewById(R.id.overview_info_text);
        contractListWrapper = (LinearLayout)findViewById(R.id.contract_list_wrapper);


        if(getAppContext().getSettingProvider().getSelectedAccount().isEmpty())
        {
            //show not logged in message and disable interactions
            contractListWrapper.setVisibility(View.GONE);
            overviewMenu.setVisibility(View.GONE);
            infoTextView.setText("No account is unlocked");
            infoTextView.setVisibility(View.VISIBLE);
        }else if(!getAppContext().getServiceProvider().getConnectionService().hasConnection())
        {
            //show disconnected message and disable interactions
            contractListWrapper.setVisibility(View.GONE);
            overviewMenu.setVisibility(View.GONE);
            infoTextView.setText("No connection to host");
            infoTextView.setVisibility(View.VISIBLE);
        }
        else{
            loadContractList();
        }
    }

    /**
     * Makes the searchItem visible in the Menu and initializes the {@link SearchView}
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean result = super.onPrepareOptionsMenu(menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        searchItem.setVisible(true);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(listFragment != null)
                    listFragment.getListFilter().filter(query);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(listFragment != null)
                    listFragment.getListFilter().filter(newText);

                return true;
            }
        });

        return result;
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

                //deserialize scaned contract and store it
                SerializationService serializationService = new GsonSerializationService();
                final ContractInfo contractInfo = serializationService.deserialize(intent.getStringExtra(QrScanningActivity.MESSAGE_CONTRACT_DATA), ContractInfo.class);
                getAppContext().getServiceProvider().getContractService().saveContract(contractInfo, getAppContext().getSettingProvider().getSelectedAccount());

                //open the DetailActivity for the new contract
                Intent detailIntent = new Intent(ContractOverviewActivity.this, ContractDetailActivity.class);
                detailIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, contractInfo.getContractAddress());
                detailIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, contractInfo.getContractType());
                startActivity(detailIntent);
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    protected void onContractCreated(String contractAddress, ContractType type) {
        super.onContractCreated(contractAddress, type);

        listFragment.loadContract(type, contractAddress);
    }

    /**
     * checks that the Ethereum contract code at the specified address matches the binary for the
     * contract type.
     *
     * @param address
     * @param contractType
     * @return
     */
    private SimplePromise<Boolean> ensureContract(final String address, final ContractType contractType)
    {
        String code = null;
        if(contractType == ContractType.Purchase)
        {
            code = PurchaseContract.BINARY_FULL;
        }else{
            code = RentContract.BINARY_FULL;
        }

        return getAppContext().getServiceProvider().getContractService().verifyContractCode(address, code)
                .always(new AlwaysCallback<Boolean>() {
                    @Override
                    public void onAlways(Promise.State state, Boolean resolved, Throwable rejected) {
                        if(rejected != null || !resolved)
                        {
                            getAppContext().getMessageService().showErrorMessage("Could not verify the contract code. Please try again.");
                        }
                    }
                });
    }

    @Override
    public void onAddContract(final String contractAddress, final ContractType type)
    {
        //check if contract exists and has correct binary code
        ensureContract(contractAddress, type).then(new DoneCallback<Boolean>() {
            @Override
            public void onDone(Boolean result) {
                if(!result)
                    return;

                //store the contract before loading it
                ContractInfo info = new ContractInfo(type, contractAddress);
                getAppContext().getServiceProvider().getContractService().saveContract(info, getAppContext().getSettingProvider().getSelectedAccount());

                listFragment.loadContract(type, contractAddress)
                        .done(new DoneCallback<ITradeContract>() {
                            @Override
                            public void onDone(ITradeContract resolved) {
                                if(resolved != null)
                                {
                                    getAppContext().getServiceProvider().getContractService().saveContract(resolved, getAppContext().getSettingProvider().getSelectedAccount());
                                }
                            }
                        });
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

        this.recreate();
    }

    @Override
    protected void onConnectionEstablished() {
        super.onConnectionEstablished();

        this.recreate();
    }

    private void loadContractList()
    {
        listFragment = (ContractListFragment) getFragmentManager().findFragmentById(R.id.contract_list_fragment);
        listFragment.loadContractsForAccount(getAppContext().getSettingProvider().getSelectedAccount());
    }

    @Override
    public void onContractDataReceived(final ContractInfo contractInfo)
    {
        //store the contract before loading it
        getAppContext().getServiceProvider().getContractService().saveContract(contractInfo, getAppContext().getSettingProvider().getSelectedAccount());

        listFragment.loadContract(contractInfo.getContractType(), contractInfo.getContractAddress());
                /*
                .always(new AlwaysCallback<ITradeContract>() {
                    @Override
                    public void onAlways(Promise.State state, ITradeContract resolved, Throwable rejected) {
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
                });*/
    }

    @Override
    public void onContractDialogCanceled() {
    }
}
