package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.FailCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.common.Web3Util;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.contract.IRentContract;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.contract.TimeUnit;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

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

    private IRentContract contract;
    private BigInteger deposit;
    private BigInteger fee;
    private TimeUnit timeUnit;
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
                        timeUnit = contract.getTimeUnit();

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

                                if(state.equals(ContractState.Locked) && seller.equals(selectedAccount))
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
        });
    }

    @Override
    protected void selectedCurrencyChanged()
    {
        updateCurrencyFields();
    }

    private void updateCurrencyFields()
    {
        Async.run(new Callable<Void>() {

            @Override
            public Void call() throws Exception
            {
                Map<Currency, Float> currencyMap = contextProvider.getServiceProvider().getExchangeService().getEthExchangeRates();
                BigDecimal depositEther = Web3Util.toEther(deposit);
                final BigDecimal feeEther = Web3Util.toEther(fee);
                BigInteger totalFee = contract.getRentingFee();
                BigDecimal totalFeeEther = Web3Util.toEther(totalFee);

                Float exchangeRate = currencyMap.get(selectedCurrency);
                final Float depositCurrency = depositEther.floatValue() * exchangeRate;
                final Float feeCurrency = feeEther.floatValue() * exchangeRate;
                final Float totalFeeCurrency = totalFeeEther.floatValue() * exchangeRate;

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rentFeeField.setText(feeCurrency.toString() + " / " + timeUnit.toString());
                        depositField.setText(depositCurrency.toString());
                        currentFeeField.setText(totalFeeCurrency.toString());
                    }
                });

                return null;
            }
        }).fail(new FailCallback() {
            @Override
            public void onFail(Throwable result) {
                messageHandler.handleError(result);
            }
        });
    }
}
