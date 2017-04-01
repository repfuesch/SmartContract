package smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.scheme.VCard;

import ch.uzh.ifi.csg.contract.service.account.AccountProfile;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

/**
 * A fragment for retrieving and displaying user information
 */
public class ProfileVCardFragment extends Fragment {

    private AccountProfile profile;

    private EditText nameField;
    private EditText addressField;
    private EditText emailField;
    private EditText phoneField;
    private EditText companyField;
    private EditText titleField;
    private EditText websiteField;

    private ImageView qrImageView;

    public ProfileVCardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_profile_vcard, container, false);
        nameField = (EditText) view.findViewById(R.id.field_profile_name);
        addressField = (EditText) view.findViewById(R.id.field_profile_address);
        emailField = (EditText) view.findViewById(R.id.field_profile_email);
        phoneField = (EditText) view.findViewById(R.id.field_profile_phone);
        companyField = (EditText) view.findViewById(R.id.field_profile_company);
        titleField = (EditText) view.findViewById(R.id.field_profile_title);
        websiteField = (EditText) view.findViewById(R.id.field_profile_website);
        qrImageView = (ImageView) view.findViewById(R.id.profile_qr_image);

        return view;
    }

    public AccountProfile getProfileInformation()
    {
        VCard card = new VCard()
                .setName(nameField.getText().toString())
                .setAddress(addressField.getText().toString())
                .setCompany(companyField.getText().toString())
                .setEmail(emailField.getText().toString())
                .setPhoneNumber(phoneField.getText().toString())
                .setTitle(titleField.getText().toString())
                .setWebsite(websiteField.getText().toString());

        loadQrImage(card);
        profile.setVCard(card);

        return profile;
    }

    public void setProfileInformation(AccountProfile profile)
    {
        this.profile = profile;

        VCard card = profile.getVCard();
        nameField.setText(card.getName());
        addressField.setText(card.getAddress());
        companyField.setText(card.getCompany());
        emailField.setText(card.getEmail());
        phoneField.setText(card.getPhoneNumber());
        titleField.setText(card.getTitle());
        websiteField.setText(card.getWebsite());

        loadQrImage(card);
    }

    private void loadQrImage(VCard card)
    {
        Bitmap bitmap = QRCode.from(card.toString()).bitmap();
        qrImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 125, 125, false));
    }

    public boolean validateProfile()
    {
        //todo:check mandatory fields here
        return true;
    }
}
