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
import java.util.concurrent.Callable;
import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.FailCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.util.Web3Util;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;


/**
 * {@link ContractDetailFragment} that displays the details and contains the interaction logic for
 * an {@link IPurchaseContract} instance.
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

        //buttons to execute transactions on the contract
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

    /**
     * Executes transactions on the smart contract
     *
     * @param view
     */
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
                appContext.getTransactionManager().toTransaction(buyPromise, contract.getContractAddress());
                break;
            case R.id.abort_button:
                BusyIndicator.show(bodyView);
                SimplePromise abortPromise = contract.abort().always(new AlwaysCallback<String>() {
                    @Override
                    public void onAlways(Promise.State state, String resolved, Throwable rejected) {
                        BusyIndicator.hide(bodyView);
                    }
                });
                appContext.getTransactionManager().toTransaction(abortPromise, contract.getContractAddress());
                break;
            case R.id.confirm_button:
                BusyIndicator.show(bodyView);
                SimplePromise confirmPromise = contract.confirmReceived().always(new AlwaysCallback<String>() {
                    @Override
                    public void onAlways(Promise.State state, String resolved, Throwable rejected) {
                        BusyIndicator.hide(bodyView);
                    }
                });
                appContext.getTransactionManager().toTransaction(confirmPromise, contract.getContractAddress());
                break;
            default:
                break;
        }
    }

    /**
     * see {@link ContractDetailFragment#init(ITradeContract)}
     *
     * @param contract
     * @return
     */
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

                        //retrieve the details of the contract
                        final ContractState state = contract.getState().get();
                        final String selectedAccount = appContext.getSettingProvider().getSelectedAccount();
                        final String seller = contract.getSeller().get();
                        final String buyer = contract.getBuyer().get();
                        price  = contract.getPrice().get();

                        //update priceView
                        updateCurrencyFields();

                        //Enable or disable contract interaction buttons based on the contract state and the role of the user
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

    /**
     * See {@link ContractDetailFragment#selectedCurrencyChanged()}
     */
    @Override
    protected void selectedCurrencyChanged()
    {
        if(price == null)
            return;

        updateCurrencyFields();
    }

    private void updateCurrencyFields()
    {
        BusyIndicator.show(bodyView);

        //Use the exchangeService to convert the prive to the selected currency and init the priceView
        appContext.getServiceProvider().getExchangeService().convertToCurrency(Web3Util.toEther(price), selectedCurrency)
                .done(new DoneCallback<BigDecimal>() {
                    @Override
                    public void onDone(final BigDecimal result) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                priceView.setText(result.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
                            }
                        });
                    }
                })
                .fail(new FailCallback() {
                    @Override
                    public void onFail(Throwable result) {
                        appContext.getMessageService().showErrorMessage("Cannot reach the exchange service. Try again later.");
                    }
                })
                .always(new AlwaysCallback<BigDecimal>() {
                    @Override
                    public void onAlways(Promise.State state, BigDecimal resolved, Throwable rejected) {
                        BusyIndicator.hide(bodyView);
                    }
                });
    }
}
