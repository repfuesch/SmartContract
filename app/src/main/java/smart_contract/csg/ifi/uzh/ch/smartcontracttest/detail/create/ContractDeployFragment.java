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
import android.util.Log;
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
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.FailCallback;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import ch.uzh.ifi.csg.contract.util.ImageHelper;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.controls.ProportionalImageView;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.dialog.ImageDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.permission.PermissionProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContext;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.util.Web3Util;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.validation.RequiredTextFieldValidator;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.ContractOverviewActivity;

import static android.app.Activity.RESULT_OK;

/**
 * A {@link Fragment} that contains the UI elements to specify an {@link ITradeContract}.
 */
public abstract class ContractDeployFragment extends Fragment implements TextWatcher, RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    public static final String TAG = "TAG.Deploy";

    private EditText priceField;
    private EditText titleField;
    private EditText descriptionField;
    private Button deployButton;
    private Button cancelButton;

    private RadioGroup verifyOptionsGroup;
    private RadioGroup deployOptionsGroup;

    private Spinner currencySpinner;
    protected Currency selectedCurrency;
    protected List<Currency> currencies;

    private ImageButton addImageButton;
    private LinearLayout imageContainer;
    private Map<ProportionalImageView, Bitmap> images;
    private ProportionalImageView selectedImage;

    private boolean needsVerification;
    protected boolean deployFull;

    protected ApplicationContext appContext;

    public ContractDeployFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        images = new LinkedHashMap<>();
        currencies = Arrays.asList(Currency.values());
        deployFull = true;

        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(images == null)
            return;

        //Add images again to the imageContainer when the Fragment is re-created after orientation
        // changes
        if(images.size() > 0)
        {
            List<Bitmap> bitmaps = new ArrayList<>(images.values());
            for(ProportionalImageView imageView : images.keySet())
            {
                unregisterForContextMenu(imageView);
                imageContainer.removeView(imageView);
            }

            images.clear();

            for(Bitmap bmp : bitmaps)
                addImage(bmp);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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
        verifyOptionsGroup = (RadioGroup) view.findViewById(R.id.contract_verify_options_radio_group);
        verifyOptionsGroup.setOnCheckedChangeListener(this);

        deployOptionsGroup = (RadioGroup) view.findViewById(R.id.contract_deploy_options_radio_group);
        deployOptionsGroup.setOnCheckedChangeListener(this);

        priceField.addTextChangedListener(this);
        titleField.addTextChangedListener(this);
        descriptionField.addTextChangedListener(this);

        currencySpinner = (Spinner) view.findViewById(R.id.contract_currency);
        ArrayAdapter<Currency> itemsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, currencies);

        currencySpinner.setAdapter(itemsAdapter);
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setSelectedCurrency(currencies.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                currencySpinner.setSelection(0);
                setSelectedCurrency(currencies.get(0));
        }});
        itemsAdapter.notifyDataSetChanged();

        addImageButton = (ImageButton) view.findViewById(R.id.action_add_image);
        addImageButton.setOnClickListener(this);
        registerForContextMenu(addImageButton);

        imageContainer = (LinearLayout) view.findViewById(R.id.image_container);

        return view;
    }

    /**
     * Abstract getter to obtain the LayoutId of derived fragments
     *
     * @return
     */
    protected abstract int getLayoutId();

    protected void setSelectedCurrency(Currency currency)
    {
        selectedCurrency = currency;
    }

    /**
     * Checks that the account balance is higher than the specified value.
     *
     * @param value
     * @return
     */
    protected Boolean ensureBalance(final BigInteger value)
    {
        String account = appContext.getSettingProvider().getSelectedAccount();

        BigInteger balance = appContext.getServiceProvider().getAccountService().getAccountBalance(account).get();
        if(balance == null)
        {
            //cannot reach exchange service
            appContext.getMessageService().showErrorMessage("Cannot reach exchange service. Please try again later");
            return false;
        }

        if(balance.compareTo(value) < 0)
        {
            appContext.getMessageService().showErrorMessage("You don't have enough money to do that!");
            return false;
        }

        return true;
    }

    /**
     * Deploys an {@link ITradeContract} instance with the attributes specified by the user.
     */
    public void deploy()
    {
        final String title = titleField.getText().toString();
        final String desc = descriptionField.getText().toString();
        final BigDecimal price = new BigDecimal(priceField.getText().toString());

        Async.run(new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                final Map<String, String> imageSignatures = new HashMap<>();
                for(Bitmap bmp : images.values())
                {
                    //save selected images in the image directory of the application
                    File imgFile = ImageHelper.saveBitmap(bmp, appContext.getSettingProvider().getImageDirectory());
                    //calculate the hash of the image
                    String hashSig = ImageHelper.getImageHash(bmp);
                    imageSignatures.put(hashSig, imgFile.getAbsolutePath());
                }

                //Convert currency to ether
                BigDecimal priceEther = appContext.getServiceProvider().getExchangeService().convertToEther(price, selectedCurrency).get();
                if(priceEther == null)
                {
                    appContext.getMessageService().showErrorMessage("Cannot reach the exchange service. Try again later.");
                    return null;
                }

                BigInteger priceWei = Web3Util.toWei(priceEther);

                //Deploy the contract using the concrete deploy method
                deployContract(priceWei, title, desc, needsVerification, imageSignatures);
                return null;

            }
        }).fail(new FailCallback() {
            @Override
            public void onFail(Throwable result) {
                //notify the user that an unexpected error occurred
                Log.e("deploy", "An unexpected error occurred during deployment", result);

                appContext.getMessageService().showErrorMessage(
                        "Cannot deploy the contract. \n An unexpected error occurred");
            }
        });

        //return to OverviewActivity
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getActivity(), ContractOverviewActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Method that deploys a concrete instance of {@link ITradeContract}. This method must be
     * implemented by all concrete derived fragment classes.
     */
    protected abstract void deployContract(BigInteger priceWei, String title, String description, boolean needsVerification, Map<String, String> imageSignatures);

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
        }else{
            throw new RuntimeException("Context must implement ApplicationContext!");
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

        if(verifyFields())
        {
            deployButton.setEnabled(true);
        }else{
            deployButton.setEnabled(false);
        }
    }

    /**
     * Verifies that all values on the fields are set.
     *
     * @return
     */
    protected boolean verifyFields()
    {
        if(titleField.getError() != null || priceField.getError() != null || descriptionField.getError() != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

        if(radioGroup.getId() == R.id.contract_verify_options_radio_group)
        {
            if(i == R.id.option_verification)
            {
                needsVerification = true;
            }else{
                needsVerification = false;
            }
        }else if(radioGroup.getId() == R.id.contract_deploy_options_radio_group)
        {
            if(i == R.id.option_deploy_full)
            {
                deployFull = true;
            }else{
                deployFull = false;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.action_deploy_contract:
                deploy();
                break;
            case R.id.action_cancel_deploy:
                Intent intent = new Intent(getActivity(), ContractOverviewActivity.class);
                startActivity(intent);
                break;
            case R.id.action_add_image:
                getActivity().openContextMenu(addImageButton);
                break;
        }
    }

    /**
     * Creates the context menu for a {@link ProportionalImageView}
     *
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        if(v.getId() == R.id.action_add_image)
        {
            //show image selection dialog
            menu.setHeaderTitle("Select an Image");
            menu.add(0, v.getId(), 0, "from file");
            menu.add(0, v.getId(), 0, "from camera");
        }else if (v instanceof ProportionalImageView)
        {
            //show dialog to delete an image
            menu.setHeaderTitle("Delete image?");
            menu.add(0, v.getId(), 0, "delete");
        }
    }

    /**
     * Contains the interaction logic for the image context menu.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        PermissionProvider permissionProvider = appContext.getPermissionProvider();

        if(item.getTitle().equals("from file")){

            if(!permissionProvider.hasPermission(PermissionProvider.READ_STORAGE))
            {
                permissionProvider.requestPermission(PermissionProvider.READ_STORAGE);
                return false;
            }

            ImageHelper.openImageFile(this);
        }
        else if(item.getTitle().equals("from camera"))
        {
            if(!permissionProvider.hasPermission(PermissionProvider.CAMERA))
            {
                permissionProvider.requestPermission(PermissionProvider.CAMERA);
                return false;
            }

            ImageHelper.makePicture(this);
        }
        else if(item.getTitle().equals("delete"))
        {
            if(selectedImage == null)
                return true;

            removeImage(selectedImage);
        }

        return true;
    }

    private void removeImage(ProportionalImageView image)
    {
        unregisterForContextMenu(selectedImage);
        imageContainer.removeView(selectedImage);
        images.remove(selectedImage);
    }

    /**
     * Handles the result of Image capture and Pick file requests.
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        switch (requestCode)
        {
            case ImageHelper.PICK_FILE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bitmap bmp = getBitmap(intent.getData());
                    if(bmp != null)
                        addImage(bmp);
                }
                break;
            case ImageHelper.IMAGE_CAPTURE_REQUEST_CODE:
                if(resultCode == RESULT_OK) {
                    Bitmap bmp = getBitmap(intent.getData());
                    if(bmp != null)
                        addImage(bmp);
                }
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * Attempts to create a Bitmap image from the {@link Uri} obtained from the result of an Intent.
     *
     * @param uri
     * @return
     */
    private Bitmap getBitmap(Uri uri)
    {
        try{
            Bitmap bmp = ImageHelper.getCorrectlyOrientedImage(getActivity(), uri, 800);
            return bmp;
        }catch(Exception ex)
        {
            Log.e("deploy", "Could not save image", ex);
            appContext.getMessageService().showErrorMessage("Could not save image!" );
            return null;
        }
    }

    private void addImage(Bitmap bmp)
    {
        //create an ImageView from a bitmap
        final ProportionalImageView imageView = new ProportionalImageView(getActivity());
        imageView.setScale(ProportionalImageView.ScaleDimension.Height);
        int heightPx = (int)ImageHelper.convertDpToPixel(new Float(64.0), this.getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(heightPx, heightPx);
        layoutParams.setMargins(8,8,8,8);
        imageView.setLayoutParams(layoutParams);
        imageView.setImageBitmap(bmp);

        //add image to the container
        imageContainer.addView(imageView);
        images.put(imageView, bmp);

        registerForContextMenu(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageDialog(imageView);
            }
        });

        //register context menu on image
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
        ArrayList<Bitmap> bitmaps = new ArrayList<>(images.values());
        int startIndex = bitmaps.indexOf(images.get(imageView));

        //open DialogFragment
        DialogFragment imageDialog = new ImageDialogFragment();
        Bundle imageArgs = new Bundle();
        imageArgs.putSerializable(ImageDialogFragment.MESSAGE_IMAGE_BMPS, new ArrayList<>(images.values()));
        imageArgs.putInt(ImageDialogFragment.MESSAGE_IMAGE_INDEX, startIndex);
        imageDialog.setArguments(imageArgs);
        imageDialog.show(getFragmentManager(), "ImageDialog");
    }
}
