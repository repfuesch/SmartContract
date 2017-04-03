package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create;


import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.math.BigInteger;

import ch.uzh.ifi.csg.contract.async.broadcast.TransactionManager;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.service.account.UserProfile;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.validation.RequiredTextFieldValidator;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.ContractOverviewActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsProvider;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContractDeployFragment extends Fragment implements TextWatcher, RadioGroup.OnCheckedChangeListener {

    private EditText priceField;
    private EditText titleField;
    private EditText descriptionField;
    private Button deployButton;
    private RadioGroup deployOptionsGroup;
    private ImageView qrImageView;

    private UserProfile verifiedProfile;
    private boolean isVerified;
    private boolean isValid;
    private boolean needsVerification;

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

        deployOptionsGroup = (RadioGroup) view.findViewById(R.id.contract_options_radio_group);
        qrImageView = (ImageView) view.findViewById(R.id.activity_qr_scanning);

        deployOptionsGroup.setOnCheckedChangeListener(this);
        priceField.addTextChangedListener(this);
        titleField.addTextChangedListener(this);
        descriptionField.addTextChangedListener(this);

        return view;
    }

    public void deployContract()
    {
        final BigInteger price = BigInteger.valueOf(Integer.parseInt(priceField.getText().toString()));
        if(!(price.mod(BigInteger.valueOf(2))).equals(BigInteger.ZERO))
        {
            priceField.setError("Price must be dividable by 2!");
            return;
        }

        final String title = titleField.getText().toString();
        final String desc = descriptionField.getText().toString();

        SimplePromise<IPurchaseContract> promise = ServiceProvider.getInstance().getContractService().deployContract(price, title, desc, needsVerification)
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
                break;
            default:
                qrImageView.setVisibility(View.GONE);
                needsVerification = false;
                isVerified = true;
                checkStatus();
        }
    }
}
