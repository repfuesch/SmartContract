package ch.uzh.ifi.csg.smartcontract.app.detail.display;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import org.jdeferred.Promise;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import ch.uzh.ifi.csg.smartcontract.app.R;
import ch.uzh.ifi.csg.smartcontract.app.common.controls.BusyIndicator;
import ch.uzh.ifi.csg.smartcontract.library.async.Async;
import ch.uzh.ifi.csg.smartcontract.library.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.smartcontract.library.async.promise.DoneCallback;
import ch.uzh.ifi.csg.smartcontract.library.async.promise.SimplePromise;
import ch.uzh.ifi.csg.smartcontract.library.contract.ContractState;
import ch.uzh.ifi.csg.smartcontract.library.contract.IRentContract;
import ch.uzh.ifi.csg.smartcontract.library.contract.ITradeContract;
import ch.uzh.ifi.csg.smartcontract.library.contract.TimeUnit;
import ch.uzh.ifi.csg.smartcontract.library.util.Web3Util;

/**
 * {@link ContractDetailFragment} that displays the details and contains the interaction logic for
 * an {@link IRentContract} instance.
 */
public class RentContractDetailFragment extends ContractDetailFragment
{
    private Button rentButton;
    private Button abortButton;
    private Button returnButton;
    private Button reclaimButton;

    private EditText rentFeeField;
    private EditText currentFeeField;
    private EditText depositField;
    private Spinner timeUnitSpinner;
    private TimeUnit selectedTimeUnit;

    private IRentContract contract;
    private BigInteger deposit;
    private BigInteger fee;

    private BigInteger currentFee;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        rentFeeField = (EditText) view.findViewById(R.id.rent_fee);
        currentFeeField = (EditText) view.findViewById(R.id.rent_current_fee);
        depositField = (EditText) view.findViewById(R.id.rent_deposit);

        //buttons to execute transactions on the contract
        rentButton = (Button) view.findViewById(R.id.rent_button);
        abortButton = (Button) view.findViewById(R.id.abort_button);
        returnButton = (Button) view.findViewById(R.id.return_button);
        reclaimButton = (Button) view.findViewById(R.id.reclaim_button);

        rentButton.setOnClickListener(this);
        abortButton.setOnClickListener(this);
        returnButton.setOnClickListener(this);
        reclaimButton.setOnClickListener(this);

