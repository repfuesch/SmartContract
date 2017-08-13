package smart_contract.csg.ifi.uzh.ch.smartcontract.account;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import org.jdeferred.Promise;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.datamodel.Account;
import ch.uzh.ifi.csg.contract.service.account.AccountService;
import ch.uzh.ifi.csg.contract.util.FileUtil;
import smart_contract.csg.ifi.uzh.ch.smartcontract.R;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.BusyIndicator;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.broadcast.BroadCastService;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.provider.ApplicationContext;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.provider.ApplicationContextProvider;

/**
 * Displays a list of accounts in a {@link RecyclerView} instance. Implements methods
 * to load, unlock and import accounts.
 */
public class AccountFragment extends Fragment implements AccountRecyclerViewAdapter.OnAccountListener {

    private LinearLayout accountView;
    private RecyclerView accountList;
    private AccountRecyclerViewAdapter accountListAdapter;
    private List<Account> accounts;
    private ApplicationContext appContext;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AccountFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accounts = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_list, container, false);

        accountList = (RecyclerView) view.findViewById(R.id.account_list);
        accountView = (LinearLayout) view.findViewById(R.id.account_view);

        accountList.setLayoutManager(new LinearLayoutManager(accountList.getContext()));

        accountListAdapter = new AccountRecyclerViewAdapter(accounts, this, appContext.getSettingProvider());
        accountListAdapter.notifyDataSetChanged();
        accountList.setAdapter(accountListAdapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {

        attachContext(context);

        super.onAttach(context);
    }

    private void attachContext(Context context)
    {
        if(context instanceof ApplicationContextProvider)
        {
            appContext = ((ApplicationContextProvider) context).getAppContext();
        }else{
            throw new RuntimeException(context.toString() + " must implement ApplicationContext");
        }
    }

    @Override
    public void onAttach(Activity activity)
    {
        attachContext(activity);
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();

        reloadAccountList();
    }

    /**
     * Loads all accounts from the {@link AccountService} and updated the
     * {@link AccountRecyclerViewAdapter}
     */
    public void reloadAccountList()
    {
        appContext.getServiceProvider().getAccountService().getAccounts()
                .always(new AlwaysCallback<List<Account>>() {
                    @Override
                    public void onAlways(Promise.State state, final List<Account> resolved, final Throwable rejected) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(rejected != null){
                                    Log.e("account", "Can not load account list", rejected);
                                }else{
                                    accounts.clear();
                                    accounts.addAll(resolved);
                                    accountListAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                });
    }

    /**
     * Creates a new {@link Account} using the {@link AccountService}
     *
     * @param accountName
     * @param password
     */
    public void createAccount(String accountName, String password)
    {
        BusyIndicator.show(accountView);
        appContext.getServiceProvider().getAccountService().createAccount(accountName, password)
                .always(new AlwaysCallback<Account>() {
                    @Override
                    public void onAlways(Promise.State state, final Account resolved, final Throwable rejected) {

                        if(rejected != null)
                        {
                            Log.e("account", "Cannot create account", rejected);
                            appContext.getMessageService().showErrorMessage("Could not create account.");
                        }else{
                            notifyAccountChanged(resolved);
                        }

                        BusyIndicator.hide(accountView);
                    }
                });
    }

    /**
     * Imports an account from the specified walletFileUri using the
     * {@link AccountService#importAccount} method.
     *
     * @param accountName
     * @param password
     * @param walletFileUri
     */
    public void importAccount(final String accountName, final String password, Uri walletFileUri)
    {
        final String fileName = walletFileUri.getLastPathSegment();

        //create new wallet file in internal storage
        final File walletFile = new File(appContext.getSettingProvider().getWalletFileDirectory() + File.separator + fileName);
        try{
            final InputStream is = getActivity().getContentResolver().openInputStream(walletFileUri);
            FileUtil.copyInputStreamToFile(is, walletFile);

            BusyIndicator.show(accountView);
            appContext.getServiceProvider().getAccountService().importAccount(accountName, password, fileName)
                    .always(new AlwaysCallback<Account>() {
                        @Override
                        public void onAlways(Promise.State state, Account resolved, Throwable rejected) {
                            if(rejected != null)
                            {
                                //handle wrong password or wrong wallet files
                                appContext.getMessageService().showErrorMessage("The account cannot be created. \n " +
                                        "Please make sure that the provided password matches and the provided file is a valid wallet file.");
                                walletFile.delete();
                            }else{
                                notifyAccountChanged(resolved);
                            }

                            BusyIndicator.hide(accountView);
                        }
                    });

        }catch(final IOException e)
        {
            //handle error when wallet file does not exist
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("account", "Cannot find or open the provided wallet file", e);
                    appContext.getMessageService().showErrorMessage("Cannot find or open the provided wallet file");
                }
            });
        }
    }

    /**
     * Attempts to unlock an account using the {@link AccountService#unlockAccount} method. Informs
     * the callback about the result of the operation.
     *
     * see {@link AccountRecyclerViewAdapter.OnAccountListener#onAccountLogin}
     *
     * @param account
     * @param password
     * @param resultListener
     */
    @Override
    public void onAccountLogin(final Account account, final String password, final AccountRecyclerViewAdapter.OnAccountLoginResultListener resultListener)
    {
        BusyIndicator.show(accountView);
        appContext.getServiceProvider().getAccountService().unlockAccount(account, password)
                .always(new AlwaysCallback<Boolean>() {
                    @Override
                    public void onAlways(Promise.State state, final Boolean resolved, final Throwable rejected) {

                            if(rejected != null)
                            {
                                Log.e("account", "Could not unlock account", rejected);
                                resultListener.onLoginResult(false);
                                appContext.getMessageService().showErrorMessage("Could not unlock account.");
                            }
                            else if(!resolved)
                            {
                                resultListener.onLoginResult(false);
                                appContext.getMessageService().showErrorMessage("Unlocking of account failed. Wrong password");
                            }else{
                                resultListener.onLoginResult(true);
                                notifyAccountChanged(account);
                            }
                            BusyIndicator.hide(accountView);
                        }
                    });
    }

    /**
     *  see {@link AccountRecyclerViewAdapter.OnAccountListener#onAccountLock}
     */
    @Override
    public void onAccountLock()
    {
        BusyIndicator.show(accountView);
        notifyAccountChanged(null);
        appContext.getServiceProvider().getAccountService().lockAccount();
        BusyIndicator.hide(accountView);
    }

    /**
     * Uses the {@link BroadCastService} to inform other components when an account was unlocked
     * @param account
     */
    private void notifyAccountChanged(Account account)
    {
        Intent intent = new Intent();
        intent.setAction(AccountActivity.ACTION_ACCOUNT_CHANGED);
        if(account == null)
        {
            //set empty account id
            intent.putExtra(AccountActivity. MESSAGE_ACCOUNT_CHANGED, "");
        }else{
            intent.putExtra(AccountActivity. MESSAGE_ACCOUNT_CHANGED, account.getId());
        }

        appContext.getBroadCastService().sendBroadcast(intent);
    }
}
