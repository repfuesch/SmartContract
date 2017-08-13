package ch.uzh.ifi.csg.smartcontract.app.account;

import android.app.DialogFragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import ch.uzh.ifi.csg.smartcontract.library.util.ImageHelper;
import ch.uzh.ifi.csg.smartcontract.app.R;
import ch.uzh.ifi.csg.smartcontract.app.common.ActivityBase;
import ch.uzh.ifi.csg.smartcontract.app.common.permission.PermissionProvider;

/**
 * Provides the UI to lock/unlock accounts and to create/import accounts into the application. The
 * main logic is contained in the {@link AccountFragment}
 */
public class AccountActivity extends ActivityBase implements AccountDialogFragment.AccountDialogListener {

    public final static String ACTION_ACCOUNT_CHANGED = "ch.uzh.ifi.csg.smart_contract.account_changed";
    public final static String MESSAGE_ACCOUNT_CHANGED = "ch.uzh.ifi.csg.smart_contract.account";

    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.title_account);
        accountFragment = (AccountFragment) getFragmentManager().findFragmentById(R.id.account_fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(getAppContext().getSettingProvider().useRemoteAccountManagement())
        {
            //hide add button when remote accounts are used
            findViewById(R.id.account_add_button).setVisibility(View.GONE);
        }else{
            //hide add button when remote accounts are used
            findViewById(R.id.account_add_button).setVisibility(View.VISIBLE);
        }

        accountFragment.reloadAccountList();
    }

    public void onAddButtonClick(View view)
    {
        DialogFragment dialogFragment = new AccountDialogFragment();
        dialogFragment.show(getFragmentManager(), "AccountDialog");
    }

    @Override
    protected void onPermissionGranted(String permission) {
        super.onPermissionGranted(permission);

        if(permission.equals(PermissionProvider.READ_STORAGE))
        {
            AccountDialogFragment dialog = (AccountDialogFragment)getFragmentManager().findFragmentByTag("AccountDialog");
            if(dialog == null)
                return;

            //try to open the file again when permission was granted
            ImageHelper.openFile(dialog);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_account;
    }

    @Override
    protected void onSettingsChanged() {
        accountFragment.reloadAccountList();
    }

    @Override
    public void onAccountCreated(String accountName, String password)
    {
        accountFragment.createAccount(accountName, password);
    }

    @Override
    public void onAccountImported(String accountName, String password, Uri walletFileUri) {
        accountFragment.importAccount(accountName, password, walletFileUri);
    }
}
