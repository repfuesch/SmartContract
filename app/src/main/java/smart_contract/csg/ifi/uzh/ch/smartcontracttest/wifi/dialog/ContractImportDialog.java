package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.common.ImageHelper;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiBuyerCallback;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiResponse;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service.UserProfileListener;

/**
 * Created by flo on 24.06.17.
 */

public class ContractImportDialog extends DialogFragment implements WifiBuyerCallback
{
    private ContractImportListener importListener;
    private ApplicationContextProvider contextProvider;

    private ContractInfo contractInfo;
    private UserProfile userProfile;

    private LinearLayout importDialogContent;
    private TextView importDialogInfo;
    private LinearLayout verifyProfileView;
    private CheckBox profileImageCheckbox;
    private AlertDialog dialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.fragment_p2p_import_dialog, null);
        contentView.setBackgroundColor(Color.TRANSPARENT);
        verifyProfileView = (LinearLayout) contentView.findViewById(R.id.select_profile_details_view);
        importDialogContent = (LinearLayout) contentView.findViewById(R.id.dialog_content);
        importDialogInfo = (TextView) contentView.findViewById(R.id.dialog_info);
        profileImageCheckbox = (CheckBox) contentView.findViewById(R.id.checkbox_profile_image);

        builder.setTitle("Import Dialog");
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                contextProvider.getP2PBuyerService().disconnect();
                if(importListener != null)
                    importListener.onContractDialogCanceled();
            }
        });

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(contentView);

        // Create the AlertDialog object and return it
        dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                setCancelable(false);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(false);

                contextProvider.getP2PBuyerService().requestConnection(ContractImportDialog.this);
                importDialogInfo.setText("Waiting for connection request");
                BusyIndicator.show(importDialogContent);
            }
        });

        return dialog;
    }

    @Override
    public void onUserProfileRequested(final UserProfileListener listener)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BusyIndicator.hide(importDialogContent);
                importDialogInfo.setText("User profile requested by other peer");

                verifyProfileView.setVisibility(View.VISIBLE);
                final Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

                okButton.setEnabled(true);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        okButton.setEnabled(false);
                        okButton.setOnClickListener(null);

                        //construct new UserProfile based on selection of user
                        String selectedAccount = contextProvider.getSettingProvider().getSelectedAccount();
                        UserProfile localProfile = contextProvider.getServiceProvider().getAccountService().getAccountProfile(selectedAccount);

                        final UserProfile profile = new UserProfile();
                        profile.setVCard(localProfile.getVCard());

                        if(profileImageCheckbox.isChecked())
                        {
                            profile.setProfileImagePath(localProfile.getProfileImagePath());
                        }

                        listener.onUserProfileReceived(profile);

                        BusyIndicator.show(importDialogContent);
                    }
                });
            }
        });

    }

    @Override
    public void onUserProfileReceived(final UserProfile data) {
        userProfile = data;
        if(userProfile.getProfileImagePath() != null)
        {
            //copy the profile image into the correct path
            File newFile = ImageHelper.saveImageFile(userProfile.getProfileImagePath(), contextProvider.getSettingProvider().getProfileImageDirectory());
            userProfile.setProfileImagePath(newFile.getAbsolutePath());
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                importDialogInfo.setText("User profile received");
            }
        });
    }

    @Override
    public void onContractInfoReceived(final ContractInfo info) {

        for(String imgSig : info.getImages().keySet())
        {
            //copy the images into the correct application path
            File newFile = ImageHelper.saveImageFile(info.getImages().get(imgSig), contextProvider.getSettingProvider().getProfileImageDirectory());
            info.getImages().put(imgSig, newFile.getAbsolutePath());
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                importDialogInfo.setText("Contract info received");
                contractInfo = new ContractInfo(info.getContractType(), info.getContractAddress(), userProfile, info.getImages());

                if(importListener != null)
                    importListener.onContractDataReceived(contractInfo);

                dismiss();
            }
        });
    }

    @Override
    public void onWifiResponse(final WifiResponse response) {

        if(getActivity() == null)
            return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                importDialogInfo.setText(response.getReasonPhrase());
            }
        });
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
            contextProvider = (ApplicationContextProvider)context;
        }else{
            throw new RuntimeException(context.toString() + " must implement ApplicationContextProvider");
        }

        if(context instanceof ContractImportListener)
        {
            importListener = (ContractImportListener)context;
        }else{
            throw new RuntimeException(context.toString() + " must implement ContractImportListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attachContext(activity);
    }

    public static interface ContractImportListener{
        void onContractDataReceived(ContractInfo contract);
        void onContractDialogCanceled();
    }
}
