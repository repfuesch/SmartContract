package ch.uzh.ifi.csg.smartcontract.app.profile;


import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

import org.jdeferred.Promise;

import java.io.File;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.smartcontract.app.common.controls.BusyIndicator;
import ch.uzh.ifi.csg.smartcontract.library.async.Async;
import ch.uzh.ifi.csg.smartcontract.library.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.smartcontract.library.async.promise.DoneCallback;
import ch.uzh.ifi.csg.smartcontract.library.async.promise.FailCallback;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.UserProfile;
import ch.uzh.ifi.csg.smartcontract.library.service.serialization.GsonSerializationService;
import ch.uzh.ifi.csg.smartcontract.library.util.ImageHelper;
import ezvcard.VCard;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.StructuredName;
import ch.uzh.ifi.csg.smartcontract.app.R;
import ch.uzh.ifi.csg.smartcontract.app.common.dialog.ImageDialogFragment;
import ch.uzh.ifi.csg.smartcontract.app.common.permission.PermissionProvider;
import ch.uzh.ifi.csg.smartcontract.app.common.ApplicationContext;
import ch.uzh.ifi.csg.smartcontract.app.common.ApplicationContextProvider;
import ch.uzh.ifi.csg.smartcontract.app.common.validation.RequiredTextFieldValidator;

import static android.app.Activity.RESULT_OK;

