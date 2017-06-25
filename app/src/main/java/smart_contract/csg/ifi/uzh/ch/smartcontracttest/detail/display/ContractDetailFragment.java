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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import net.glxn.qrgen.android.QRCode;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.common.ImageHelper;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.dialog.ImageDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.MessageHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.controls.ProportionalImageView;

public abstract class ContractDetailFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private TextView titleView;
    private TextView stateView;
    private TextView descriptionView;
    private TextView addressView;

    //private LinearLayout progressView;
    private LinearLayout contractInteractionView;
    private LinearLayout verifyIdentityView;
    private LinearLayout exportContractView;
    private LinearLayout imageContainer;

    private ProportionalImageView qrImageView;
    private Map<ProportionalImageView, Uri> images;

    private Spinner currencySpinner;
    private boolean isVerified;
    private List<String> currencyList;

    protected LinearLayout bodyView;
    protected Currency selectedCurrency;
    protected MessageHandler messageHandler;
    protected ApplicationContextProvider contextProvider;

    protected boolean verifyIdentity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view= inflater.inflate(getLayoutId(), container, false);

        titleView = (TextView) view.findViewById(R.id.general_title);
        stateView = (TextView) view.findViewById(R.id.general_state);
        descriptionView = (TextView) view.findViewById(R.id.general_description);
        addressView = (TextView) view.findViewById(R.id.general_address);

        //progressView = (LinearLayout) view.findViewById(R.id.progress_view);
        bodyView = (LinearLayout) view.findViewById(R.id.contract_info_body);
        contractInteractionView = (LinearLayout) view.findViewById(R.id.contract_interactions);

        qrImageView = (ProportionalImageView) view.findViewById(R.id.contract_qr_image);
        verifyIdentityView = (LinearLayout) view.findViewById(R.id.section_verify_identity);
        qrImageView.setOnClickListener(this);

        exportContractView = (LinearLayout) view.findViewById(R.id.section_export);

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

    public boolean isVerifyIdentity() {
        return verifyIdentity;
    }

    protected abstract ITradeContract getContract();

    protected abstract int getLayoutId();


    protected boolean ensureBalance(BigInteger value)
    {
        String account = contextProvider.getSettingProvider().getSelectedAccount();

        //todo: don't use blocking wait here!
        BigInteger balance = contextProvider.getServiceProvider().getAccountService().getAccountBalance(account).get();
        if(balance == null)
        {
            return false;
        }

        if(balance.compareTo(value) < 0)
        {
            messageHandler.showErrorMessage("You need at least " + value.toString() + " wei to do that!");
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View view)
    {
        //Prevent that user interacts with the contract when connection lost
        if(view.getParent().equals(contractInteractionView))
        {
            if(!contextProvider.getServiceProvider().getConnectionService().hasConnection())
            {
                messageHandler.showErrorMessage("Cannot interact with the contract when connection is lost!");
                return;
            }
        }

        switch(view.getId())
        {
            case R.id.contract_qr_image:
                DialogFragment imageDialog = new ImageDialogFragment();
                Bundle args = new Bundle();
                args.putString(ImageDialogFragment.MESSAGE_IMAGE_SOURCE, getContract().getContractAddress());
                args.putBoolean(ImageDialogFragment.MESSAGE_DISPLAY_QRCODE, true);
                imageDialog.setArguments(args);
                imageDialog.show(getFragmentManager(), "QrImageDialog");
            default:
                break;
        }
    }

    public boolean needsIdentityVerification()
    {
        return verifyIdentity;
    }

    public void verifyIdentity()
    {
        isVerified = true;
        contractInteractionView.setVisibility(View.VISIBLE);
        verifyIdentityView.setVisibility(View.GONE);
    }

    public SimplePromise<Void> init(final ITradeContract contract)
    {
        return Async.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                verifyIdentity = contract.getVerifyIdentity();
                final ContractState state = contract.getState();
                final String description = contract.getDescription();
                final String title = contract.getTitle();
                final List<String> imageSignatures = contract.getImageSignatures();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(verifyIdentity && contract.getUserProfile().getVCard() == null)
                        {
                            contractInteractionView.setVisibility(View.GONE);
                            verifyIdentityView.setVisibility(View.VISIBLE);
                        }

                        if(contract.getUserProfile().getVCard() != null)
                        {
                            exportContractView.setVisibility(View.GONE);
                        }

                        //create a bitmap image containing the qr code of the address and type of the contract
                        Bitmap bitmap = QRCode.from(contract.getContractAddress() + "," + contract.getContractType()).bitmap();
                        qrImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 125, 125, false));

                        stateView.setText(state.toString());
                        titleView.setText(title);
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
                    }
                });

                return null;
            }
        });
    }

    protected abstract void selectedCurrencyChanged();

    private void addImage(String filename)
    {
        try {
            final ProportionalImageView imageView = new ProportionalImageView(getActivity());
            int heightPx = (int)ImageHelper.convertDpToPixel(new Float(48.0), this.getActivity());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(heightPx, heightPx);
            layoutParams.setMargins(8,8,8,8);
            imageView.setLayoutParams(layoutParams);
            final Uri imgUri = Uri.fromFile(new File(filename));
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
        //init();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selectedCurrency = Currency.valueOf(currencyList.get(i));

        selectedCurrencyChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        currencySpinner.setSelection(0);
        selectedCurrency = Currency.valueOf(currencyList.get(0));

        selectedCurrencyChanged();
    }
}
