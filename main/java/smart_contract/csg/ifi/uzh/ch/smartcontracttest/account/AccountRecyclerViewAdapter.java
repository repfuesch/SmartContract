package smart_contract.csg.ifi.uzh.ch.smartcontracttest.account;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ch.uzh.ifi.csg.contract.service.account.Account;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Account} and makes a call to the
 * specified {@link smart_contract.csg.ifi.uzh.ch.smartcontracttest.account.AccountFragment.OnAccountFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class AccountRecyclerViewAdapter extends RecyclerView.Adapter<AccountRecyclerViewAdapter.ViewHolder> {

    private final List<Account> accounts;
    private final AccountFragment.OnAccountFragmentInteractionListener mListener;

    public AccountRecyclerViewAdapter(List<Account> accounts) {

        this.accounts = accounts;
        mListener = null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = accounts.get(position);
        //holder.mIdView.setText(accounts.get(position).id);
        //holder.mContentView.setText(accounts.get(position).content);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Account mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
