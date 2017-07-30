package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigInteger;
import java.math.MathContext;

import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.util.Web3Util;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.service.connection.EthConnectionService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.permission.PermissionProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContext;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.setting.SettingProviderImpl;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionHandlerImpl;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.account.AccountActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.ContractOverviewActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile.ProfileActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsActivity;

/**
 * Base class for all Activities
 *
 * Created by flo on 13.03.17.
 *
 */
public abstract class ActivityBase extends AppCompatActivity implements ApplicationContextProvider {
    private Toolbar toolbar;
    private LinearLayout accountBalanceView;
    private TextView accountBalanceField;

    private ApplicationContext appContext;
    private Class callingActivityClass;
    private ContractBroadcastReceiver broadcastReceiver = null;
    private IntentFilter contractIntentFilter;
    private boolean broadcastReceiverRegistered = false;

    private String requestedPermission;
    private boolean permissionGranted;
    private String permissionRationale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appContext = (ApplicationContext) getApplication();
        setContentView(getLayoutResourceId());
        broadcastReceiver = new ContractBroadcastReceiver();

        initIntentFilters();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.drawable.ic_eth_logo_big);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        accountBalanceView = (LinearLayout) findViewById(R.id.account_balance_view);
        if(accountBalanceView != null)
            accountBalanceField = (TextView) accountBalanceView.findViewById(R.id.account_balance_field);

        if (getIntent().getSerializableExtra("from") != null)
            callingActivityClass = (Class) getIntent().getSerializableExtra("from");
    }

    protected final LinearLayout getBalanceView() {
        return this.accountBalanceView;
    }

    protected abstract int getLayoutResourceId();

    public ApplicationContext getAppContext() { return appContext; }

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        String permission = permissions[0];
        requestedPermission = permission;
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            permissionGranted = true;
            return;
        }

        permissionGranted = false;
        if (permission.equals(PermissionProvider.CAMERA)) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, PermissionProvider.CAMERA)) {
                permissionRationale = "Camera permission is needed for this feature";
                return;
            }
        }

        if (permission.equals(PermissionProvider.READ_STORAGE)) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, PermissionProvider.CAMERA)) {
                permissionRationale = "The app needs to access the file system for this feature";
                return;
            }
            return;
        }
    }

    private void updateAccountBalance() {
        String selectedAccount = appContext.getSettingProvider().getSelectedAccount();
        if (!selectedAccount.isEmpty()) {
            appContext.getServiceProvider().getAccountService().getAccountBalance(selectedAccount)
                .done(new DoneCallback<BigInteger>() {
                    @Override
                    public void onDone(final BigInteger resolved) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(accountBalanceField != null)
                                    accountBalanceField.setText(Web3Util.toEther(resolved).round(MathContext.DECIMAL32).toString());
                            }
                        });
                    }
                });
        }
    }

    protected void onConnectionLost() {
        appContext.getMessageService().showSnackBarMessage("Lost connection to host", Snackbar.LENGTH_LONG);
    }

    protected void onConnectionEstablished() {
        appContext.getMessageService().showSnackBarMessage("Established connection to host", Snackbar.LENGTH_LONG);
    }

    protected abstract void onSettingsChanged();

    private void initIntentFilters() {
        contractIntentFilter = new IntentFilter();
        contractIntentFilter.addAction(TransactionHandler.ACTION_TRANSACTION_CREATED);
        contractIntentFilter.addAction(SettingProviderImpl.ACTION_SETTINGS_CHANGED);
        contractIntentFilter.addAction(AccountActivity.ACTION_ACCOUNT_CHANGED);
        contractIntentFilter.addAction(EthConnectionService.ACTION_HANDLE_CONNECTION_DOWN);
        contractIntentFilter.addAction(EthConnectionService.ACTION_HANDLE_CONNECTION_UP);
    }

    @Override
    protected void onPause() {
        super.onPause();

        appContext.onActivityStopped(this);
        if (broadcastReceiverRegistered) {
            appContext.getBroadCastService().unregisterReceiver(broadcastReceiver);
            broadcastReceiverRegistered = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        appContext.onActivityResumed(this);
        if (!broadcastReceiverRegistered) {
            appContext.getBroadCastService().registerReceiver(broadcastReceiver, contractIntentFilter);
            broadcastReceiverRegistered = true;
        }

        if(accountBalanceView != null)
            updateAccountBalance();

        if(requestedPermission != null)
        {
            if(permissionGranted)
            {
                onPermissionGranted(requestedPermission);
            }else{
                onPermissionDenied(requestedPermission);
            }

            requestedPermission = null;
            permissionRationale = null;
        }
    }

    protected void onPermissionGranted(String permission) {}

    protected void onPermissionDenied(String permission)
    {
        if(permissionRationale != null)
            appContext.getMessageService().showSnackBarMessage(permissionRationale, Snackbar.LENGTH_LONG);
    }

    protected void onContractCreated(String contractAddress, ContractType type) {
        appContext.getMessageService().showSnackBarMessage(getString(R.string.contract_created), Snackbar.LENGTH_LONG);
    }

    /**
     * Inner class for receiving broadcast messages upon contract transactions
     */
    private class ContractBroadcastReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TransactionHandlerImpl.ACTION_TRANSACTION_CREATED)) {
                final String contractAddress = intent.getStringExtra(TransactionHandler.CONTRACT_ADDRESS);
                final ContractType contractType = (ContractType) intent.getSerializableExtra(TransactionHandler.CONTRACT_TYPE);
                onContractCreated(contractAddress, contractType);
                return;
            } else if (intent.getAction().equals(SettingProviderImpl.ACTION_SETTINGS_CHANGED)) {
                onSettingsChanged();
            } else if (intent.getAction().equals(AccountActivity.ACTION_ACCOUNT_CHANGED)) {
                if(appContext.getSettingProvider().getSelectedAccount().isEmpty())
                {
                    appContext.getMessageService().showSnackBarMessage(getString(R.string.message_account_locked), Snackbar.LENGTH_LONG);
                }else{
                    appContext.getMessageService().showSnackBarMessage(getString(R.string.message_account_unlocked), Snackbar.LENGTH_LONG);
                }
            } else if (intent.getAction().equals(EthConnectionService.ACTION_HANDLE_CONNECTION_DOWN)) {
                onConnectionLost();
            } else if (intent.getAction().equals(EthConnectionService.ACTION_HANDLE_CONNECTION_UP)) {
                onConnectionEstablished();
            }

            if(accountBalanceView != null)
                updateAccountBalance();
        }
    }
}
