package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.util.Web3Util;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.contract.TimeUnit;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.validation.RequiredTextFieldValidator;

/**
 * Created by flo on 05.06.17.
 */

public class RentContractDeployFragment extends ContractDeployFragment
{
    private EditText depositField;
    private Spinner timeUnitSpinner;
    private TimeUnit selectedTimeUnit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        depositField = (EditText) view.findViewById(R.id.contract_deposit);
        depositField.addTextChangedListener(new RequiredTextFieldValidator(depositField));
        depositField.addTextChangedListener(this);

        timeUnitSpinner = (Spinner) view.findViewById(R.id.contract_time_unit_spinner);
        timeUnitSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, TimeUnit.values()));
        timeUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTimeUnit = TimeUnit.values()[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedTimeUnit = TimeUnit.Days;
            }
        });

        return view;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_contract_create_rent;
    }

    protected void setSelectedCurrency(Currency currency)
    {
        super.setSelectedCurrency(currency);
    }

    protected boolean verifyFields()
    {
        boolean parentValid = super.verifyFields();
        return parentValid && depositField.getError() == null;
    }

    @Override
    protected SimplePromise<ITradeContract> deployContract(BigInteger priceWei, String title, String description, boolean needsVerification, final Map<String, File> imageSignatures)
    {
        BigDecimal deposit = new BigDecimal(depositField.getText().toString());
        BigDecimal depositEther = appContext.getServiceProvider().getExchangeService().convertToEther(deposit, selectedCurrency).get();
        if(depositEther == null)
            return null;

        BigInteger depositWei = Web3Util.toWei(depositEther);
        if(!ensureBalance(depositWei))
            return null;

        BigInteger rentingFeePerSecond;
        if(selectedTimeUnit == TimeUnit.Days)
        {
            rentingFeePerSecond = priceWei.divide(BigInteger.valueOf(3600 * 24));
        }else{
            rentingFeePerSecond = priceWei.divide(BigInteger.valueOf(3600));
        }

        return  appContext.getServiceProvider().getContractService().deployRentContract(
                rentingFeePerSecond,
                depositWei,
                selectedTimeUnit,
                title,
                description,
                new ArrayList(imageSignatures.keySet()),
                needsVerification);
    }
}
