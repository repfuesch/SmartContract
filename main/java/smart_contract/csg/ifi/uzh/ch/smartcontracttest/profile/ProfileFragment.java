package smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile;


import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import net.glxn.qrgen.android.QRCode;

import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ezvcard.VCard;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.StructuredName;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.dialog.ImageDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.MessageHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.EthServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.EthSettingProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.validation.RequiredTextFieldValidator;

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
    private Button verifyButton;
    private Button editButton;
    private Button saveButton;
    private ImageView qrImageView;

    private ProfileMode mode;
    private OnProfileVerifiedListener verifiedListener;
    private MessageHandler messageHandler;
    private ApplicationContextProvider contextProvider;

    public ProfileFragment() {
        // Required empty public constructor
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

        verifyButton = (Button) view.findViewById(R.id.action_verify_identity);
        editButton = (Button) view.findViewById(R.id.action_edit_identity);
        saveButton = (Button) view.findViewById(R.id.action_save_identity);
        qrImageView = (ImageView) view.findViewById(R.id.profile_qr_image);
        verifyButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        qrImageView.setOnClickListener(this);

        layout = (LinearLayout) view.findViewById(R.id.layout_profile_fragment);

        this.profile = new UserProfile();
        mode = ProfileMode.Edit;

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof OnProfileVerifiedListener)
        {
            verifiedListener = (OnProfileVerifiedListener) context;
        }

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
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setMode(ProfileMode mode)
    {
        this.mode = mode;

        if(mode == ProfileMode.Verify)
        {
            changeLayoutRecursive(layout, ProfileMode.Verify);
            verifyButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.GONE);
            editButton.setVisibility(View.GONE);
        }else if(mode == ProfileMode.Edit){
            changeLayoutRecursive(layout, ProfileMode.Edit);
            verifyButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);
        }else
        {
            changeLayoutRecursive(layout, ProfileMode.ReadOnly);
            verifyButton.setVisibility(View.GONE);
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
                /*
                if(view instanceof TextInputLayout)
                {
                    TextInputLayout textInput = (TextInputLayout)view;
                    textInput.setHintTextAppearance(R.style.HintTextBig);
                }*/
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
    public void onClick(View view) {

        switch(view.getId())
        {
            case R.id.action_verify_identity:
                if(verifiedListener != null && validateProfile())
                {
                    profile.setVerified(true);
                    verifiedListener.onProfileVerified(profile);
                }
                break;
            case R.id.action_save_identity:
                if(validateProfile())
                {
                    this.profile = getProfileInformation();
                    String selectedAccount = contextProvider.getSettingProvider().getSelectedAccount();
                    contextProvider.getServiceProvider().getAccountService().saveAccountProfile(selectedAccount, profile);
                    messageHandler.showMessage("Profile saved!");
                }else{
                    messageHandler.showMessage("Please fill out all required fields!");
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

        }
    }

    public static interface OnProfileVerifiedListener
    {
        void onProfileVerified(UserProfile profile);
    }

    public static enum ProfileMode
    {
        Edit,
        Verify,
        ReadOnly,
    }
}
