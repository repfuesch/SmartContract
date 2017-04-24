package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.csg.contract.async.broadcast.TransactionManager;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.common.Web3;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.service.account.UserProfile;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import ezvcard.Ezvcard;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.MessageHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.qrcode.QrScanningActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.validation.RequiredTextFieldValidator;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.ContractOverviewActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsProvider;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContractDeployFragment extends Fragment implements TextWatcher, RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private EditText priceField;
    private EditText titleField;
    private EditText descriptionField;
    private Button deployButton;
    private RadioGroup deployOptionsGroup;
    private ImageView qrImageView;
    private Spinner currencySpinner;
    private Currency selectedCurrency;

    private UserProfile verifiedProfile;
    private boolean isVerified;
    private boolean isValid;
    private boolean needsVerification;
    private OnProfileVerificationRequestedListener listener;
    private MessageHandler messageHandler;

    public ContractDeployFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_fragment_contract_create, container, false);

        needsVerification = false;
        isVerified = false;
        isValid = false;

        priceField = (EditText) view.findViewById(R.id.contract_price);
        priceField.addTextChangedListener(new RequiredTextFieldValidator(priceField));
        titleField = (EditText) view.findViewById(R.id.contract_title);
        titleField.addTextChangedListener(new RequiredTextFieldValidator(titleField));
        descriptionField = (EditText) view.findViewById(R.id.contract_description);
        descriptionField.addTextChangedListener(new RequiredTextFieldValidator(descriptionField));
        deployButton = (Button) view.findViewById(R.id.action_deploy_contract);
        deployButton.setEnabled(false);
        deployButton.setOnClickListener(this);

        deployOptionsGroup = (RadioGroup) view.findViewById(R.id.contract_options_radio_group);
        qrImageView = (ImageView) view.findViewById(R.id.action_scan_profile);
        qrImageView.setOnClickListener(this);

        deployOptionsGroup.setOnCheckedChangeListener(this);
        priceField.addTextChangedListener(this);
        titleField.addTextChangedListener(this);
        descriptionField.addTextChangedListener(this);

        currencySpinner = (Spinner) view.findViewById(R.id.contract_currency);

        final List<String> currencyList = new ArrayList<>();
        currencyList.add(Currency.EUR.toString());
        currencyList.add(Currency.USD.toString());
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, currencyList);

        currencySpinner.setAdapter(itemsAdapter);
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCurrency = Currency.valueOf(currencyList.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                currencySpinner.setSelection(0);
                selectedCurrency = Currency.valueOf(currencyList.get(0));
        }});
        itemsAdapter.notifyDataSetChanged();

        return view;
    }

    private boolean ensureBalance(BigInteger price)
    {
        String account = SettingsProvider.getInstance().getSelectedAccount();
        BigInteger balance = ServiceProvider.getInstance().getAccountService().getAccountBalance(account).get();
        if(balance.compareTo(price) < 0)
        {
            messageHandler.showMessage("You don't have enough money to do that!");
            return false;
        }

        return true;
    }

    public void deployContract()
    {
        final float price = Float.parseFloat(priceField.getText().toString());
        BigInteger priceWei = BigInteger.ZERO;

        Map<Currency, Float> currencyMap = ServiceProvider.getInstance().getExchangeService().getEthExchangeRates().get();
        if(currencyMap == null)
        {
            messageHandler.showMessage("Cannot reach exchange service. Please try again later!");
            return;
        }

        Float exchangeRate = currencyMap.get(selectedCurrency);
        float priceEther = price / exchangeRate;
        priceWei = Web3.toWei(BigDecimal.valueOf(priceEther));

        if(!(priceWei.mod(BigInteger.valueOf(2))).equals(BigInteger.ZERO))
        {
            priceWei = priceWei.add(BigInteger.ONE);
        }

        if(!ensureBalance(priceWei))
            return;

        final String title = titleField.getText().toString();
        final String desc = descriptionField.getText().toString();

        SimplePromise<IPurchaseContract> promise = ServiceProvider.getInstance().getContractService().deployContract(priceWei, title, desc, needsVerification)
                .done(new DoneCallback<IPurchaseContract>() {
                    @Override
                    public void onDone(IPurchaseContract result) {
                        result.setUserProfile(verifiedProfile);
                        ServiceProvider.getInstance().getContractService().saveContract(result, SettingsProvider.getInstance().getSelectedAccount());
                    }
                });

        TransactionManager.toTransaction(promise, null);

        Intent intent = new Intent(getActivity(), ContractOverviewActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof OnProfileVerificationRequestedListener)
        {
            listener = (OnProfileVerificationRequestedListener)context;
        }else{
            throw new RuntimeException("Context must implement OnProfileVerificationRequestedListener!");
        }

        if(context instanceof MessageHandler)
        {
            messageHandler = (MessageHandler) context;
        }else{
            throw new RuntimeException("Context must implement MessageHandler!");
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {

        if(titleField.getError() != null || priceField.getError() != null || descriptionField.getError() != null)
        {
            isValid = false;
        }else{
            isValid = true;
        }

        checkStatus();
    }

    private void checkStatus()
    {
        if(isValid && needsVerification && isVerified)
        {
            deployButton.setEnabled(true);
            return;
        }

        if(isValid && !needsVerification)
        {
            deployButton.setEnabled(true);
            return;
        }

        deployButton.setEnabled(false);
    }

    public void verifyIdentity(UserProfile profile)
    {
        verifiedProfile = profile;
        isVerified = true;
        checkStatus();
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

        switch(i)
        {
            case R.id.option_verification:
                needsVerification = true;
                isVerified = false;
                checkStatus();
                qrImageView.setVisibility(View.VISIBLE);
                listener.onProfileVerificationEnabled(true);
                break;
            default:
                qrImageView.setVisibility(View.GONE);
                needsVerification = false;
                isVerified = true;
                checkStatus();
                listener.onProfileVerificationEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.action_deploy_contract:
                deployContract();
                break;
            case R.id.action_cancel_deploy:
                Intent intent = new Intent(getActivity(), ContractOverviewActivity.class);
                startActivity(intent);
                break;
            case R.id.action_scan_profile:
                Intent scanIntent = new Intent(getActivity(), QrScanningActivity.class);
                scanIntent.setAction(QrScanningActivity.ACTION_SCAN_CONTRACT);
                startActivityForResult(
                        scanIntent,
                        ContractCreateActivity.SCAN_CONTRACT_INFO_REQUEST);
                break;


        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        switch (requestCode)
        {
            case ContractCreateActivity.SCAN_CONTRACT_INFO_REQUEST:
                if(intent == null)
                    return;

                String vCardString = intent.getStringExtra(QrScanningActivity.MESSAGE_SCAN_DATA);
                UserProfile profile = new UserProfile();
                profile.setVCard(Ezvcard.parse(vCardString).first());
                listener.onProfileVerificationRequested(profile);
                break;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }


    public static interface OnProfileVerificationRequestedListener
    {
        void onProfileVerificationEnabled(boolean enabled);
        void onProfileVerificationRequested(UserProfile profile);
    }
}
