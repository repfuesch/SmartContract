package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.jdeferred.Promise;

import java.io.Serializable;

import ch.uzh.ifi.csg.contract.account.ContractFileManager;
import ch.uzh.ifi.csg.contract.account.ContractInfo;
import ch.uzh.ifi.csg.contract.account.ContractManager;
import ch.uzh.ifi.csg.contract.async.broadcast.TransactionManager;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.login.LoginDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsProvider;

import static smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.AppContext.getContext;

/**
 * Base class for all Activities
 *
 * Created by flo on 13.03.17.
 *
 */

public abstract class ActivityBase extends AppCompatActivity implements ContractErrorHandler, ContractErrorDialogFragment.ExceptionConfirmedListener
{
    private Toolbar toolbar;

    private ContractBroadcastReceiver broadcastReceiver = null;
    boolean broadcastReceiverRegistered = false;

    protected Toolbar getToolbar()
    {
        return toolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        broadcastReceiver = new ContractBroadcastReceiver();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
    }

    protected abstract int getLayoutResourceId();

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login:
                // Show login dialog
                DialogFragment loginDialogFragment = new LoginDialogFragment();
                loginDialogFragment.show(getSupportFragmentManager(), "loginDialogFragment");
                return true;

            case R.id.action_settings:
                //show setting dialog
                /*
                DialogFragment settingDialogFragment = new SettingDialogFragment();
                settingDialogFragment.show(getSupportFragmentManager(), "settingDialogFragment");
                */
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void handleError(Throwable exception)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ContractErrorDialogFragment.EXCEPTION_MESSAGE, exception);
        showErrorDialog(bundle);
    }

    @Override
    public void showMessage(String message)
    {
        Bundle bundle = new Bundle();
        bundle.putString(ContractErrorDialogFragment.ERROR_MESSAGE, message);
        showErrorDialog(bundle);
    }

    private void showErrorDialog(Bundle bundle)
    {
        DialogFragment errorDialogFragment = new ContractErrorDialogFragment();
        errorDialogFragment.setArguments(bundle);
        errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
    }

    @Override
    public void onExceptionConfirmed(Throwable throwable)
    {
    }

    protected abstract void onSettingsChanged();

    @Override
    protected void onPause() {
        super.onPause();

        if (broadcastReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            broadcastReceiverRegistered = false;
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (!broadcastReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(TransactionManager.ACTION_HANDLE_TRANSACTION));
            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(SettingsProvider.ACTION_SETTINGS_CHANGED));
            broadcastReceiverRegistered = true;
        }
    }

    protected abstract void onContractCreated(String contractAddress);

    protected abstract void onContractUpdated(String contractAddress);

    protected void onContractTransactionError(Throwable throwable)
    {
        handleError(throwable);
    }

    /**
     * Inner class for receiving broadcast messages upon contract transactions
     */
    private class ContractBroadcastReceiver extends android.content.BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.getAction().equals(TransactionManager.ACTION_HANDLE_TRANSACTION))
            {
                Serializable error = intent.getSerializableExtra(TransactionManager.CONTRACT_TRANSACTION_ERROR);

                if(error != null)
                {
                    String contractAddress = intent.getStringExtra(TransactionManager.CONTRACT_TRANSACTION_ADDRESS);
                    onContractTransactionError((Throwable)error);
                    if (contractAddress != null)
                    {
                        //We update the view even tough the transaction failed.
                        onContractUpdated(contractAddress);
                    }
                    return;
                }

                if(intent.getStringExtra(TransactionManager.CONTRACT_TRANSACTION_TYPE).equals(TransactionManager.CONTRACT_TRANSACTION_DEPLOY))
                {
                    final String contractAddress = intent.getStringExtra(TransactionManager.CONTRACT_TRANSACTION_ADDRESS);

                    //Load contract and persist it, such that it is stored independent of the currently active activity
                    ServiceProvider.getInstance().getContractService().loadContract(contractAddress).always(new AlwaysCallback<IPurchaseContract>() {
                        @Override
                        public void onAlways(Promise.State state, IPurchaseContract resolved, Throwable rejected) {
                            if(rejected != null)
                            {
                                handleError(rejected);
                            }

                            ContractManager manager = new ContractFileManager(getContext().getFilesDir() + "/accounts");
                            manager.saveContract(new ContractInfo(ContractState.Created, contractAddress), SettingsProvider.getInstance().getSelectedAccount());
                            onContractCreated(contractAddress);
                        }
                    });

                    return;
                }

                if(intent.getStringExtra(TransactionManager.CONTRACT_TRANSACTION_TYPE).equals(TransactionManager.CONTRACT_TRANSACTION_UPDATE))
                {
                    String contractAddress = intent.getStringExtra(TransactionManager.CONTRACT_TRANSACTION_ADDRESS);
                    onContractUpdated(contractAddress);
                    return;
                }
            } else if(intent.getAction().equals(SettingsProvider.ACTION_SETTINGS_CHANGED))
            {
                onSettingsChanged();
            }
        }
    }
}
