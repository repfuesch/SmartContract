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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.contract.TimeUnit;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

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

    @Override
    protected SimplePromise<ITradeContract> deployContract(BigInteger priceWei, String title, String description, boolean needsVerification, final Map<String, File> imageSignatures)
    {
        float deposit = Float.parseFloat(depositField.getText().toString());
        BigInteger depositWei = convertToWei(deposit);
        if(depositWei == null)
            return null;

        return  contextProvider.getServiceProvider().getContractService().deployRentContract(
                priceWei,
                depositWei,
                selectedTimeUnit,
                title,
                description,
                new ArrayList(imageSignatures.keySet()),
                needsVerification);
    }
}
