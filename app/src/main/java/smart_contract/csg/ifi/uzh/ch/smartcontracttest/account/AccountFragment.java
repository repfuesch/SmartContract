package smart_contract.csg.ifi.uzh.ch.smartcontracttest.account;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import org.jdeferred.Promise;

import java.util.ArrayList;
import java.util.List;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.datamodel.Account;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContext;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;

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

        // Set the adapter
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

        //reloadAccountList();
    }

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
                                    //messageService.showErrorMessage("An error occurred: " + rejected.getMessage());
                                    //todo:log
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

    public void createAccount(String accountName, String password)
    {
        BusyIndicator.show(accountView);
        appContext.getServiceProvider().getAccountService().createAccount(accountName, password)
                .always(new AlwaysCallback<Account>() {
                    @Override
                    public void onAlways(Promise.State state, final Account resolved, final Throwable rejected) {

                        if(rejected != null)
                        {
                            appContext.getMessageService().showErrorMessage("Could not create account.");
                        }else{
                            notifyAccountChanged(resolved);
                        }

                        BusyIndicator.hide(accountView);
                    }
                });
    }

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
                                resultListener.onLoginResult(false);
                                appContext.getMessageService().showErrorMessage("Could not unlock account.");
                                //todo:log
                            }
                            else if(!resolved)
                            {
                                resultListener.onLoginResult(false);
                                appContext.getMessageService().showErrorMessage("Unlocking account failed. Wrong password");
                            }else{
                                resultListener.onLoginResult(true);
                                notifyAccountChanged(account);
                            }
                            BusyIndicator.hide(accountView);
                        }
                    });
    }

    @Override
    public void onAccountLock()
    {
        BusyIndicator.show(accountView);
        notifyAccountChanged(null);
        appContext.getServiceProvider().getAccountService().lockAccount();
        BusyIndicator.hide(accountView);
    }

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
