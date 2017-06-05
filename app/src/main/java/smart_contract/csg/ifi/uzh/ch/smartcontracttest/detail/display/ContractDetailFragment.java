package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
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

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.csg.contract.common.ImageHelper;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.common.Web3Util;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.dialog.ImageDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.MessageHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.controls.ProportionalImageView;

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
    private LinearLayout imageContainer;

    private ProportionalImageView qrImageView;
    private Map<ProportionalImageView, Uri> images;

    private IPurchaseContract contract;

    private Spinner currencySpinner;
    private boolean isVerified;
    private List<String> currencyList;
    private Currency selectedCurrency;

    private MessageHandler messageHandler;
    private ApplicationContextProvider contextProvider;

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
        View view= inflater.inflate(R.layout.fragment_purchase_contract_detail, container, false);

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

        imageContainer = (LinearLayout)view.findViewById(R.id.image_container);
        images = new LinkedHashMap<>();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        attachContext(context);
    }

    private void attachContext(Context context)
    {
        if(context instanceof MessageHandler)
        {
            messageHandler = (MessageHandler)context;
        }else
        {
            throw new RuntimeException("Context must implement MessageHandler!");
        }

        if(context instanceof ApplicationContextProvider)
        {
            contextProvider = (ApplicationContextProvider) context;
        }else
        {
            throw new RuntimeException("Context must implement ApplicationContextProvider!");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attachContext(activity);
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
        String account = contextProvider.getSettingProvider().getSelectedAccount();
        BigInteger balance = contextProvider.getServiceProvider().getAccountService().getAccountBalance(account).get();
        BigInteger value = contract.getPrice().get().multiply(BigInteger.valueOf(2));
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
                contextProvider.getTransactionManager().toTransaction(buyPromise, contract.getContractAddress());
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
                contextProvider.getTransactionManager().toTransaction(abortPromise, contract.getContractAddress());
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
                contextProvider.getTransactionManager().toTransaction(confirmPromise, contract.getContractAddress());
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
        ContractState state = contract.getState().get();
        BigInteger value = contract.getPrice().get();
        String description = contract.getDescription().get();
        String seller = contract.getSeller().get();
        String buyer = contract.getBuyer().get();
        List<String> imageSignatures = contract.getImageSignatures().get();

        String selectedAccount = contextProvider.getSettingProvider().getSelectedAccount();

        titleView.setText(contract.getTitle().get());
        if(state != null)
            stateView.setText(state.toString());

        if(value != null)
        {
            Map<Currency, Float> currencyMap = contextProvider.getServiceProvider().getExchangeService().getEthExchangeRates().get();
            if(currencyMap != null)
            {
                BigDecimal amountEther = Web3Util.toEther(value);
                Float amountCurrency = amountEther.floatValue() * currencyMap.get(selectedCurrency);
                priceView.setText(amountCurrency.toString());
            }
        }

        if(description != null)
            descriptionView.setText(description);

        imageContainer.removeAllViews();
        images.clear();

        if(imageSignatures != null)
        {
            for(String sig : imageSignatures)
            {
                addImage(contract.getImages().get(sig));
            }
        }

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

    private void addImage(String filename)
    {
        try {
            final ProportionalImageView imageView = new ProportionalImageView(getActivity());
            int heightPx = (int)ImageHelper.convertDpToPixel(new Float(48.0), this.getActivity());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(heightPx, heightPx);
            layoutParams.setMargins(8,8,8,8);
            imageView.setLayoutParams(layoutParams);
            final Uri imgUri = Uri.fromFile(new File(contextProvider.getSettingProvider().getProfileImageDirectory() + "/" + filename));
            imageView.setImageURI(imgUri);
            imageContainer.addView(imageView);
            images.put(imageView, imgUri);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showImageDialog(imageView);
                }
            });

        }
        catch (Exception e) {
            messageHandler.showSnackBarMessage(e.getMessage(), Snackbar.LENGTH_LONG);
        }
    }

    private void showImageDialog(ProportionalImageView imageView)
    {
        ArrayList<Uri> uris = new ArrayList<>(images.values());
        int startIndex = uris.indexOf(images.get(imageView));

        DialogFragment imageDialog = new ImageDialogFragment();
        Bundle imageArgs = new Bundle();
        imageArgs.putSerializable(ImageDialogFragment.MESSAGE_IMAGE_URIS, new ArrayList<>(images.values()));
        imageArgs.putInt(ImageDialogFragment.MESSAGE_IMAGE_INDEX, startIndex);
        imageDialog.setArguments(imageArgs);
        imageDialog.show(getFragmentManager(), "ImageDialog");
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
        Boolean verifyIdentity = contract.getVerifyIdentity().get();

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
