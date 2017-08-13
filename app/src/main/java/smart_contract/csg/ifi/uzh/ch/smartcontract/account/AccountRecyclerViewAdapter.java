package smart_contract.csg.ifi.uzh.ch.smartcontract.account;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import ch.uzh.ifi.csg.contract.datamodel.Account;
import smart_contract.csg.ifi.uzh.ch.smartcontract.R;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.setting.SettingProvider;

/**
 * Android {@link RecyclerView.Adapter} that manages a list of {@link Account} objects. Its {@link
 * ViewHolder} items contain the UI logic to lock/unlock a specific account
 */
public class AccountRecyclerViewAdapter extends RecyclerView.Adapter<AccountRecyclerViewAdapter.ViewHolder> {

    private final List<Account> accounts;
    private OnAccountListener loginListener;
    private SettingProvider settingProvider;

    public AccountRecyclerViewAdapter(List<Account> accounts, OnAccountListener loginListener, SettingProvider settingProvider) {

        this.accounts = accounts;
        this.loginListener = loginListener;
        this.settingProvider = settingProvider;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_account, parent, false);
        return new ViewHolder(view, loginListener, settingProvider);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.account = accounts.get(position);
        holder.initView();
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, OnAccountLoginResultListener {
        public final View view;
        public Account account;

        private final TextView aliasView;
        private final TextView idView;
        private final EditText passwordView;
        private final ImageButton loginButton;
        private final LinearLayout accountView;
        private final LinearLayout loginView;

        private Handler handler;
        private OnAccountListener loginListener;
        private SettingProvider settingProvider;

        public ViewHolder(View view, OnAccountListener loginListener, SettingProvider settingProvider)
        {
            super(view);

            this.loginListener = loginListener;
            this.settingProvider = settingProvider;
            this.handler = new Handler(Looper.getMainLooper());

            this.view = view;
            this.aliasView = (TextView)view.findViewById(R.id.account_alias_field);
            this.idView = (TextView)view.findViewById(R.id.account_id_field);
            this.passwordView = (EditText) view.findViewById(R.id.account_password_field);
            this.loginButton = (ImageButton) view.findViewById(R.id.account_login_button);
            this.accountView = (LinearLayout) view.findViewById(R.id.account_view);
            this.loginView = (LinearLayout) view.findViewById(R.id.account_login_view);

            loginButton.setOnClickListener(this);
            passwordView.setOnClickListener(this);
        }

        public void initView()
        {
            aliasView.setText(account.getLabel());
            idView.setText(account.getId());
            if(settingProvider.getSelectedAccount().equals(account.getId()))
            {
                //highlight the selected account
                accountView.setBackgroundResource(R.drawable.card_selected_background);
                loginButton.setImageResource(R.drawable.ic_action_lock);

            }else{
                accountView.setBackgroundResource(R.drawable.card_background);
                loginButton.setImageResource(R.drawable.ic_action_unlock);
            }

        }

        @Override
        public void onClick(View view)
        {
            switch(view.getId())
            {
                case R.id.account_password_field:
                    passwordView.setText("");
                    passwordView.requestFocus();
                    break;
                case R.id.account_login_button:

                    if(settingProvider.getSelectedAccount().equals(account.getId()))
                    {
                        passwordView.setText("");
                        loginListener.onAccountLock();
                    }else{
                        String password = passwordView.getText().toString();
                        loginListener.onAccountLogin(account, password, this);
                    }
            }
        }

        private void runOnUiThread(Runnable runnable)
        {
            handler.post(runnable);
        }

        @Override
        public void onLoginResult(final boolean success) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(success)
                    {
                        accountView.setBackgroundResource(R.drawable.card_selected_background);
                        loginButton.setImageResource(R.drawable.ic_action_lock);
                    }else{
                        passwordView.setText("");
                        passwordView.requestFocus();
                    }
                }
            });
        }
    }

    /**
     * Callback interface that a {@link ViewHolder} uses when the user wants to lock or unlock an
     * account.
     *
     * Implemented by the {@link AccountFragment}
     */
    public interface OnAccountListener
    {
        /**
         * Handles a login request for the specified account and password
         *
         * @param account
         * @param password
         * @param resultListener
         */
        void onAccountLogin(Account account, String password, OnAccountLoginResultListener resultListener);

        /**
         * locks the currently unlocked account
         */
        void onAccountLock();
    }

    /**
     * Implemented by the {@link ViewHolder} to get notified about the result of an unlock attempt
     */
    public interface OnAccountLoginResultListener
    {
        void onLoginResult(boolean success);
    }
}
