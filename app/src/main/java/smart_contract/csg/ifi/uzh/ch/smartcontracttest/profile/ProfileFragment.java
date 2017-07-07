package smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile;


import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import net.glxn.qrgen.android.QRCode;

import java.io.File;

import ch.uzh.ifi.csg.contract.common.ImageHelper;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ezvcard.VCard;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.StructuredName;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.dialog.ImageDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.MessageHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.validation.RequiredTextFieldValidator;

import static android.app.Activity.RESULT_OK;

/**
 * A fragment for retrieving and displaying user information
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

    private UserProfile profile;

    private LinearLayout layout;
    private EditText firstNameField;
    private EditText lastNameField;
    private EditText streetField;
    private EditText cityField;
    private EditText zipField;
    private EditText countryField;
    private EditText regionField;
    private EditText emailField;
    private EditText phoneField;
    private Button editButton;
    private Button saveButton;
    private ImageView qrImageView;
    private ImageView profileImage;

    private ProfileMode mode;
    private MessageHandler messageHandler;
    private ApplicationContextProvider contextProvider;

    public ProfileFragment() {
        // Required empty public constructor
        this.profile = new UserProfile();
        mode = ProfileMode.Edit;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view =  inflater.inflate(R.layout.fragment_profile_vcard, container, false);

        firstNameField = (EditText) view.findViewById(R.id.field_profile_first_name);
        firstNameField.addTextChangedListener(new RequiredTextFieldValidator(firstNameField));
        lastNameField = (EditText) view.findViewById(R.id.field_profile_last_name);
        lastNameField.addTextChangedListener(new RequiredTextFieldValidator(lastNameField));
        streetField = (EditText) view.findViewById(R.id.field_profile_street);
        streetField.addTextChangedListener(new RequiredTextFieldValidator(streetField));
        cityField = (EditText) view.findViewById(R.id.field_profile_city);
        cityField.addTextChangedListener(new RequiredTextFieldValidator(cityField));
        zipField = (EditText) view.findViewById(R.id.field_profile_zip);
        zipField.addTextChangedListener(new RequiredTextFieldValidator(zipField));
        countryField = (EditText) view.findViewById(R.id.field_profile_country);
        countryField.addTextChangedListener(new RequiredTextFieldValidator(countryField));
        regionField = (EditText) view.findViewById(R.id.field_profile_region);
        regionField.addTextChangedListener(new RequiredTextFieldValidator(regionField));
        emailField = (EditText) view.findViewById(R.id.field_profile_email);
        emailField.addTextChangedListener(new RequiredTextFieldValidator(emailField));
        phoneField = (EditText) view.findViewById(R.id.field_profile_phone);
        phoneField.addTextChangedListener(new RequiredTextFieldValidator(phoneField));

        editButton = (Button) view.findViewById(R.id.action_edit_identity);
        saveButton = (Button) view.findViewById(R.id.action_save_identity);
        qrImageView = (ImageView) view.findViewById(R.id.profile_qr_image);
        profileImage = (ImageView) view.findViewById(R.id.profile_image);

        editButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        qrImageView.setOnClickListener(this);

        registerForContextMenu(profileImage);
        profileImage.setOnClickListener(this);
        profileImage.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                getActivity().openContextMenu(profileImage);
                return true;
            }
        });

        layout = (LinearLayout) view.findViewById(R.id.layout_profile_fragment);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        attachContext(context);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attachContext(activity);
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
            throw new RuntimeException("Context must implement MessageHandler!");
        }
    }

    private UserProfile getProfileInformation()
    {
        VCard card = new VCard();

        StructuredName name = new StructuredName();
        name.setFamily(lastNameField.getText().toString());
        name.setGiven(firstNameField.getText().toString());

        Address address = new Address();
        address.setStreetAddress(streetField.getText().toString());
        address.setLocality(cityField.getText().toString());
        address.setPostalCode(zipField.getText().toString());
        address.setCountry(countryField.getText().toString());
        address.setRegion(regionField.getText().toString());

        card.addAddress(address);
        card.setStructuredName(name);
        card.addEmail(emailField.getText().toString());
        card.addTelephoneNumber(phoneField.getText().toString(), TelephoneType.HOME);

        loadQrImage(card);
        profile.setVCard(card);

        return profile;
    }

    public void setProfileInformation(UserProfile userProfile)
    {
        profile.setVCard(userProfile.getVCard());
        profile.setProfileImagePath(userProfile.getProfileImagePath());

        VCard card = profile.getVCard();
        StructuredName name = card.getStructuredName();
        firstNameField.setText(name.getGiven());
        lastNameField.setText(name.getFamily());

        if(card.getAddresses().size() > 0)
        {
            Address address = card.getAddresses().get(0);
            streetField.setText(address.getStreetAddress());
            countryField.setText(address.getCountry());
            zipField.setText(address.getPostalCode());
            cityField.setText(address.getLocality());
            regionField.setText(address.getRegion());
        }

        if(card.getEmails().size() > 0)
            emailField.setText(card.getEmails().get(0).getValue());

        if(card.getTelephoneNumbers().size() > 0)
            phoneField.setText(card.getTelephoneNumbers().get(0).getText());

        loadQrImage(card);

        if(profile.getProfileImagePath() != null)
            profileImage.setImageURI(Uri.fromFile(new File(profile.getProfileImagePath())));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setMode(ProfileMode mode)
    {
        this.mode = mode;

        if(mode == ProfileMode.Edit){
            changeLayoutRecursive(layout, ProfileMode.Edit);
            saveButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);
        }else
        {
            changeLayoutRecursive(layout, ProfileMode.ReadOnly);
            saveButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
        }
    }

    private void changeLayoutRecursive(ViewGroup viewGroup, ProfileMode mode)
    {
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ViewGroup)
            {
                changeLayoutRecursive((ViewGroup) view, mode);
            }
            else if (view instanceof EditText) {
                view.setEnabled(mode == ProfileMode.Edit);
            }
        }
    }

    private void loadQrImage(VCard card)
    {
        Bitmap bitmap = QRCode.from(card.write()).bitmap();
        qrImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 125, 125, false));
    }

    private boolean validateProfile()
    {
        return firstNameField.getError() == null &&
                lastNameField.getError() == null &&
                streetField.getError() == null &&
                countryField.getError() == null &&
                zipField.getError() == null &&
                regionField.getError() == null &&
                phoneField.getError() == null &&
                emailField.getError() == null;
    }

    public void loadAccountProfileInformation()
    {
        String selectedAccount = contextProvider.getSettingProvider().getSelectedAccount();
        UserProfile userProfile = contextProvider.getServiceProvider().getAccountService().getAccountProfile(selectedAccount);
        if(userProfile.getVCard() != null)
            setProfileInformation(userProfile);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select and Image");
        menu.add(0, v.getId(), 0, "from file");
        menu.add(0, v.getId(), 0, "from camera");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        if(item.getTitle().equals("from file")){
            ImageHelper.openFile(this);
        }
        else if(item.getTitle().equals("from camera")){
            ImageHelper.makePicture(this);
        }else{
            return false;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //get the new value from Intent data
        switch(requestCode) {
            case ImageHelper.PICK_IMAGE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri uri = data.getData();
                        Bitmap bmp = ImageHelper.getCorrectlyOrientedImage(getActivity(), uri, 1280);
                        File imgFile = ImageHelper.saveBitmap(bmp, contextProvider.getSettingProvider().getProfileImageDirectory());
                        profile.setProfileImagePath(imgFile.getAbsolutePath());
                        String selectedAccount = contextProvider.getSettingProvider().getSelectedAccount();
                        contextProvider.getServiceProvider().getAccountService().saveAccountProfile(selectedAccount, profile);
                        profileImage.setImageURI(Uri.fromFile(imgFile));
                    }
                    catch (Exception e) {
                        messageHandler.showSnackBarMessage(e.getMessage(), Snackbar.LENGTH_LONG);
                    }
                }
                break;
            case ImageHelper.IMAGE_CAPTURE_REQUEST_CODE:
                if(resultCode == RESULT_OK) {
                    Bitmap bitmap;
                    try
                    {
                        Bitmap bmp = ImageHelper.getCorrectlyOrientedImage(getActivity(), data.getData(), 1280);
                        File imgFile = ImageHelper.saveBitmap(bmp, contextProvider.getSettingProvider().getProfileImageDirectory());
                        profile.setProfileImagePath(imgFile.getAbsolutePath());
                        String selectedAccount = contextProvider.getSettingProvider().getSelectedAccount();
                        contextProvider.getServiceProvider().getAccountService().saveAccountProfile(selectedAccount, profile);
                        profileImage.setImageURI(Uri.fromFile(imgFile));
                    }
                    catch (Exception e)
                    {
                        messageHandler.showSnackBarMessage(e.getMessage(), Snackbar.LENGTH_LONG);
                    }
                }
        }
    }

    @Override
    public void onClick(View view) {

        switch(view.getId())
        {
            case R.id.action_save_identity:
                if(validateProfile())
                {
                    this.profile = getProfileInformation();
                    String selectedAccount = contextProvider.getSettingProvider().getSelectedAccount();
                    contextProvider.getServiceProvider().getAccountService().saveAccountProfile(selectedAccount, profile);
                    messageHandler.showErrorMessage("Profile saved!");
                }else{
                    messageHandler.showErrorMessage("Please fill out all required fields!");
                }

                break;
            case R.id.profile_qr_image:
                DialogFragment imageDialog = new ImageDialogFragment();
                Bundle args = new Bundle();
                args.putString(ImageDialogFragment.MESSAGE_IMAGE_SOURCE, profile.getVCard().write());
                args.putBoolean(ImageDialogFragment.MESSAGE_DISPLAY_QRCODE, true);
                imageDialog.setArguments(args);
                imageDialog.show(getFragmentManager(), "QrImageDialog");
                break;
            case R.id.profile_image:
                if(profile.getProfileImagePath() == null)
                    return;

                DialogFragment profileImageDialog = new ImageDialogFragment();
                Bundle imageArgs = new Bundle();
                imageArgs.putString(ImageDialogFragment.MESSAGE_IMAGE_SOURCE, profile.getProfileImagePath());
                imageArgs.putBoolean(ImageDialogFragment.MESSAGE_DISPLAY_QRCODE, false);
                profileImageDialog.setArguments(imageArgs);
                profileImageDialog.show(getFragmentManager(), "ProfileImageDialog");
                break;

        }
    }

    public static enum ProfileMode
    {
        Edit,
        ReadOnly,
    }
}
