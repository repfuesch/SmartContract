package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.jdeferred.Promise;

import ch.uzh.ifi.csg.contract.async.broadcast.TransactionManager;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsProvider;

public class ContractGeneralInfoFragment extends Fragment implements View.OnClickListener {

    private TextView titleView;
    private TextView stateView;
    private TextView priceView;
    private TextView descriptionView;
    private Button buyButton;
    private Button abortButton;
    private Button confirmButton;
    private Button showDetailsButton;
    private LinearLayout bodyView;
    private LinearLayout progressView;
    private LinearLayout contractInteractionView;

    private IPurchaseContract contract;

    public ContractGeneralInfoFragment() {
        // Required empty public constructor
    }

    public static ContractGeneralInfoFragment newInstance(String param1, String param2) {
        ContractGeneralInfoFragment fragment = new ContractGeneralInfoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_contract_general_info, container, false);

        titleView = (TextView) view.findViewById(R.id.general_title);
        stateView = (TextView) view.findViewById(R.id.general_state);
        priceView = (TextView) view.findViewById(R.id.general_price);
        descriptionView = (TextView) view.findViewById(R.id.general_description);

        bodyView = (LinearLayout) view.findViewById(R.id.general_information_body);
        progressView = (LinearLayout) view.findViewById(R.id.progress_view);
        contractInteractionView = (LinearLayout) view.findViewById(R.id.contract_interactions);

        buyButton = (Button) view.findViewById(R.id.buy_button);
        abortButton = (Button) view.findViewById(R.id.abort_button);
        confirmButton = (Button) view.findViewById(R.id.confirm_button);

        buyButton.setOnClickListener(this);
        abortButton.setOnClickListener(this);
        confirmButton.setOnClickListener(this);

        return view;
    }

    private void disableInteractions()
    {
        contractInteractionView.setEnabled(false);
    }

    private void enableInteractions()
    {
        contractInteractionView.setEnabled(true);
    }

    private void showProgressView()
    {
        progressView.setVisibility(View.VISIBLE);
        bodyView.setVisibility(View.GONE);
    }

    private void hideProgressView()
    {
        bodyView.setVisibility(View.VISIBLE);
        progressView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.buy_button:
                showProgressView();
                SimplePromise buyPromise = contract.confirmPurchase().always(new AlwaysCallback<String>() {
                    @Override
                    public void onAlways(Promise.State state, String resolved, Throwable rejected) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressView();
                        }
                    });
                    }
                });
                TransactionManager.toTransaction(buyPromise, contract.getContractAddress());
                break;
            case R.id.abort_button:
                showProgressView();
                SimplePromise abortPromise = contract.abort().always(new AlwaysCallback<String>() {
                    @Override
                    public void onAlways(Promise.State state, String resolved, Throwable rejected) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressView();
                            }
                        });
                    }
                });
                TransactionManager.toTransaction(abortPromise, contract.getContractAddress());
                break;
            case R.id.confirm_button:
                showProgressView();
                SimplePromise confirmPromise = contract.confirmReceived().always(new AlwaysCallback<String>() {
                    @Override
                    public void onAlways(Promise.State state, String resolved, Throwable rejected) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgressView();
                            }
                        });
                    }
                });
                TransactionManager.toTransaction(confirmPromise, contract.getContractAddress());
                break;
            default:
                break;
        }
    }

    public void updateView()
    {
        ContractState state = contract.state().get();
        Integer value = contract.value().get();
        String description = contract.description().get();
        String seller = contract.seller().get();
        String buyer = contract.buyer().get();
        String selectedAccount = SettingsProvider.getInstance().getSelectedAccount();

        titleView.setText(contract.title().get());
        if(state != null)
            stateView.setText(state.toString());

        if(value != null)
            priceView.setText(value.toString() + " ETH");

        if(description != null)
            descriptionView.setText(description);

        if(state.equals(ContractState.Created) && !seller.equals(selectedAccount))
        {
            buyButton.setEnabled(true);
        }else{
            buyButton.setEnabled(false);
        }

        if(state.equals(ContractState.Created) && seller.equals(selectedAccount))
        {
            abortButton.setEnabled(true);
        }else{
            abortButton.setEnabled(false);
        }

        if(state.equals(ContractState.Locked) && buyer.equals(selectedAccount))
        {
            confirmButton.setEnabled(true);
        }else{
            confirmButton.setEnabled(false);
        }
    }


    public void setContract(IPurchaseContract contract) {
        this.contract = contract;
    }
}
