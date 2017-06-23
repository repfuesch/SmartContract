package smart_contract.csg.ifi.uzh.ch.smartcontracttest.account;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiBuyerCallback;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiResponse;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiSellerCallback;

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

    public void onRequestContractDataClick(View view)
    {
        getP2PBuyerService().connect(new WifiBuyerCallback() {
            @Override
            public UserProfile getUserProfile() {
                showMessage("user profile submitted!");
                return new UserProfile();
            }

            @Override
            public void onUserProfileReceived(UserProfile data) {
                UserProfile profile = data;
                showMessage("user profile received!");
            }

            @Override
            public void onContractInfoReceived(ContractInfo contractInfo) {
                ContractInfo info = contractInfo;
                showMessage("Contract info received!");
            }

            @Override
            public void onWifiResponse(WifiResponse response) {
                String reason = response.getReasonPhrase();
                showMessage("Wifi response: " + reason);
            }
        });
    }

    public void onSendContractDataClick(View view)
    {
        getP2PSellerService().connect(new WifiSellerCallback() {
            @Override
            public UserProfile getUserProfile() {
                showMessage("retrieving user profile");
                return new UserProfile();
            }

            @Override
            public ContractInfo getContractInfo() {
                showMessage("restrieving contract info");
                return new ContractInfo(ContractType.Purchase, "ausdfgbhiaudgflidaugfdsaiugfbdaiugfhbidsagbdsig");
            }

            @Override
            public void onUserProfileReceived(UserProfile data) {
                UserProfile profile = data;
                showMessage("received user profile");
            }

            @Override
            public void onWifiResponse(WifiResponse response) {
                String reason = response.getReasonPhrase();
                showMessage("Wifi response: " + reason);
            }
        }, true);
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
