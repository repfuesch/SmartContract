package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import org.jdeferred.Promise;

import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.event.IContractObserver;
import ch.uzh.ifi.csg.contract.setting.EthSettings;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.ContractOverviewActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

public class ContractDetailActivity extends ActivityBase implements IContractObserver {

    public final static String ACTION_SHOW_CONTRACT_DETAILS = "ch.uzh.ifi.csg.smart_contract.detail";
    public final static String MESSAGE_SHOW_CONTRACT_DETAILS = "ch.uzh.ifi.csg.smart_contract.detail.address";

    private ContractGeneralInfoFragment generalInfoFragment;
    private IPurchaseContract contract;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_contract_detail);

        Intent intent = getIntent();
        String contractAddress = intent.getStringExtra(MESSAGE_SHOW_CONTRACT_DETAILS);
        generalInfoFragment = (ContractGeneralInfoFragment) getFragmentManager().findFragmentById(R.id.general_info);
        initTabHost();

        LoadContract(contractAddress);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        contract.deleteObserver(this);
    }

    private void LoadContract(String contractAddress)
    {
        ServiceProvider.getInstance().getContractService().loadContract(contractAddress).always(new AlwaysCallback<IPurchaseContract>() {
            @Override
            public void onAlways(Promise.State state, final IPurchaseContract resolved, Throwable rejected) {

                if(rejected != null)
                {
                    handleError(rejected);
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contract = resolved;
                        generalInfoFragment.setContract(resolved);
                        generalInfoFragment.updateView();
                        resolved.addObserver(ContractDetailActivity.this);
                    }
                });
            }
        });
    }

    private void initTabHost()
    {
        TabHost host = (TabHost)findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("General");
        spec.setContent(R.id.general_info);
        spec.setIndicator("General");
        host.addTab(spec);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_contract_detail2;
    }

    @Override
    protected void onSettingsChanged() {
        LoadContract(contract.getContractAddress());
    }

    @Override
    public void contractStateChanged(String event, Object value) {
        generalInfoFragment.updateView();
    }
}
