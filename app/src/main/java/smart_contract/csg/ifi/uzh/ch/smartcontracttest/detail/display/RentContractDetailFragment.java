package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display;

import android.app.DialogFragment;
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
import java.util.Map;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.FailCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.common.Web3Util;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.IRentContract;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.contract.TimeUnit;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.dialog.ImageDialogFragment;

/**
 * Created by flo on 05.06.17.
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

        rentButton = (Button) view.findViewById(R.id.rent_button);
        abortButton = (Button) view.findViewById(R.id.abort_button);
        returnButton = (Button) view.findViewById(R.id.return_button);
        reclaimButton = (Button) view.findViewById(R.id.reclaim_button);
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

        rentButton.setOnClickListener(this);
        abortButton.setOnClickListener(this);
        returnButton.setOnClickListener(this);
        reclaimButton.setOnClickListener(this);

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

    public SimplePromise<Void> init(final ITradeContract tradeContract)
    {
        this.contract = (IRentContract) tradeContract;

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
                        fee = contract.getPrice();
                        deposit = contract.getDeposit();

                        updateCurrencyFields();

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

    @Override
    protected void selectedCurrencyChanged()
    {
        updateCurrencyFields();
    }

    private void updateCurrencyFields()
    {
        BusyIndicator.show(bodyView);
        Async.run(new Callable<Void>() {

            @Override
            public Void call() throws Exception
            {
                final BigDecimal depositEther = Web3Util.toEther(deposit);
                final BigDecimal feeEther = Web3Util.toEther(fee);
                BigInteger totalFee = contract.getRentingFee();
                final BigDecimal totalFeeEther = Web3Util.toEther(totalFee);

                contextProvider.getServiceProvider().getExchangeService().getExchangeRate(selectedCurrency)
                        .done(new DoneCallback<BigDecimal>() {
                            @Override
                            public void onDone(BigDecimal exchangeRate) {
                                final BigDecimal depositCurrency = depositEther.multiply(exchangeRate);
                                BigDecimal feeCurrency = feeEther.multiply(exchangeRate);
                                BigDecimal totalFeeCurrency = totalFeeEther.multiply(exchangeRate);
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
                                        rentFeeField.setText(finalFeeCurrency.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
                                        depositField.setText(depositCurrency.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
                                        currentFeeField.setText(finalTotalFeeCurrency.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
                                    }
                                });
                            }
                        })
                        .always(new AlwaysCallback<BigDecimal>() {
                            @Override
                            public void onAlways(Promise.State state, BigDecimal resolved, Throwable rejected) {
                                BusyIndicator.hide(bodyView);
                            }
                        });

                return null;
            }
        }).fail(new FailCallback() {
            @Override
            public void onFail(Throwable result) {
                //todo:log
                result.printStackTrace();
                //messageHandler.handleError(result);
            }
        });
    }

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
            case R.id.return_button:
                BusyIndicator.show(bodyView);
                SimplePromise returnPromise = contract.returnItem().always(new AlwaysCallback<String>() {
                    @Override
                    public void onAlways(Promise.State state, String resolved, Throwable rejected) {
                        BusyIndicator.hide(bodyView);
                    }
                });
                contextProvider.getTransactionManager().toTransaction(returnPromise, contract.getContractAddress());
                break;
            case R.id.reclaim_button:
                BusyIndicator.show(bodyView);
                SimplePromise reclaimPromise = contract.reclaimItem().always(new AlwaysCallback<String>() {
                    @Override
                    public void onAlways(Promise.State state, String resolved, Throwable rejected) {
                        BusyIndicator.hide(bodyView);
                    }
                });
                contextProvider.getTransactionManager().toTransaction(reclaimPromise, contract.getContractAddress());
                break;
            default:
                break;
        }
    }
}
