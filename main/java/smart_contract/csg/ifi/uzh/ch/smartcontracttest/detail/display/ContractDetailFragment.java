package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display;

import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import net.glxn.qrgen.android.QRCode;

import org.jdeferred.Promise;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.csg.contract.async.broadcast.TransactionManager;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.common.Web3;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ImageDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.MessageHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.controls.ProportionalImageView;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsProvider;

public class ContractDetailFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private TextView titleView;
    private TextView stateView;
    private TextView priceView;
    private TextView descriptionView;
    private TextView addressView;
    private Button buyButton;
    private Button abortButton;
    private Button confirmButton;
    private LinearLayout bodyView;
    private LinearLayout progressView;
    private LinearLayout contractInteractionView;
    private LinearLayout verifyIdentityView;
    private ProportionalImageView qrImageView;
    private IPurchaseContract contract;
    private Spinner currencySpinner;
    private boolean isVerified;
    private List<String> currencyList;
    private Currency selectedCurrency;
    private MessageHandler messageHandler;

    public ContractDetailFragment() {
        // Required empty public constructor
    }

    public static ContractDetailFragment newInstance(String param1, String param2) {
        ContractDetailFragment fragment = new ContractDetailFragment();
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
        addressView = (TextView) view.findViewById(R.id.general_address);

        progressView = (LinearLayout) view.findViewById(R.id.progress_view);
        bodyView = (LinearLayout) view.findViewById(R.id.contract_info_body);
        contractInteractionView = (LinearLayout) view.findViewById(R.id.contract_interactions);

        buyButton = (Button) view.findViewById(R.id.buy_button);
        abortButton = (Button) view.findViewById(R.id.abort_button);
        confirmButton = (Button) view.findViewById(R.id.confirm_button);
        qrImageView = (ProportionalImageView) view.findViewById(R.id.contract_qr_image);
        verifyIdentityView = (LinearLayout) view.findViewById(R.id.section_verify_identity);

        buyButton.setOnClickListener(this);
        abortButton.setOnClickListener(this);
        confirmButton.setOnClickListener(this);
        qrImageView.setOnClickListener(this);

        currencySpinner = (Spinner) view.findViewById(R.id.contract_currency);
        currencyList = new ArrayList<>();
        currencyList.add(Currency.EUR.toString());
        currencyList.add(Currency.USD.toString());
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, currencyList);

        currencySpinner.setAdapter(itemsAdapter);
        currencySpinner.setOnItemSelectedListener(this);
        selectedCurrency = Currency.valueOf(currencyList.get(0));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof MessageHandler)
        {
            messageHandler = (MessageHandler)context;
        }else
        {
            throw new RuntimeException("Context must implement MessageHandler!");
        }
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
        updateView();
    }

    private boolean ensureBalance()
    {
        String account = SettingsProvider.getInstance().getSelectedAccount();
        BigInteger balance = ServiceProvider.getInstance().getAccountService().getAccountBalance(account).get();
        BigInteger value = contract.value().get().multiply(BigInteger.valueOf(2));
        if(balance.compareTo(value) < 0)
        {
            messageHandler.showMessage("You need at least " + value.toString() + " wei to do that!");
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.buy_button:
                if(!ensureBalance())
                    return;

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
            case R.id.contract_qr_image:
                DialogFragment imageDialog = new ImageDialogFragment();
                Bundle args = new Bundle();
                args.putString(ImageDialogFragment.MESSAGE_IMAGE_SOURCE, contract.getContractAddress());
                args.putBoolean(ImageDialogFragment.MESSAGE_DISPLAY_QRCODE, true);
                imageDialog.setArguments(args);
                imageDialog.show(getFragmentManager(), "QrImageDialog");
            default:
                break;
        }
    }

    public void verifyIdentity()
    {
        isVerified = true;
        contractInteractionView.setVisibility(View.VISIBLE);
        verifyIdentityView.setVisibility(View.GONE);
    }

    public void updateView()
    {
        ContractState state = contract.state().get();
        BigInteger value = contract.value().get();
        String description = contract.description().get();
        String seller = contract.seller().get();
        String buyer = contract.buyer().get();

        String selectedAccount = SettingsProvider.getInstance().getSelectedAccount();

        titleView.setText(contract.title().get());
        if(state != null)
            stateView.setText(state.toString());

        if(value != null)
        {
            Map<Currency, Float> currencyMap = ServiceProvider.getInstance().getExchangeService().getEthExchangeRates().get();
            if(currencyMap != null)
            {
                BigDecimal amountEther = Web3.toEther(value);
                Float amountCurrency = amountEther.floatValue() * currencyMap.get(selectedCurrency);
                priceView.setText(amountCurrency.toString());
            }
        }

        if(description != null)
            descriptionView.setText(description);

        addressView.setText(contract.getContractAddress());

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

    public void identityVerified()
    {
        contractInteractionView.setVisibility(View.VISIBLE);
        verifyIdentityView.setVisibility(View.GONE);
        updateView();
    }

    public void setContract(IPurchaseContract contract)
    {
        this.contract = contract;
        Boolean verifyIdentity = contract.verifyIdentity().get();

        if(verifyIdentity && contract.getUserProfile().getVCard() == null)
        {
            contractInteractionView.setVisibility(View.GONE);
            verifyIdentityView.setVisibility(View.VISIBLE);
        }

        Bitmap bitmap = QRCode.from(contract.getContractAddress()).bitmap();
        qrImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 125, 125, false));
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selectedCurrency = Currency.valueOf(currencyList.get(i));
        updateView();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        currencySpinner.setSelection(0);
        selectedCurrency = Currency.valueOf(currencyList.get(0));
        updateView();
    }
}