package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiResponse;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiSellerCallback;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service.ContractInfoListener;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service.UserProfileListener;

/**
 * Created by flo on 24.06.17.
 */

public class ContractExportDialog extends DialogFragment implements WifiSellerCallback
{
    public static String MESSAGE_IDENTIFICATION_USED = "ch.uzh.ifi.csg.smart_contract.exchange.identification";
    public static String MESSAGE_CONTRACT_DATA = "ch.uzh.ifi.csg.smart_contract.exchange.contract_data";

    private ContractExportListener exportListener;
    private ApplicationContextProvider contextProvider;

    private UserProfile userProfile;
    private ContractInfo contractInfo;
    private boolean useIdentification;

    private LinearLayout exportDialogContent;
    private TextView exportDialogInfo;
    private LinearLayout verifyProfileView;
    private CheckBox profileImageCheckbox;
    private AlertDialog dialog;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        useIdentification = args.getBoolean(MESSAGE_IDENTIFICATION_USED);
        contractInfo = (ContractInfo) args.getSerializable(MESSAGE_CONTRACT_DATA);
        System.out.print("aewsrfdgfds");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.fragment_p2p_exchange_dialog, null);
        contentView.setBackgroundColor(Color.TRANSPARENT);
        verifyProfileView = (LinearLayout) contentView.findViewById(R.id.select_profile_details_view);
        exportDialogContent = (LinearLayout) contentView.findViewById(R.id.dialog_content);
        exportDialogInfo = (TextView) contentView.findViewById(R.id.dialog_info);
        profileImageCheckbox = (CheckBox) contentView.findViewById(R.id.checkbox_profile_image);

        builder.setTitle("Export Dialog");
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                contextProvider.getP2PSellerService().disconnect();
                if(exportListener != null)
                    exportListener.onContractDialogCanceled();
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
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(false);

                contextProvider.getP2PSellerService().connect(ContractExportDialog.this, useIdentification);
                BusyIndicator.show(exportDialogContent);
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
                BusyIndicator.hide(exportDialogContent);
                exportDialogInfo.setText("User profile requested by other peer");

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

                        UserProfile profile = new UserProfile();
                        profile.setVCard(localProfile.getVCard());

                        if(profileImageCheckbox.isChecked())
                        {
                            //todo:add image path to profile
                        }

                        listener.onUserProfileReceived(profile);

                        BusyIndicator.show(exportDialogContent);
                    }
                });
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

        if(context instanceof ContractExportListener)
        {
            exportListener = (ContractExportListener)context;
        }else{
            throw new RuntimeException(context.toString() + " must implement ContractExportListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attachContext(activity);
    }

    @Override
    public void onContractInfoRequested(final ContractInfoListener listener)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                exportDialogInfo.setText("Contract info requested by other peer");
                listener.onContractInfoReceived(contractInfo);

                //After transmission of contract data, we enable the ''OK' button and inform the listener
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(exportListener != null)
                            exportListener.onContractDataExchanged(userProfile);
                    }
                });

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                BusyIndicator.hide(exportDialogContent);
            }
        });
    }

    @Override
    public void onUserProfileReceived(final UserProfile data) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                exportDialogInfo.setText("User profile received");
                userProfile = data;
            }
        });
    }

    @Override
    public void onWifiResponse(final WifiResponse response) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                exportDialogInfo.setText(response.getReasonPhrase());
            }
        });
    }

    public static interface ContractExportListener
    {
        void onContractDataExchanged(UserProfile buyerProfile);
        void onContractDialogCanceled();
    }

}
