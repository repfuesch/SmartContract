package smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import ch.uzh.ifi.csg.contract.async.broadcast.TransactionManager;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.event.IContractObserver;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ContractErrorHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.ContractDetailActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsProvider;

import java.util.ArrayList;
import java.util.List;

public class PurchaseContractRecyclerViewAdapter
        extends RecyclerView.Adapter<PurchaseContractRecyclerViewAdapter.ViewHolder> {

    private final List<IPurchaseContract> contracts;
    private final ContractErrorHandler errorHandler;

    public PurchaseContractRecyclerViewAdapter(ContractErrorHandler errorHandler)
    {
        contracts = new ArrayList<>();
        this.errorHandler = errorHandler;
    }

    public boolean addContract(IPurchaseContract contract)
    {
        for(IPurchaseContract c : contracts)
        {
            if(c.getContractAddress().equals(contract.getContractAddress()))
            {
                errorHandler.showMessage("You already added this contract!");
                return false;
            }
        }

        contracts.add(contract);
        notifyItemInserted(contracts.size() - 1);
        return true;
    }

    public void removeContract(IPurchaseContract contract)
    {
        IPurchaseContract toRemove = null;
        for(IPurchaseContract c : contracts)
        {
            if(c.getContractAddress().equals(contract.getContractAddress()))
            {
                toRemove = c;
                break;
            }
        }

        if(toRemove != null)
        {
            contracts.remove(contract);
            notifyDataSetChanged();
        }
    }

    public void clearContracts()
    {
        contracts.clear();
        notifyDataSetChanged();
    }

    public void updateContract(String contractAddress)
    {
        for(IPurchaseContract contract : contracts)
        {
            if(contract.getContractAddress().equals(contractAddress))
                notifyItemChanged(contracts.indexOf(contract));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_purchasecontract, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        IPurchaseContract contract = contracts.get(position);
        holder.attachContract(contract);
    }

    @Override
    public int getItemCount() {
        return contracts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, IContractObserver {
        public final View view;
        public final TextView titleView;
        public final TextView stateView;
        public final TextView priceView;
        public final LinearLayout headerView;
        public final LinearLayout bodyView;
        public final Button buyButton;
        public final Button abortButton;
        public final Button confirmButton;
        public final Button showDetailsButton;
        public final LinearLayout cardProgressView;

        private Handler handler;
        private IPurchaseContract contract;

        public ViewHolder(View view) {
            super(view);
            this.view = view;

            this.handler = new Handler(Looper.getMainLooper());

            titleView = (TextView) view.findViewById(R.id.list_detail_title);
            stateView = (TextView) view.findViewById(R.id.list_detail_state);
            priceView = (TextView) view.findViewById(R.id.list_detail_price);

            headerView = (LinearLayout) view.findViewById(R.id.list_detail_header);
            bodyView = (LinearLayout) view.findViewById(R.id.list_detail_body);
            cardProgressView = (LinearLayout) view.findViewById(R.id.card_progress_view);

            buyButton = (Button) view.findViewById(R.id.detail_buy_button);
            abortButton = (Button) view.findViewById(R.id.detail_abort_button);
            confirmButton = (Button) view.findViewById(R.id.detail_confirm_button);
            showDetailsButton = (Button) view.findViewById(R.id.detail_show_more_button);
            buyButton.setOnClickListener(this);
            abortButton.setOnClickListener(this);
            confirmButton.setOnClickListener(this);
            showDetailsButton.setOnClickListener(this);
        }


        @Override
        public String toString() {
            return super.toString() + " '" + stateView.getText() + "'";
        }

        public void attachContract(IPurchaseContract contract)
        {
            this.contract = contract;
            contract.addObserver(this);
            updateViewFromState();
        }

        public void detachContract()
        {
            contract.deleteObserver(this);
        }

        private void runOnUiThread(Runnable runnable)
        {
            handler.post(runnable);
        }

        @Override
        public void onClick(View view)
        {
            switch(view.getId())
            {
                case R.id.detail_buy_button:
                    buyButton.setEnabled(false);
                    TransactionManager.toTransaction(contract.confirmPurchase(), contract.getContractAddress());
                    break;
                case R.id.detail_abort_button:
                    abortButton.setEnabled(false);
                    TransactionManager.toTransaction(contract.abort(), contract.getContractAddress());
                    break;
                case R.id.detail_confirm_button:
                    confirmButton.setEnabled(false);
                    TransactionManager.toTransaction(contract.confirmReceived(), contract.getContractAddress());
                    break;
                case R.id.detail_show_more_button:
                    Intent intent = new Intent(view.getContext(), ContractDetailActivity.class);
                    intent.putExtra(ContractDetailActivity.MESSAGE_SHOW_CONTRACT_DETAILS, contract.getContractAddress());
                    view.getContext().startActivity(intent);
                default:
                    break;
            }
        }

        private void showProgressView()
        {
            bodyView.setVisibility(View.GONE);
            cardProgressView.setVisibility(View.VISIBLE);
        }

        private void hideProgressView()
        {
            cardProgressView.setVisibility(View.GONE);
            bodyView.setVisibility(View.VISIBLE);
        }

        private void updateViewFromState()
        {
            ContractState state = contract.state().get();
            Integer value = contract.value().get();
            String seller = contract.seller().get();
            String buyer = contract.buyer().get();
            String selectedAccount = SettingsProvider.getInstance().getSelectedAccount();

            titleView.setText(contract.title().get());
            if(state != null)
                stateView.setText(state.toString());

            if(value != null)
                priceView.setText(value.toString() + " ETH");

            if(state.equals(ContractState.Created) && !seller.equals(selectedAccount))
            {
                buyButton.setVisibility(View.VISIBLE);
            }else{
                buyButton.setVisibility(View.GONE);
            }

            if(state.equals(ContractState.Created) && seller.equals(selectedAccount))
            {
                abortButton.setVisibility(View.VISIBLE);
            }else{
                abortButton.setVisibility(View.GONE);
            }

            if(state.equals(ContractState.Locked) && buyer.equals(selectedAccount))
            {
                confirmButton.setVisibility(View.VISIBLE);
            }else{
                confirmButton.setVisibility(View.GONE);
            }
        }

        @Override
        public void contractStateChanged(String event, Object value)
        {
            //update the contract view
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgressView();
                    updateViewFromState();
                    hideProgressView();
                }
            });

        }
    }

}