        timeUnitSpinner = (Spinner) view.findViewById(R.id.contract_time_unit_spinner);
        timeUnitSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, TimeUnit.values()));
        timeUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTimeUnit = TimeUnit.values()[i];
                updateCurrencyFields();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedTimeUnit = TimeUnit.Days;
                updateCurrencyFields();
            }
        });

        return view;
    }

    @Override
    protected ITradeContract getContract() {
        return contract;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_rent_contract_detail;
    }

    /**
     * see {@link ContractDetailFragment#init(ITradeContract)}
     *
     * @param tradeContract
     * @return
     */
    public SimplePromise<Void> init(final ITradeContract tradeContract)
    {
        this.contract = (IRentContract) tradeContract;

        return super.init(contract).then(new DoneCallback<Void>() {
            @Override
            public void onDone(Void result) {

                Async.run(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        //retrieve the contract details
                        final ContractState state = contract.getState().get();
                        final String selectedAccount = appContext.getSettingProvider().getSelectedAccount();
                        final String seller = contract.getSeller().get();
                        final String buyer = contract.getBuyer().get();
                        fee = contract.getPrice().get();
                        deposit = contract.getDeposit().get();

                        updateCurrencyFields();

                        //Enable or disable contract interaction buttons based on the contract state and the role of the user
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if(state.equals(ContractState.Locked) || state.equals(ContractState.AwaitPayment))
                                {
                                    currentFeeField.setHint(R.string.hint_current_fee);
                                }else{
                                    currentFeeField.setHint(R.string.hint_paid_fee);
                                }
                                if(state.equals(ContractState.Created) && !seller.equals(selectedAccount))
                                {
                                    rentButton.setEnabled(true);
                                }else{
                                    rentButton.setEnabled(false);
                                }

                                if(state.equals(ContractState.Created) && seller.equals(selectedAccount))
                                {
                                    abortButton.setEnabled(true);
                                }else{
                                    abortButton.setEnabled(false);
                                }

                                if((state.equals(ContractState.Locked) || state.equals(ContractState.AwaitPayment)) && seller.equals(selectedAccount))
                                {
                                    reclaimButton.setEnabled(true);
                                }else{
                                    reclaimButton.setEnabled(false);
                                }

                                if(state.equals(ContractState.AwaitPayment) && buyer.equals(selectedAccount))
                                {
                                    returnButton.setEnabled(true);
                                }else{
                                    returnButton.setEnabled(false);
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
     * see {@link ContractDetailFragment#selectedCurrencyChanged()}
     */
    @Override
    protected void selectedCurrencyChanged()
    {
        if(fee == null)
            return;

        updateCurrencyFields();
    }

    private void updateCurrencyFields()
    {
        BusyIndicator.show(bodyView);
        Async.run(new Callable<Void>() {

            @Override
            public Void call() throws Exception
            {
                //convert the fees to ether
                final BigDecimal depositEther = Web3Util.toEther(deposit);
                final BigDecimal feeEther = Web3Util.toEther(fee);
                BigInteger totalFee = contract.getRentingFee().get();
                final BigDecimal totalFeeEther = Web3Util.toEther(totalFee);

                //retrieve ether exchange rate
                BigDecimal exchangeRate = appContext.getServiceProvider().getExchangeService().getExchangeRate(selectedCurrency).get();
                if(exchangeRate == null)
                {
                    appContext.getMessageService().showErrorMessage("Cannot reach the exchange service. Try again later.");
                    return null;
                }

                //convert the fees to currency
                final BigDecimal depositCurrency = depositEther.multiply(exchangeRate);
                BigDecimal feeCurrency = feeEther.multiply(exchangeRate);
                BigDecimal totalFeeCurrency = totalFeeEther.multiply(exchangeRate);

                //calculate the fee per specified time unit
                if(selectedTimeUnit == TimeUnit.Days)
                {
                    feeCurrency = feeCurrency.multiply(BigDecimal.valueOf(3600 * 24));
                }else{
                    feeCurrency = feeCurrency.multiply(BigDecimal.valueOf(3600));
                }

                final BigDecimal finalTotalFeeCurrency = totalFeeCurrency;
                final BigDecimal finalFeeCurrency = feeCurrency;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //update currency fields
                        rentFeeField.setText(finalFeeCurrency.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
                        depositField.setText(depositCurrency.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
                        currentFeeField.setText(finalTotalFeeCurrency.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
                    }
                });

                return null;
            }
        }).always(new AlwaysCallback<Void>() {
            @Override
            public void onAlways(Promise.State state, Void resolved, Throwable rejected) {
                BusyIndicator.hide(bodyView);
            }
        });
    }

    /**
     *Executes transactions on the smart contract
     *
     * @param view
     */
    @Override
    public void onClick(View view)
    {
        super.onClick(view);

        switch(view.getId())
        {
            case R.id.rent_button:
                BigInteger value = deposit;
                if(!ensureBalance(value))
                    return;

                BusyIndicator.show(bodyView);
                SimplePromise buyPromise = contract.rentItem().always(new AlwaysCallback<String>() {
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
            case R.id.return_button:
                BusyIndicator.show(bodyView);
                SimplePromise returnPromise = contract.returnItem().always(new AlwaysCallback<String>() {
                    @Override
                    public void onAlways(Promise.State state, String resolved, Throwable rejected) {
                        BusyIndicator.hide(bodyView);
                    }
                });
                appContext.getTransactionManager().toTransaction(returnPromise, contract.getContractAddress());
                break;
            case R.id.reclaim_button:
                BusyIndicator.show(bodyView);
                SimplePromise reclaimPromise = contract.reclaimItem().always(new AlwaysCallback<String>() {
                    @Override
                    public void onAlways(Promise.State state, String resolved, Throwable rejected) {
                        BusyIndicator.hide(bodyView);
                    }
                });
                appContext.getTransactionManager().toTransaction(reclaimPromise, contract.getContractAddress());
                break;
            default:
                break;
        }
    }
}
