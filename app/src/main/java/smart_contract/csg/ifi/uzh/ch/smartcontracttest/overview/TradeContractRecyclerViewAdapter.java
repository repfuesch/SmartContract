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

import org.jdeferred.Promise;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.common.Web3Util;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.IRentContract;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.event.IContractObserver;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display.ContractDetailActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class TradeContractRecyclerViewAdapter
        extends RecyclerView.Adapter<TradeContractRecyclerViewAdapter.ViewHolder> {

    private final List<ITradeContract> contracts;
    private final List<ViewHolder> boundViewHolders;
    private ApplicationContextProvider contextProvider;

    public TradeContractRecyclerViewAdapter(List<ITradeContract> contracts, ApplicationContextProvider contextProvider)
    {
        this.contracts = contracts;
        this.boundViewHolders = new ArrayList<>();
        this.contextProvider = contextProvider;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_purchasecontract, parent, false);
        return new ViewHolder(view, contextProvider);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        ITradeContract contract = contracts.get(position);
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, IContractObserver
    {
        private final TextView titleView;
        private final TextView stateView;
        private final TextView priceView;
        private final TextView contractTypeView;
        private final CardView cardView;

        private Handler handler;
        private ITradeContract contract;

        private final ApplicationContextProvider contextProvider;

        public ViewHolder(View view, ApplicationContextProvider contextProvider) {
            super(view);
            this.contextProvider = contextProvider;

            this.handler = new Handler(Looper.getMainLooper());

            titleView = (TextView) view.findViewById(R.id.list_detail_title);
            stateView = (TextView) view.findViewById(R.id.list_detail_state);
            priceView = (TextView) view.findViewById(R.id.list_detail_price);
            contractTypeView = (TextView) view.findViewById(R.id.list_detail_contract_type);
            cardView = (CardView) view.findViewById(R.id.card_view);
            cardView.setOnClickListener(this);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + stateView.getText() + "'";
        }

        public void attachContract(ITradeContract contract)
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
                    intent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, contract.getContractAddress());
                    intent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, contract.getContractType());
                    view.getContext().startActivity(intent);
                    break;
                default:
                    break;
            }
        }

        private void updateViewFromState()
        {
            BusyIndicator.show(cardView);
            Async.run(new Callable<Void>() {
                @Override
                public Void call() throws Exception {

                    final ContractState state = contract.getState();
                    final BigInteger price = contract.getPrice();
                    final String title = contract.getTitle();
                    final ContractType type = contract.getContractType();

                    BigDecimal amountEther;
                    final String contractType;
                    if(type == ContractType.Purchase)
                    {
                        amountEther = Web3Util.toEther(price);
                        contractType = "Purchase Contract";
                    }else{
                        IRentContract rentContract = (IRentContract)contract;
                        amountEther = Web3Util.toEther(rentContract.getRentingFee());
                        contractType = "Rent Contract";
                    }

                    contextProvider.getServiceProvider().getExchangeService().convertToCurrency(amountEther, Currency.USD)
                            .done(new DoneCallback<BigDecimal>() {
                                @Override
                                public void onDone(final BigDecimal result) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            BigDecimal rounded = result.setScale(2, BigDecimal.ROUND_HALF_EVEN);
                                            priceView.setText(rounded.toString());
                                            titleView.setText(title);
                                            stateView.setText(state.toString());
                                            contractTypeView.setText(contractType);
                                        }
                                    });
                                }
                            });

                    return null;
                }
            }).always(new AlwaysCallback<Void>() {
                @Override
                public void onAlways(Promise.State state, Void resolved, Throwable rejected) {
                    BusyIndicator.hide(cardView);
                }
            });
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
