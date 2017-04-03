package smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import net.glxn.qrgen.android.QRCode;

import ch.uzh.ifi.csg.contract.service.account.UserProfile;
import ezvcard.VCard;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.StructuredName;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.validation.RequiredTextFieldValidator;

/**
 * A fragment for retrieving and displaying user information
 */
public class ProfileFragment extends Fragment {

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

    private ImageView qrImageView;
    private boolean verificationEnabled;
    private OnProfileVerifiedListener listener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateProfile())
                {
                    verifyButton.setVisibility(View.GONE);
                    listener.onProfileVerified(getProfileInformation());
                }
            }
        });

        qrImageView = (ImageView) view.findViewById(R.id.profile_qr_image);
        layout = (LinearLayout) view.findViewById(R.id.layout_profile_fragment);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof OnProfileVerifiedListener)
        {
            listener = (OnProfileVerifiedListener) context;
        }else{
            throw new IllegalArgumentException("Context must implement interface OnProfileVerifiedListener!");
        }
    }

    public UserProfile getProfileInformation()
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

    public void setProfileInformation(UserProfile profile)
    {
        this.profile = profile;

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

        if(verificationEnabled)
        {
            if(validateProfile())
                verifyButton.setEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void setReadOnly()
    {
        changeLayoutRecursive(layout);
    }

    public void enableVerification()
    {
        verificationEnabled = true;
        verifyButton.setVisibility(View.VISIBLE);
    }

    private void changeLayoutRecursive(ViewGroup viewGroup) {

        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ViewGroup)
            {
                if(view instanceof TextInputLayout)
                {
                    TextInputLayout textInput = (TextInputLayout)view;
                    textInput.setHintTextAppearance(R.style.HintTextBig);
                }
                changeLayoutRecursive((ViewGroup) view);
            }
            else if (view instanceof EditText) {
                view.setEnabled(false);
            }
        }

    }

    private void loadQrImage(VCard card)
    {
        Bitmap bitmap = QRCode.from(card.toString()).bitmap();
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

    public static interface OnProfileVerifiedListener
    {
        void onProfileVerified(UserProfile profile);
    }
}
