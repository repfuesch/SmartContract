package smart_contract.csg.ifi.uzh.ch.smartcontracttest.account;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import org.jdeferred.Promise;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.datamodel.Account;
import ch.uzh.ifi.csg.contract.util.FileUtil;
import ch.uzh.ifi.csg.contract.util.ImageHelper;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.permission.PermissionProvider;

public class AccountActivity extends ActivityBase implements AccountDialogFragment.AccountDialogListener {

    public final static String ACTION_ACCOUNT_CHANGED = "ch.uzh.ifi.csg.smart_contract.account_changed";
    public final static String MESSAGE_ACCOUNT_CHANGED = "ch.uzh.ifi.csg.smart_contract.account";

    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.title_account);
        accountFragment = (AccountFragment) getFragmentManager().findFragmentById(R.id.account_fragment);
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
        String fileName = walletFileUri.getLastPathSegment();
        final File walletFile = new File(getAppContext().getSettingProvider().getWalletFileDirectory() + File.separator + fileName);

        try{
            FileUtil.copyInputStreamToFile(getContentResolver().openInputStream(walletFileUri), walletFile);
            getAppContext().getServiceProvider().getAccountService().importAccount(accountName, password, fileName)
                .always(new AlwaysCallback<Account>() {
                    @Override
                    public void onAlways(Promise.State state, Account resolved, Throwable rejected) {
                        if(rejected != null)
                        {
                            getAppContext().getMessageService().showErrorMessage("The account cannot be created. \n " +
                                    "Please make sure that the provided password matches and the provided file is a valid wallet file.");
                            walletFile.delete();
                        }
                    }
                });

        }catch(IOException e)
        {
            //todo:log
            getAppContext().getMessageService().showErrorMessage("Cannot find or open the provided wallet file");
        }
    }
}
