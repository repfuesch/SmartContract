package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import net.glxn.qrgen.android.QRCode;
import org.jdeferred.Promise;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.util.ImageHelper;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContext;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.dialog.ImageDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.controls.ProportionalImageView;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;

/**
 * {@link Fragment} that displays the details of an {@link ITradeContract} instance and provides
 * UI-elements to execute transactions on the smart contract on the blockchain that belongs to this
 * contract.
 */
public abstract class ContractDetailFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private TextView titleView;
    private TextView stateView;
    private TextView descriptionView;
    private TextView addressView;

    private LinearLayout contractInteractionView;
    private LinearLayout imageContainer;

    private ProportionalImageView qrImageView;
    private Map<ProportionalImageView, Bitmap> images;

    private Spinner currencySpinner;
    private List<String> currencyList;

    protected LinearLayout bodyView;
    protected Currency selectedCurrency;
    protected ApplicationContext appContext;

    protected boolean verifyIdentity;
    private String seller;
    private ContractState state;
    private ITradeContract contract;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view= inflater.inflate(getLayoutId(), container, false);

        titleView = (TextView) view.findViewById(R.id.general_title);
        stateView = (TextView) view.findViewById(R.id.general_state);
        descriptionView = (TextView) view.findViewById(R.id.general_description);
        addressView = (TextView) view.findViewById(R.id.general_address);

        bodyView = (LinearLayout) view.findViewById(R.id.contract_info_body);
        contractInteractionView = (LinearLayout) view.findViewById(R.id.contract_interactions);

        qrImageView = (ProportionalImageView) view.findViewById(R.id.contract_qr_image);
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
        if(context instanceof ApplicationContextProvider)
        {
            appContext = ((ApplicationContextProvider) context).getAppContext();
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

    protected abstract ITradeContract getContract();

    protected abstract int getLayoutId();

    /**
     * Checks if the account balance is higher than the specified value
     * @param value
     * @return
     */
    protected boolean ensureBalance(BigInteger value)
    {
        String account = appContext.getSettingProvider().getSelectedAccount();

        BigInteger balance = appContext.getServiceProvider().getAccountService().getAccountBalance(account).get();
        if(balance == null)
        {
            appContext.getMessageService().showErrorMessage("Cannot reach the exchange service. Try again later.");
            return false;
        }

        if(balance.compareTo(value) < 0)
        {
            appContext.getMessageService().showErrorMessage("You need at least " + value.toString() + " wei to do that!");
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.contract_qr_image:
                //display the QR-code for this contract in a Dialog
                DialogFragment imageDialog = new ImageDialogFragment();
                Bundle args = new Bundle();
                args.putString(ImageDialogFragment.MESSAGE_IMAGE_SOURCE, contract.toJson());
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

    public String getSeller(){ return seller; }

    public ContractState getState(){return state; }

    public void identityVerified()
    {
        contractInteractionView.setVisibility(View.VISIBLE);
    }

    /**
     * Initializes the UI elements from the provided contract instance
     * @param contract
     * @return
     */
    public SimplePromise<Void> init(final ITradeContract contract)
    {
        BusyIndicator.show(bodyView);
        return Async.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                //retrieve the details of this contract
                ContractDetailFragment.this.contract = contract;
                verifyIdentity = contract.getVerifyIdentity().get();
                state = contract.getState().get();
                seller = contract.getSeller().get();

                //check if the local content matches the remote content hash (used for light contracts only)
                final boolean contentVerified = contract.verifyContent().get();

                final String description = contract.getDescription().get();
                final String title = contract.getTitle().get();
                final List<String> imageSignatures = contract.getImageSignatures().get();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(verifyIdentity && contract.getUserProfile().getVCard() == null)
                        {
                            //hide interaction view and display notification if identity verification is required
                            contractInteractionView.setVisibility(View.GONE);
                            appContext.getMessageService().showMessage("You must first scan the user profile of the other party to interact with this contract!");
                        }

                        if(!contentVerified)
                        {
                            //notify the user that the local and remote content do not match (only for light contracts)
                            //This can happen when the user scanned the contract by QR-Code and the contract contains images...
                            appContext.getMessageService().showMessage("The content for this local contract could not be verified.\n Please try to import the contract again.");
                        }

                        //create a bitmap image that contains a QR code with all the details of the contract
                        Bitmap bm = QRCode.from(contract.toJson()).withSize(250, 250).bitmap();
                        qrImageView.setImageBitmap(bm);

                        //init text views
                        stateView.setText(state.toString());
                        titleView.setText(title);
                        descriptionView.setText(description);
                        addressView.setText(contract.getContractAddress());

                        //add images to container
                        imageContainer.removeAllViews();
                        images.clear();

                        if(imageSignatures != null)
                        {
                            for(String sig : imageSignatures)
                            {
                                addImage(contract.getImages().get(sig));
                            }
                        }
                    }
                });

                return null;
            }
        }).always(new AlwaysCallback<Void>() {
            @Override
            public void onAlways(Promise.State state, Void resolved, Throwable rejected) {
                BusyIndicator.hide(bodyView);
            }
        });
    }

    /**
     * Invoked when the user select another currency. Must be implemented by derived Fragments
     * to update their UI
     */
    protected abstract void selectedCurrencyChanged();

    /**
     * Creates a {@link ProportionalImageView} from the specified path and adds it to the image container
     *
     * @param filepath
     */
    private void addImage(String filepath)
    {
        final ProportionalImageView imageView = new ProportionalImageView(getActivity());
        imageView.setScale(ProportionalImageView.ScaleDimension.Height);
        int heightPx = (int)ImageHelper.convertDpToPixel(new Float(64.0), this.getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(heightPx, heightPx);
        layoutParams.setMargins(8,8,8,8);
        imageView.setLayoutParams(layoutParams);
        Bitmap bmp = BitmapFactory.decodeFile(filepath);
        imageView.setImageBitmap(bmp);

        imageContainer.addView(imageView);
        images.put(imageView, bmp);

        //register clickListener to display image in Dialog
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageDialog(imageView);
            }
        });
    }

    /**
     * Displays an image in an {@link ImageDialogFragment}
     *
     * @param imageView
     */
    private void showImageDialog(ProportionalImageView imageView)
    {
        ArrayList<Bitmap> uris = new ArrayList<>(images.values());
        int startIndex = uris.indexOf(images.get(imageView));

        DialogFragment imageDialog = new ImageDialogFragment();
        Bundle imageArgs = new Bundle();
        imageArgs.putSerializable(ImageDialogFragment.MESSAGE_IMAGE_BMPS, new ArrayList<>(images.values()));
        imageArgs.putInt(ImageDialogFragment.MESSAGE_IMAGE_INDEX, startIndex);
        imageDialog.setArguments(imageArgs);
        imageDialog.show(getFragmentManager(), "ImageDialog");
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
