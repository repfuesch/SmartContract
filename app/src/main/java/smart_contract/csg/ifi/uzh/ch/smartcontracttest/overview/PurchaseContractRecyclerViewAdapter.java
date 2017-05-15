package smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.uzh.ifi.csg.contract.common.Web3;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.event.IContractObserver;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display.ContractDetailActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class PurchaseContractRecyclerViewAdapter
        extends RecyclerView.Adapter<PurchaseContractRecyclerViewAdapter.ViewHolder> {

    private final List<IPurchaseContract> contracts;
    private final List<ViewHolder> boundViewHolders;

    public PurchaseContractRecyclerViewAdapter(List<IPurchaseContract> contracts)
    {
        this.contracts = contracts;
        this.boundViewHolders = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_purchasecontract, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        IPurchaseContract contract = contracts.get(position);
        holder.attachContract(contract);
        boundViewHolders.add(holder);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        boundViewHolders.remove(holder);
    }

    @Override
    public int getItemCount() {
        return contracts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void onDestroy()
    {
        for(ViewHolder holder : boundViewHolders)
            holder.detachContract();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, IContractObserver
    {
        private final TextView titleView;
        private final TextView stateView;
        private final TextView priceView;
        private final CardView cardView;

        private Handler handler;
        private IPurchaseContract contract;

        public ViewHolder(View view) {
            super(view);

            this.handler = new Handler(Looper.getMainLooper());

            titleView = (TextView) view.findViewById(R.id.list_detail_title);
            stateView = (TextView) view.findViewById(R.id.list_detail_state);
            priceView = (TextView) view.findViewById(R.id.list_detail_price);
            cardView = (CardView) view.findViewById(R.id.card_view);
            cardView.setOnClickListener(this);
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
            if(contract != null)
                contract.removeObserver(this);
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
                case R.id.card_view:
                    Intent intent = new Intent(view.getContext(), ContractDetailActivity.class);
                    intent.putExtra(ContractDetailActivity.MESSAGE_SHOW_CONTRACT_DETAILS, contract.getContractAddress());
                    view.getContext().startActivity(intent);
                    break;
                default:
                    break;
            }
        }

        private void updateViewFromState()
        {
            ContractState state = contract.state().get();
            BigInteger value = contract.value().get();

            titleView.setText(contract.title().get());
            if(state != null)
                stateView.setText(state.toString());

            if(value != null)
                priceView.setText(Web3.toEther(value).round(MathContext.DECIMAL32).toString() + " ETH");
        }

        @Override
        public void contractStateChanged(String event, Object value)
        {
            //update the contract view
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateViewFromState();
                }
            });
        }
    }

}