/**
 * A {@link Fragment} that displays the details of a {@link UserProfile}.
 * The {@link #mode} determines whether the Profile can be modified or if the fields are read-only.
 *
 * The parent Activity must implement the {@link ProfileDataChangedListener} interface to get
 * notified when the profile data has changed.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener, TextWatcher {

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
    private Button saveButton;
    private ImageView qrImageView;
    private ImageView profileImage;
    private LinearLayout profileImageContainer;

    private ProfileMode mode;
    private ApplicationContext appContext;
    private ProfileDataChangedListener dataChangedListener;

    public ProfileFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.profile = new UserProfile();
        mode = ProfileMode.Edit;

        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);

        firstNameField = (EditText) view.findViewById(R.id.field_profile_first_name);
        firstNameField.addTextChangedListener(new RequiredTextFieldValidator(firstNameField));
        firstNameField.addTextChangedListener(this);
        lastNameField = (EditText) view.findViewById(R.id.field_profile_last_name);
        lastNameField.addTextChangedListener(new RequiredTextFieldValidator(lastNameField));
        lastNameField.addTextChangedListener(this);
        streetField = (EditText) view.findViewById(R.id.field_profile_street);
        streetField.addTextChangedListener(new RequiredTextFieldValidator(streetField));
        streetField.addTextChangedListener(this);
        cityField = (EditText) view.findViewById(R.id.field_profile_city);
        cityField.addTextChangedListener(new RequiredTextFieldValidator(cityField));
        cityField.addTextChangedListener(this);
        zipField = (EditText) view.findViewById(R.id.field_profile_zip);
        zipField.addTextChangedListener(new RequiredTextFieldValidator(zipField));
        zipField.addTextChangedListener(this);
        countryField = (EditText) view.findViewById(R.id.field_profile_country);
        countryField.addTextChangedListener(new RequiredTextFieldValidator(countryField));
        countryField.addTextChangedListener(this);
        regionField = (EditText) view.findViewById(R.id.field_profile_region);
        regionField.addTextChangedListener(new RequiredTextFieldValidator(regionField));
        regionField.addTextChangedListener(this);
        emailField = (EditText) view.findViewById(R.id.field_profile_email);
        emailField.addTextChangedListener(new RequiredTextFieldValidator(emailField));
        emailField.addTextChangedListener(this);
        phoneField = (EditText) view.findViewById(R.id.field_profile_phone);
        phoneField.addTextChangedListener(new RequiredTextFieldValidator(phoneField));
        phoneField.addTextChangedListener(this);

        saveButton = (Button) view.findViewById(R.id.action_save_profile);
        qrImageView = (ImageView) view.findViewById(R.id.profile_qr_image);
        profileImage = (ImageView) view.findViewById(R.id.profile_image);
        profileImageContainer = (LinearLayout) view.findViewById(R.id.profile_image_container);

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        attachContext(getActivity());
        if(profile != null)
            setProfileInformation(profile);
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
        if(context instanceof ApplicationContextProvider)
        {
            appContext = ((ApplicationContextProvider) context).getAppContext();
        }else{
            throw new RuntimeException("Context must implement ApplicationContextProvider!");
        }

        if(context instanceof  ProfileDataChangedListener)
        {
            dataChangedListener = (ProfileDataChangedListener)context;
        }else{
            throw new RuntimeException("Context must implement ProfileDataChangedListener!");
        }
    }

    /**
     * Returns a {@link UserProfile} that contains the information the user inserted in the text
     * fields
     *
     * @return the modified profile
     */
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

        profile.setVCard(card);

        loadQrImage(profile);

        return profile;
    }

    /**
     * Initializes the text fields and the image view from the user profile. Creates an image that
     * contains the QR-code to scan this profile.
     *
     * @param userProfile
     */
    public void setProfileInformation(UserProfile userProfile)
    {
        if(profile.getProfileImagePath() != null) {
            profileImage.setImageURI(Uri.fromFile(new File(profile.getProfileImagePath())));
        }else{
            profile.setProfileImagePath(userProfile.getProfileImagePath());
            if(profile.getProfileImagePath() != null)
                profileImage.setImageURI(Uri.fromFile(new File(profile.getProfileImagePath())));
        }

        if(profile.getVCard() == null)
            profile.setVCard(userProfile.getVCard());

        if(profile.getVCard() == null)
            return;

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

        loadQrImage(userProfile);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Changes the layout of the View depending on the {@link ProfileMode}
     *
     * @param mode
     */
    public void setMode(ProfileMode mode)
    {
        this.mode = mode;

        if(mode == ProfileMode.Edit){
            changeLayoutRecursive(layout, ProfileMode.Edit);
            saveButton.setVisibility(View.VISIBLE);
        }else
        {
            changeLayoutRecursive(layout, ProfileMode.ReadOnly);
            saveButton.setVisibility(View.GONE);
        }
    }

    public ProfileMode getMode()
    {
        return mode;
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

    private void loadQrImage(UserProfile profile)
    {
        String json = new GsonSerializationService().serialize(profile);
        Bitmap bitmap = QRCode.from(json).withSize(250, 250).bitmap();
        qrImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        //create context menu to change the profile image
        menu.setHeaderTitle("Select and Image");
        menu.add(0, v.getId(), 0, "from file");
        menu.add(0, v.getId(), 0, "from camera");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){

        PermissionProvider permissionProvider = appContext.getPermissionProvider();

        if(item.getTitle().equals("from file")){

            if(!permissionProvider.hasPermission(PermissionProvider.READ_STORAGE))
            {
                permissionProvider.requestPermission(PermissionProvider.READ_STORAGE);
                return true;
            }

            //select an image from the device
            ImageHelper.openImageFile(this);
        }
        else if(item.getTitle().equals("from camera"))
        {
            if(!permissionProvider.hasPermission(PermissionProvider.CAMERA))
            {
                permissionProvider.requestPermission(PermissionProvider.CAMERA);
                return true;
            }

            //make a new image with the camera of the device
            ImageHelper.makePicture(this);
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ImageHelper.PICK_FILE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    replaceImage(data.getData());
                }
                break;
            case ImageHelper.IMAGE_CAPTURE_REQUEST_CODE:
                if(resultCode == RESULT_OK) {
                    replaceImage(data.getData());
                }
        }
    }

    private void replaceImage(final Uri uri)
    {
        //delete old image
        if(profile.getProfileImagePath() != null)
        {
            new File(profile.getProfileImagePath()).delete();
            profile.setProfileImagePath(null);
        }

        BusyIndicator.show(profileImageContainer);

        //scale and safe image in a background thread
        Async.toPromise(new Callable<File>() {
            @Override
            public File call() throws Exception {

                //obtain the correctly oriented bitmap from the Uri
                Bitmap bmp = ImageHelper.getCorrectlyOrientedImage(getActivity(), uri, 800);
                //save profile image on the file system
                File imgFile = ImageHelper.saveBitmap(bmp, appContext.getSettingProvider().getImageDirectory());

                return imgFile;
            }
        }).done(new DoneCallback<File>() {
            @Override
            public void onDone(final File result) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //update profile and image
                        profile.setProfileImagePath(result.getAbsolutePath());
                        profileImage.setImageURI(Uri.fromFile(result));

                        //notify the parent activity that the profile data changed
                        if(verifyFields())
                            dataChangedListener.onProfileDataChanged(getProfileInformation());
                    }
                });
            }
        }).fail(new FailCallback() {
            @Override
            public void onFail(Throwable result) {
                appContext.getMessageService().showErrorMessage("Could not process the provided image.");
                Log.e("profile", "Could not process the provided image.", result);
            }
        }).always(new AlwaysCallback<File>() {
            @Override
            public void onAlways(Promise.State state, File resolved, Throwable rejected) {
                BusyIndicator.hide(profileImageContainer);
            }
        });
    }

    @Override
    public void onClick(View view) {

        switch(view.getId())
        {
            case R.id.action_save_profile:
                //notify the parent activity that profile data has changed
                if(!verifyFields())
                {
                    appContext.getMessageService().showSnackBarMessage("please fill out all required fields", Snackbar.LENGTH_LONG);
                    return;
                }
                this.profile = getProfileInformation();
                dataChangedListener.onProfileDataChanged(profile);
                appContext.getMessageService().showSnackBarMessage("Profile saved", Snackbar.LENGTH_LONG);
                break;
            case R.id.profile_qr_image:
                //Show QR-Code in an ImageDialogFragment
                DialogFragment imageDialog = new ImageDialogFragment();
                Bundle args = new Bundle();
                args.putString(ImageDialogFragment.MESSAGE_IMAGE_SOURCE, new GsonSerializationService().serialize(profile));
                args.putBoolean(ImageDialogFragment.MESSAGE_DISPLAY_QRCODE, true);
                imageDialog.setArguments(args);
                imageDialog.show(getFragmentManager(), "QrImageDialog");
                break;
            case R.id.profile_image:
                if(profile.getProfileImagePath() == null)
                    return;

                //Show profile image in an ImageDialogFragment
                DialogFragment profileImageDialog = new ImageDialogFragment();
                Bundle imageArgs = new Bundle();
                imageArgs.putString(ImageDialogFragment.MESSAGE_IMAGE_SOURCE, profile.getProfileImagePath());
                imageArgs.putBoolean(ImageDialogFragment.MESSAGE_DISPLAY_QRCODE, false);
                profileImageDialog.setArguments(imageArgs);
                profileImageDialog.show(getFragmentManager(), "ProfileImageDialog");
                break;

        }
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
            saveButton.setEnabled(true);
        }else{
            saveButton.setEnabled(false);
        }
    }

    /**
     * Verifies that all mandatory fields are set
     *
     * @return
     */
    private boolean verifyFields()
    {
        return firstNameField.getError() == null &&
                lastNameField.getError() == null &&
                streetField.getError() == null &&
                countryField.getError() == null &&
                cityField.getError() == null &&
                zipField.getError() == null &&
                regionField.getError() == null &&
                phoneField.getError() == null &&
                emailField.getError() == null;
    }

    public enum ProfileMode
    {
        Edit,
        ReadOnly,
    }

    /**
     * Callback interface that must be implemented by the parent Activity that creates this Fragment.
     * Notifies the implementing Activity about changes in the {@link UserProfile}
     */
    public interface ProfileDataChangedListener
    {
        void onProfileDataChanged(UserProfile profile);
    }
}
