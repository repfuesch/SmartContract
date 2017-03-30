package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigInteger;

import ch.uzh.ifi.csg.contract.async.broadcast.TransactionManager;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.ContractOverviewActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

public class ContractCreateActivity extends ActivityBase implements TextWatcher {

    private EditText priceField;
    private EditText titleField;
    private EditText descriptionField;
    private Button deployButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_contract_create);

        priceField = (EditText) findViewById(R.id.contract_price);
        titleField = (EditText) findViewById(R.id.contract_title);
        descriptionField = (EditText) findViewById(R.id.contract_description);
        deployButton = (Button) findViewById(R.id.action_deploy_contract);
        deployButton.setEnabled(false);

        priceField.addTextChangedListener(this);
        titleField.addTextChangedListener(this);
        descriptionField.addTextChangedListener(this);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_contract_detail;
    }

    @Override
    protected void onSettingsChanged() {
    }

    public void onDeployContractButtonClick(final View view)
    {
        TextView priceField = (TextView)findViewById(R.id.contract_price);

        final BigInteger price = BigInteger.valueOf(Integer.parseInt(priceField.getText().toString()));
        if(!(price.mod(BigInteger.valueOf(2))).equals(BigInteger.ZERO))
        {
            showMessage("Price must be dividable by 2!");
            return;
        }

        final String title = ((TextView)findViewById(R.id.contract_title)).getText().toString();
        final String desc = ((TextView)findViewById(R.id.contract_description)).getText().toString();

        SimplePromise<IPurchaseContract> promise = ServiceProvider.getInstance().getContractService().deployContract(price, title, desc);
        TransactionManager.toTransaction(promise, null);

        Intent intent = new Intent(this, ContractOverviewActivity.class);
        startActivity(intent);
    }

    public void onCancelContractButtonClick(View view)
    {
        Intent intent = new Intent(this, ContractOverviewActivity.class);
        startActivity(intent);
    }

    public void onTextFieldClick(View view)
    {
        EditText textView = (EditText)view;
        textView.setText("");
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {

        if(titleField.getText().toString().isEmpty() ||
                priceField.getText().toString().isEmpty() ||
                descriptionField.getText().toString().isEmpty())
        {
            deployButton.setEnabled(false);
        }else{
            deployButton.setEnabled(true);
        }
    }
}
