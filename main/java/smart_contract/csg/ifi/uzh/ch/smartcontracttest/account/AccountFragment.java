package smart_contract.csg.ifi.uzh.ch.smartcontracttest.account;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import org.jdeferred.Promise;
import java.util.List;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.service.account.Account;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.MessageHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ServiceProvider;

public class AccountFragment extends Fragment implements AccountRecyclerViewAdapter.OnAccountLoginListener {

    private int mColumnCount = 1;
    private LinearLayout progressView;
    private LinearLayout accountView;
    private RecyclerView accountList;
    private AccountRecyclerViewAdapter accountListAdapter;
    private List<Account> accounts;
    private MessageHandler messageHandler;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AccountFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.accounts = ServiceProvider.getInstance().getAccountService().getAccounts().get();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_list, container, false);

        progressView = (LinearLayout) view.findViewById(R.id.progress_view);
        accountList = (RecyclerView) view.findViewById(R.id.account_list);
        accountView = (LinearLayout) view.findViewById(R.id.account_view);

        // Set the adapter
        if (mColumnCount <= 1) {
            accountList.setLayoutManager(new LinearLayoutManager(accountList.getContext()));
        } else {
            accountList.setLayoutManager(new GridLayoutManager(accountList.getContext(), mColumnCount));
        }

        accountListAdapter = new AccountRecyclerViewAdapter(accounts, this);
        accountListAdapter.notifyDataSetChanged();
        accountList.setAdapter(accountListAdapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {

        if(context instanceof MessageHandler)
        {
            messageHandler = (MessageHandler) context;
        }else{
            throw new RuntimeException(context.toString() + " must implement MessageHandler");
        }

        super.onAttach(context);
    }

    public void reloadAccountList()
    {
        accountListAdapter.notifyDataSetChanged();
    }

    public void createAccount(String accountName, String password)
    {
        showProgressView();
        ServiceProvider.getInstance().getAccountService().createAccount(accountName, password)
                .always(new AlwaysCallback<Account>() {
                    @Override
                    public void onAlways(Promise.State state, final Account resolved, final Throwable rejected) {

                        if(rejected != null)
                        {
                            notifyError("Could not create account. Reason: \n " + rejected.getMessage());
                        }else{
                            notifyAccountChanged(resolved);
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressView();
                            }
                        });
                    }
                });
    }

    private void showProgressView()
    {
        accountView.setVisibility(View.GONE);
        progressView.setVisibility(View.VISIBLE);
    }

    private void hideProgressView()
    {
        progressView.setVisibility(View.GONE);
        accountView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAccountLogin(final Account account, final String password, final AccountRecyclerViewAdapter.OnAccountLoginResultListener resultListener)
    {
        showProgressView();

        ServiceProvider.getInstance().getAccountService().unlockAccount(account, password)
                .always(new AlwaysCallback<Boolean>() {
                    @Override
                    public void onAlways(Promise.State state, final Boolean resolved, final Throwable rejected) {

                            if(rejected != null)
                            {
                                resultListener.onLoginResult(false);
                                notifyError("Could not unlock account. Reason: \n " + rejected.getMessage());
                            }
                            else if(!resolved)
                            {
                                resultListener.onLoginResult(false);
                                notifyError("Unlocking account failed. Wrong password");
                            }else{
                                resultListener.onLoginResult(true);
                                notifyAccountChanged(account);
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hideProgressView();
                                }
                            });
                        }
                    });
    }

    private void notifyAccountChanged(Account account)
    {
        Intent intent = new Intent();
        intent.setAction(AccountActivity.ACTION_ACCOUNT_CHANGED);
        intent.putExtra(AccountActivity. MESSAGE_ACCOUNT_CHANGED, account.getId());
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

    private void notifyError(String message)
    {
        Intent intent = new Intent();
        intent.setAction(MessageHandler.ACTION_SHOW_ERROR);
        intent.putExtra(MessageHandler.MESSAGE_SHOW_ERROR, message);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
    }

}
