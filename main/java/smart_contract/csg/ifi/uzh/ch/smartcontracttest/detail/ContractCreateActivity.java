package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class ContractCreateActivity extends ActivityBase {

    public final static String CONTRACT_MESSAGE = "PENDING_CONTRACT_TRANSACTION_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getToolbar().setVisibility(View.GONE);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_contract_detail;
    }

    @Override
    protected void onSettingsChanged() {
    }

    @Override
    protected void onContractCreated(String contractAddress) {
    }

    public void onDeployContractButtonClick(final View view)
    {
        TextView priceField = (TextView)findViewById(R.id.contract_price);

        final BigInteger price = BigInteger.valueOf(Integer.parseInt(priceField.getText().toString()));
        if(!(price.mod(BigInteger.valueOf(2))).equals(BigInteger.ZERO))
        {
            //price must be dividable by 2
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
}
