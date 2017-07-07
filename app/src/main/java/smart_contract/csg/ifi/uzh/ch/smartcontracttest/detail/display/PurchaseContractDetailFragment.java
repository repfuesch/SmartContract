package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.jdeferred.Promise;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.Callable;
import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.FailCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.common.Web3Util;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;


/**
 * Created by flo on 05.06.17.
 */

public class PurchaseContractDetailFragment extends ContractDetailFragment {

    private TextView priceView;

    private Button buyButton;
    private Button abortButton;
    private Button confirmButton;

    protected BigInteger price;
    private IPurchaseContract contract;

    public PurchaseContractDetailFragment() {
        // Required empty public constructor
    }

    public static ContractDetailFragment newInstance(String param1, String param2) {
        ContractDetailFragment fragment = new PurchaseContractDetailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        priceView = (TextView) view.findViewById(R.id.general_price);

        buyButton = (Button) view.findViewById(R.id.buy_button);
        abortButton = (Button) view.findViewById(R.id.abort_button);
        confirmButton = (Button) view.findViewById(R.id.confirm_button);

        buyButton.setOnClickListener(this);
        abortButton.setOnClickListener(this);
        confirmButton.setOnClickListener(this);

        return view;
    }

    @Override
    protected ITradeContract getContract() {
        return contract;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_purchase_contract_detail;
    }

    @Override
    public void onClick(View view)
    {
        super.onClick(view);

        switch(view.getId())
        {
            case R.id.buy_button:
                BigInteger value = price.multiply(BigInteger.valueOf(2));
                if(!ensureBalance(value))
                    return;

                BusyIndicator.show(bodyView);
                SimplePromise buyPromise = contract.confirmPurchase().always(new AlwaysCallback<String>() {
                    @Override
                    public void onAlways(Promise.State state, String resolved, Throwable rejected) {
                        BusyIndicator.hide(bodyView);

                    }
                });
                contextProvider.getTransactionManager().toTransaction(buyPromise, contract.getContractAddress());
                break;
            case R.id.abort_button:
                BusyIndicator.show(bodyView);
                SimplePromise abortPromise = contract.abort().always(new AlwaysCallback<String>() {
                    @Override
                    public void onAlways(Promise.State state, String resolved, Throwable rejected) {
                        BusyIndicator.hide(bodyView);
                    }
                });
                contextProvider.getTransactionManager().toTransaction(abortPromise, contract.getContractAddress());
                break;
            case R.id.confirm_button:
                BusyIndicator.show(bodyView);
                SimplePromise confirmPromise = contract.confirmReceived().always(new AlwaysCallback<String>() {
                    @Override
                    public void onAlways(Promise.State state, String resolved, Throwable rejected) {
                        BusyIndicator.hide(bodyView);
                    }
                });
                contextProvider.getTransactionManager().toTransaction(confirmPromise, contract.getContractAddress());
                break;
            default:
                break;
        }
    }

    public SimplePromise<Void> init(final ITradeContract contract)
    {
        this.contract = (IPurchaseContract)contract;

        BusyIndicator.show(bodyView);
        return super.init(contract).then(new DoneCallback<Void>() {
            @Override
            public void onDone(Void result) {

                Async.run(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        final ContractState state = contract.getState();
                        final String selectedAccount = contextProvider.getSettingProvider().getSelectedAccount();
                        final String seller = contract.getSeller();
                        final String buyer = contract.getBuyer();
                        price  = contract.getPrice();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(state.equals(ContractState.Created) && !seller.equals(selectedAccount))
                                {
                                    buyButton.setEnabled(true);
                                }else{
                                    buyButton.setEnabled(false);
                                }

                                if(state.equals(ContractState.Created) && seller.equals(selectedAccount))
                                {
                                    abortButton.setEnabled(true);
                                }else{
                                    abortButton.setEnabled(false);
                                }

                                if(state.equals(ContractState.Locked) && buyer.equals(selectedAccount))
                                {
                                    confirmButton.setEnabled(true);
                                }else{
                                    confirmButton.setEnabled(false);
                                }
                            }
                        });
                        return null;
                    }
                });
            }
        }).always(new AlwaysCallback<Void>() {
            @Override
            public void onAlways(Promise.State state, Void resolved, Throwable rejected) {
                BusyIndicator.hide(bodyView);
            }
        });
    }

    @Override
    protected void selectedCurrencyChanged()
    {
        contextProvider.getServiceProvider().getExchangeService().getEthExchangeRatesAsync()
                .done(new DoneCallback<Map<Currency, Float>>() {
                    @Override
                    public void onDone(Map<Currency, Float> currencyMap) {
                        BigDecimal amountEther = Web3Util.toEther(price);
                        final Float amountCurrency = amountEther.floatValue() * currencyMap.get(selectedCurrency);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                priceView.setText(amountCurrency.toString());
                            }
                        });
                    }
                })
                .fail(new FailCallback() {
                    @Override
                    public void onFail(Throwable result) {
                        //todo:log
                        //messageHandler.handleError(result);
                    }
                });
    }
}
