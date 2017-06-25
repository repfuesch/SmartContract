package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create;


import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.csg.contract.common.ImageHelper;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.controls.ProportionalImageView;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.dialog.ImageDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.common.Web3Util;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import ezvcard.Ezvcard;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.MessageHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.qrcode.QrScanningActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.validation.RequiredTextFieldValidator;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.ContractOverviewActivity;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class ContractDeployFragment extends Fragment implements TextWatcher, RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private EditText priceField;
    private EditText titleField;
    private EditText descriptionField;
    private Button deployButton;
    private Button cancelButton;
    private RadioGroup deployOptionsGroup;
    private ImageView qrImageView;
    private Spinner currencySpinner;
    private Currency selectedCurrency;
    private ImageButton addImageButton;
    private LinearLayout imageContainer;

    private Map<ProportionalImageView, Uri> images;
    private ProportionalImageView selectedImage;

    private boolean needsVerification;
    private boolean isValid;
    private MessageHandler messageHandler;

    protected ApplicationContextProvider contextProvider;

    public ContractDeployFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(getLayoutId(), container, false);

        needsVerification = false;

        priceField = (EditText) view.findViewById(R.id.contract_price);
        priceField.addTextChangedListener(new RequiredTextFieldValidator(priceField));
        titleField = (EditText) view.findViewById(R.id.contract_title);
        titleField.addTextChangedListener(new RequiredTextFieldValidator(titleField));
        descriptionField = (EditText) view.findViewById(R.id.contract_description);
        descriptionField.addTextChangedListener(new RequiredTextFieldValidator(descriptionField));

        deployButton = (Button) view.findViewById(R.id.action_deploy_contract);
        deployButton.setEnabled(false);
        deployButton.setOnClickListener(this);

        cancelButton = (Button) view.findViewById(R.id.action_cancel_deploy);
        cancelButton.setOnClickListener(this);

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

        addImageButton = (ImageButton) view.findViewById(R.id.action_add_image);
        addImageButton.setOnClickListener(this);
        registerForContextMenu(addImageButton);

        imageContainer = (LinearLayout) view.findViewById(R.id.image_container);
        images = new LinkedHashMap<>();

        return view;
    }

    protected abstract int getLayoutId();

    protected Boolean ensureBalance(final BigInteger value)
    {
        String account = contextProvider.getSettingProvider().getSelectedAccount();

        //todo: find a way to handle exceptions here
        BigInteger balance = contextProvider.getServiceProvider().getAccountService().getAccountBalance(account).get();
        if(balance == null)
        {
            return false;
        }

        if(balance.compareTo(value) < 0)
        {
            messageHandler.showErrorMessage("You don't have enough money to do that!");
            return false;
        }

        return true;
    }

    protected BigInteger convertToWei(float value)
    {
        Map<Currency, Float> currencyMap;
        currencyMap = contextProvider.getServiceProvider().getExchangeService().getEthExchangeRatesAsync().get();
        if(currencyMap == null)
        {
            messageHandler.showErrorMessage("Cannot reach exchange service. Please try again later!");
            return null;
        }

        Float exchangeRate = currencyMap.get(selectedCurrency);
        float priceEther = value / exchangeRate;
        return Web3Util.toWei(BigDecimal.valueOf(priceEther));
    }

    public void deploy()
    {
        final float price = Float.parseFloat(priceField.getText().toString());
        BigInteger priceWei = convertToWei(price);
        if(priceWei == null)
            return;

        final String title = titleField.getText().toString();
        final String desc = descriptionField.getText().toString();

        final Map<String, File> imageSignatures = new HashMap<>();

        for(Uri uri : images.values())
        {
            try{
                Bitmap bmp = ImageHelper.getCorrectlyOrientedImage(getActivity(), uri, 800);
                File imgFile = ImageHelper.saveBitmap(bmp, contextProvider.getSettingProvider().getProfileImageDirectory());
                String hashSig = ImageHelper.getHash(bmp);
                imageSignatures.put(hashSig, imgFile);
            }catch(IOException ex)
            {
                messageHandler.showErrorMessage("Could not save image: " + ex.getMessage());
                return;
            }
        }

        SimplePromise<ITradeContract> promise = deployContract(priceWei, title, desc, needsVerification, imageSignatures)
                .done(new DoneCallback<ITradeContract>() {
                    @Override
                    public void onDone(ITradeContract result) {
                        //add image paths to contract after creation
                        for(String sig : imageSignatures.keySet())
                        {
                            result.getImages().put(sig, imageSignatures.get(sig).getAbsolutePath());
                        }

                        //persist contract on the file system
                        contextProvider.getServiceProvider().getContractService().saveContract(result, contextProvider.getSettingProvider().getSelectedAccount());
                    }
                });

        contextProvider.getTransactionManager().toTransaction(promise);

        Intent intent = new Intent(getActivity(), ContractOverviewActivity.class);
        startActivity(intent);
    }

    protected abstract SimplePromise<ITradeContract> deployContract(BigInteger priceWei, String title, String description, boolean needsVerification, Map<String, File> imageSignatures);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        attachContext(context);
    }

    private void attachContext(Context context)
    {
        if(context instanceof MessageHandler)
        {
            messageHandler = (MessageHandler) context;
        }else{
            throw new RuntimeException("Context must implement MessageHandler!");
        }

        if(context instanceof ApplicationContextProvider)
        {
            contextProvider = (ApplicationContextProvider) context;
        }else{
            throw new RuntimeException("Context must implement ApplicationContextProvider!");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attachContext(activity);
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
            deployButton.setEnabled(true);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

        switch(i)
        {
            case R.id.option_verification:
                needsVerification = true;
                break;
            default:
                qrImageView.setVisibility(View.GONE);
                needsVerification = false;
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.action_deploy_contract:
                if(contextProvider.getServiceProvider().getConnectionService().hasConnection())
                {
                    deploy();
                }else{
                    messageHandler.showErrorMessage("Cannot deploy contract when connection to host is not established!");
                }
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
            case R.id.action_add_image:
                getActivity().openContextMenu(addImageButton);
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v.getId() == R.id.action_add_image)
        {
            menu.setHeaderTitle("Select an Image");
            menu.add(0, v.getId(), 0, "from file");
            menu.add(0, v.getId(), 0, "from camera");
        }else if (v instanceof ProportionalImageView)
        {
            menu.setHeaderTitle("Delete image?");
            menu.add(0, v.getId(), 0, "delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if(item.getTitle().equals("from file")){
            ImageHelper.openFile(this);
        }
        else if(item.getTitle().equals("from camera")){
            ImageHelper.makePicture(this);
        }
        else if(item.getTitle().equals("delete"))
        {
            if(selectedImage == null)
                return false;

            removeImage(selectedImage);
        }else{
            return false;
        }

        return true;
    }

    private void removeImage(ProportionalImageView image)
    {
        unregisterForContextMenu(selectedImage);
        imageContainer.removeView(selectedImage);
        images.remove(selectedImage);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        switch (requestCode)
        {
            case ImageHelper.PICK_IMAGE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    addImage(intent.getData());
                }
                break;
            case ImageHelper.IMAGE_CAPTURE_REQUEST_CODE:
                if(resultCode == RESULT_OK) {
                    addImage(intent.getData());
                }
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    private void addImage(Uri uri)
    {
        final ProportionalImageView imageView = new ProportionalImageView(getActivity());
        int heightPx = (int)ImageHelper.convertDpToPixel(new Float(48.0), this.getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(heightPx, heightPx);
        layoutParams.setMargins(8,8,8,8);
        imageView.setLayoutParams(layoutParams);
        final Uri imgUri = uri;
        imageView.setImageURI(imgUri);
        imageContainer.addView(imageView);
        images.put(imageView, imgUri);

        registerForContextMenu(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageDialog(imageView);
            }
        });

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                selectedImage = imageView;
                getActivity().openContextMenu(selectedImage);
                return true;
            }
        });
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
}
