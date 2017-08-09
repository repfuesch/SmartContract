package smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.list;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.jdeferred.Promise;
import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.util.Web3Util;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.IRentContract;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.contract.IContractObserver;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContext;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display.ContractDetailActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * {@link RecyclerView.Adapter} that holds a list of {@link ITradeContract} objects. It implements
 * the {@link Filterable} interface to filter the list based on a text constraint.
 */
public class TradeContractRecyclerViewAdapter extends RecyclerView.Adapter<TradeContractRecyclerViewAdapter.ViewHolder> implements Filterable {

    private  List<ITradeContract> originalContracts;
    private  List<ITradeContract> filteredContracts;
    private final List<ViewHolder> boundViewHolders;
    private ITradeContract selectedContract;
    private ApplicationContext contextProvider;

    public TradeContractRecyclerViewAdapter(ApplicationContext contextProvider)
    {
        this.originalContracts = new ArrayList<>();
        this.filteredContracts = new ArrayList<>();
        this.boundViewHolders = new ArrayList<>();
        this.contextProvider = contextProvider;
    }

    public void addContract(ITradeContract contract)
    {
        originalContracts.add(contract);
        filteredContracts.add(contract);
    }

    public void setContracts(List<ITradeContract> contracts)
    {
        originalContracts = new ArrayList<>(contracts);
        filteredContracts = new ArrayList<>(contracts);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_contract_item, parent, false);
        
        return new ViewHolder(view, contextProvider);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        final ITradeContract contract = filteredContracts.get(position);
        holder.attachContract(contract);
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                selectedContract = contract;
                return false;
            }
        });

        boundViewHolders.add(holder);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.cardView.setOnLongClickListener(null);
        boundViewHolders.remove(holder);
    }

    @Override
    public int getItemCount() {
        return filteredContracts.size();
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

    public ITradeContract getSelectedContract() {
        return selectedContract;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredContracts = (List<ITradeContract>) results.values;
                TradeContractRecyclerViewAdapter.this.notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ITradeContract> filteredResults = null;
                if (constraint.length() == 0) {
                    filteredResults = originalContracts;
                } else {
                    filteredResults = getFilteredResults(constraint.toString().toLowerCase());
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;

                return results;
            }
        };
    }

    /**
     * Filters originalContracts based on text constraint.
     *
     * @param constraint
     * @return
     */
    protected List<ITradeContract> getFilteredResults(String constraint) {
        List<ITradeContract> results = new ArrayList<>();

        for (ITradeContract item : originalContracts) {
            if (item.getTitle().get().toLowerCase().contains(constraint)) {
                results.add(item);
            }else if(item.getDescription().get().toLowerCase().contains(constraint))
            {
                results.add(item);
            }
        }

        return results;
    }

    /**
     * Represents one {@link ITradeContract} in the list. Displays the most important attributes
     * of the contract to the user and navigates her to the {@link ContractDetailActivity} when she
     * clicks on it.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, IContractObserver, View.OnCreateContextMenuListener {
        private final TextView titleView;
        private final TextView stateView;
        private final TextView priceView;
        private final TextView contractTypeView;
        public final CardView cardView;
        private final LinearLayout cardContent;

        private Handler handler;
        private ITradeContract contract;
        private ContractState state;
        private String title;

        private final ApplicationContext contextProvider;

        public ViewHolder(View view, ApplicationContext contextProvider) {
            super(view);

            this.contextProvider = contextProvider;
            this.handler = new Handler(Looper.getMainLooper());

            titleView = (TextView) view.findViewById(R.id.list_detail_title);
            stateView = (TextView) view.findViewById(R.id.list_detail_state);
            priceView = (TextView) view.findViewById(R.id.list_detail_price);
            contractTypeView = (TextView) view.findViewById(R.id.list_detail_contract_type);
            cardContent = (LinearLayout) view.findViewById(R.id.card_content);
            cardView = (CardView) view.findViewById(R.id.card_view);
            cardView.setOnClickListener(this);

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
        {
            if(state != ContractState.Locked)
            {
                menu.setHeaderTitle("Remove contract?");
                menu.add(0, v.getId(), 0, "remove");
            }
        }

        @Override
        public String toString() {
            return super.toString() + " '" + stateView.getText() + "'";
        }

        public void attachContract(ITradeContract contract)
        {
            if(this.contract != null)
                detachContract();

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

        /**
         * Initializes the view from the attached contract
         */
        private void updateViewFromState()
        {
            BusyIndicator.show(cardContent);
            Async.run(new Callable<Void>() {
                @Override
                public Void call() throws Exception {

                    state = contract.getState().get();
                    final BigInteger price = contract.getPrice().get();
                    title = contract.getTitle().get();
                    final ContractType type = contract.getContractType();

                    BigDecimal amountEther;
                    final String contractType;
                    if(type == ContractType.Purchase)
                    {
                        amountEther = Web3Util.toEther(price);
                        contractType = "Purchase Contract";
                    }else{
                        IRentContract rentContract = (IRentContract)contract;
                        amountEther = Web3Util.toEther(rentContract.getRentingFee().get());
                        contractType = "Rent Contract";
                    }

                    //convert price to US-Dollar
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
                                            if(state != null)
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
                    BusyIndicator.hide(cardContent);
                }
            });
        }

        @Override
        public void contractStateChanged(String event, Object value)
        {
            //update the view when the contract state changes
            updateViewFromState();
        }

    }

}
