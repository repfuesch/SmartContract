package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.jdeferred.Promise;
import java.math.BigInteger;
import java.math.MathContext;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.common.Web3Util;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.service.connection.EthConnectionService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.SettingProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionManager;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionManagerImpl;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.account.AccountActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.dialog.MessageDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.EthSettingProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.ContractOverviewActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile.ProfileActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.WifiManager;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service.P2PBuyerService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service.P2PSellerService;

/**
 * Base class for all Activities
 *
 * Created by flo on 13.03.17.
 *
 */
public abstract class ActivityBase extends AppCompatActivity implements ApplicationContextProvider, MessageHandler, MessageDialogFragment.ExceptionConfirmedListener {
    private Toolbar toolbar;
    private LinearLayout accountBalanceView;
    private TextView accountBalanceField;

    private AppContext appContext;
    private Class callingActivityClass;
    private ContractBroadcastReceiver broadcastReceiver = null;
    private IntentFilter contractIntentFilter;
    boolean broadcastReceiverRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appContext = (AppContext) getApplication();
        setContentView(getLayoutResourceId());
        broadcastReceiver = new ContractBroadcastReceiver();

        initIntentFilters();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.drawable.ic_eth_logo_big);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        accountBalanceView = (LinearLayout) findViewById(R.id.account_balance_view);
        accountBalanceField = (TextView) accountBalanceView.findViewById(R.id.account_balance_field);

        if (getIntent().getSerializableExtra("from") != null)
            callingActivityClass = (Class) getIntent().getSerializableExtra("from");

        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(appContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);

        }

        if (ContextCompat.checkSelfPermission(appContext,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);

        }

        if (ContextCompat.checkSelfPermission(appContext,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    0x000000);
        }
    }

    protected final LinearLayout getBalanceView() {
        return this.accountBalanceView;
    }

    protected abstract int getLayoutResourceId();

    public final ServiceProvider getServiceProvider() {
        return appContext.getServiceProvider();
    }

    public final SettingProvider getSettingProvider() {
        return appContext.getSettingProvider();
    }

    public final TransactionManager getTransactionManager() {
        return appContext.getTransactionManager();
    }

    @Override
    public P2PSellerService getP2PSellerService() {
        return appContext.getP2PSellerService();
    }

    @Override
    public P2PBuyerService getP2PBuyerService() {
        return appContext.getP2PBuyerService();
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected void startActivity(Activity callingActivity, Class nextActivity) {
        Intent intent = new Intent(this, nextActivity);
        intent.putExtra("from", callingActivity.getClass());
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (callingActivityClass != null) {
            startActivity(this, callingActivityClass);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login:
                // Show account activity
                Intent accountIntent = new Intent(this, AccountActivity.class);
                startActivity(accountIntent);
                return true;

            case R.id.action_settings:
                //show setting activity
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingIntent);
                return true;

            case R.id.action_home:
                //show overview activity
                Intent overviewIntent = new Intent(this, ContractOverviewActivity.class);
                startActivity(overviewIntent);
                return true;

            case R.id.action_profile:
                //show profile activity
                Intent profileIntent = new Intent(this, ProfileActivity.class);
                startActivity(profileIntent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void handleError(Throwable exception) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MessageDialogFragment.EXCEPTION_MESSAGE, exception);
        showMessageDialog(bundle);
    }

    @Override
    public void showErrorMessage(String message) {
        Bundle bundle = new Bundle();
        bundle.putString(MessageDialogFragment.ERROR_MESSAGE, message);
        showMessageDialog(bundle);
    }

    @Override
    public void showMessage(String message) {
        Bundle bundle = new Bundle();
        bundle.putString(MessageDialogFragment.MESSAGE, message);
        showMessageDialog(bundle);
    }

    @Override
    public void showSnackBarMessage(String message, int length) {
        Snackbar.make(this.findViewById(android.R.id.content), message,
                length)
                .show();
    }

    private void showMessageDialog(final Bundle bundle) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogFragment errorDialogFragment = new MessageDialogFragment();
                errorDialogFragment.setArguments(bundle);
                errorDialogFragment.show(getSupportFragmentManager(), "messageDialogFragment");
            }
        });
    }


    private void updateAccountBalance() {
        String selectedAccount = getSettingProvider().getSelectedAccount();
        if (!selectedAccount.isEmpty()) {
            appContext.getServiceProvider().getAccountService().getAccountBalance(selectedAccount)
                    .always(new AlwaysCallback<BigInteger>() {
                        @Override
                        public void onAlways(Promise.State state, final BigInteger resolved, Throwable rejected) {
                            if (rejected != null) {
                                handleError(rejected);
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        accountBalanceField.setText(Web3Util.toEther(resolved).round(MathContext.DECIMAL32).toString());
                                    }
                                });
                            }
                        }
                    });
        }
    }

    @Override
    public void onExceptionConfirmed(Throwable throwable) {
    }

    protected void onConnectionLost() {
        showSnackBarMessage("Lost connection to host", Snackbar.LENGTH_LONG);
    }

    protected void onConnectionEstablished() {
        showSnackBarMessage("Established connection to host", Snackbar.LENGTH_LONG);
    }

    protected abstract void onSettingsChanged();

    private void initIntentFilters() {
        contractIntentFilter = new IntentFilter();
        contractIntentFilter.addAction(TransactionManager.ACTION_HANDLE_TRANSACTION);
        contractIntentFilter.addAction(EthSettingProvider.ACTION_SETTINGS_CHANGED);
        contractIntentFilter.addAction(MessageHandler.ACTION_SHOW_ERROR);
        contractIntentFilter.addAction(AccountActivity.ACTION_ACCOUNT_CHANGED);
        contractIntentFilter.addAction(EthConnectionService.ACTION_HANDLE_CONNECTION_DOWN);
        contractIntentFilter.addAction(EthConnectionService.ACTION_HANDLE_CONNECTION_UP);
    }

    @Override
    protected void onPause() {
        super.onPause();

        appContext.onActivityStopped(this);
        if (broadcastReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            broadcastReceiverRegistered = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        appContext.onActivityResumed(this);
        if (!broadcastReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, contractIntentFilter);
            broadcastReceiverRegistered = true;
        }

        updateAccountBalance();
    }

    protected void onContractCreated(String contractAddress, ContractType type) {
        showSnackBarMessage(getString(R.string.contract_created), Snackbar.LENGTH_LONG);
    }

    /**
     * Inner class for receiving broadcast messages upon contract transactions
     */
    private class ContractBroadcastReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TransactionManagerImpl.ACTION_HANDLE_TRANSACTION)) {
                if (intent.getStringExtra(TransactionManagerImpl.CONTRACT_TRANSACTION_TYPE).equals(TransactionManagerImpl.CONTRACT_TRANSACTION_DEPLOY)) {
                    //Load contract and persist it, such that it is stored independent of the currently active activity
                    final String contractAddress = intent.getStringExtra(TransactionManager.CONTRACT_ADDRESS);
                    final ContractType contractType = (ContractType) intent.getSerializableExtra(TransactionManager.CONTRACT_TYPE);
                    onContractCreated(contractAddress, contractType);
                    return;
                }

            } else if (intent.getAction().equals(EthSettingProvider.ACTION_SETTINGS_CHANGED)) {
                onSettingsChanged();

            } else if (intent.getAction().equals(AccountActivity.ACTION_ACCOUNT_CHANGED)) {
                showSnackBarMessage(getString(R.string.message_account_changed), Snackbar.LENGTH_LONG);
            } else if (intent.getAction().equals(MessageHandler.ACTION_SHOW_ERROR)) {
                showErrorMessage(intent.getStringExtra(MessageHandler.MESSAGE_SHOW_ERROR));
            } else if (intent.getAction().equals(EthConnectionService.ACTION_HANDLE_CONNECTION_DOWN)) {
                onConnectionLost();
            } else if (intent.getAction().equals(EthConnectionService.ACTION_HANDLE_CONNECTION_UP)) {
                onConnectionEstablished();
            }

            updateAccountBalance();
        }
    }
}
