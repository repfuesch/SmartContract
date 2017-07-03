package smart_contract.csg.ifi.uzh.ch.smartcontracttest.account;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;

public class AccountActivity extends ActivityBase implements AccountCreateDialogFragment.AccountCreateListener{

    public final static String ACTION_ACCOUNT_CHANGED = "ch.uzh.ifi.csg.smart_contract.account_changed";
    public final static String MESSAGE_ACCOUNT_CHANGED = "ch.uzh.ifi.csg.smart_contract.account";

    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.title_account);
        accountFragment = (AccountFragment) getFragmentManager().findFragmentById(R.id.account_fragment);
    }

    public void onAddButtonClick(View view)
    {
        DialogFragment dialogFragment = new AccountCreateDialogFragment();
        dialogFragment.show(getFragmentManager(), "accountDialog");
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
    public void onAccountCreate(String accountName, String password)
    {
        accountFragment.createAccount(accountName, password);
    }
}
