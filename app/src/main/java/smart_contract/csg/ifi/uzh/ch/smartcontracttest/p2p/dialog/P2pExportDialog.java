package smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.util.ImageHelper;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;
import ch.uzh.ifi.csg.contract.p2p.peer.P2pSellerCallback;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.ContractInfoListener;

/**
 * Created by flo on 24.06.17.
 */

public class P2pExportDialog extends P2pDialog implements P2pSellerCallback
{
    public static String MESSAGE_IDENTIFICATION_USED = "ch.uzh.ifi.csg.smart_contract.exchange.identification";
    public static String MESSAGE_CONTRACT_DATA = "ch.uzh.ifi.csg.smart_contract.exchange.contract_data";

    private P2pExportListener exportListener;

    private ContractInfo contractInfo;
    private boolean useIdentification;

    private List<String> deviceList;
    private Spinner deviceListSpinner;
    private String selectedDevice;

    public P2pExportDialog()
    {
        super("Export Contract");

        deviceList = new ArrayList<>();
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        useIdentification = args.getBoolean(MESSAGE_IDENTIFICATION_USED);
        contractInfo = (ContractInfo) args.getSerializable(MESSAGE_CONTRACT_DATA);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_p2p_export_dialog;
    }

    @Override
    protected void onDialogCanceled()
    {
        contextProvider.getP2PSellerService().disconnect();
        if(exportListener != null)
            exportListener.onContractDialogCanceled();
    }

    @Override
    protected void onShowDialog()
    {
        super.onShowDialog();
        deviceListSpinner = (Spinner) dialog.findViewById(R.id.connection_list_spinner);

        contextProvider.getP2PSellerService().requestConnection(P2pExportDialog.this);
        dialogInfo.setText("Waiting for peer list");
        BusyIndicator.show(dialogContent);
    }

    @Override
    public void onUserProfileReceived(final UserProfile data) {
        userProfile = data;
        if(userProfile.getProfileImagePath() != null)
        {
            //copy the profile image into the correct path
            File newFile = ImageHelper.saveImageFile(userProfile.getProfileImagePath(), contextProvider.getSettingProvider().getImageDirectory());
            userProfile.setProfileImagePath(newFile.getAbsolutePath());
        }
    }

    private boolean updateDeviceList(List<String> devices)
    {
        List<String> devList = new ArrayList<>();
        boolean changed = false;
        if(deviceList.size() != devices.size())
            changed = true;

        for(String deviceName : devices)
        {
            boolean newDevice = true;
            for(String dev : deviceList)
            {
                if(deviceName.equals(dev))
                    newDevice = false;
            }

            devList.add(deviceName);
            if(newDevice)
                changed = true;
        }

        deviceList = devList;
        if(!changed)
            return false;

        deviceListSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, deviceList));
        return true;
    }

    protected void attachContext(Context context)
    {
        super.attachContext(context);

        if(context instanceof P2pExportListener)
        {
            exportListener = (P2pExportListener)context;
        }else{
            throw new RuntimeException(context.toString() + " must implement P2pExportListener");
        }
    }

    @Override
    public void onContractInfoRequested(final ContractInfoListener listener)
    {
        //make sure that no profile info is set
        contractInfo.getUserProfile().setVCard(null);
        contractInfo.getUserProfile().setProfileImagePath(null);

        if(useIdentification)
        {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BusyIndicator.hide(dialogContent);

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
                            contractInfo.getUserProfile().setVCard(localProfile.getVCard());

                            if(profileImageCheckbox.isChecked())
                            {
                                contractInfo.getUserProfile().setProfileImagePath(localProfile.getProfileImagePath());
                            }

                            listener.onContractInfoReceived(contractInfo);

                            BusyIndicator.show(dialogContent);
                        }
                    });
                }
            });
        }else{
            listener.onContractInfoReceived(contractInfo);
        }
    }

    @Override
    public void onPeersChanged(final List<String> deviceNames) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(deviceNames.size() == 0)
                    return;

                if(!updateDeviceList(deviceNames))
                    return;

                selectedDevice = deviceNames.get(0);
                deviceListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        selectedDevice = deviceNames.get(i);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        selectedDevice = deviceNames.get(0);
                    }
                });

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        okButton.setEnabled(false);

                        Async.run(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                contextProvider.getP2PSellerService().connect(selectedDevice);
                                return null;
                            }
                        });

                        deviceListSpinner.setVisibility(View.GONE);
                        BusyIndicator.show(dialogContent);
                    }
                });

                deviceListSpinner.setVisibility(View.VISIBLE);
                okButton.setEnabled(true);
                BusyIndicator.hide(dialogContent);
            }
        });
    }

    @Override
    public void onTransmissionComplete()
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelButton.setEnabled(false);
                okButton.setEnabled(true);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        contextProvider.getP2PBuyerService().disconnect();
                        if(exportListener != null)
                            exportListener.onContractDataExchanged(userProfile);

                        dismiss();
                    }
                });
            }
        });
    }

    public interface P2pExportListener
    {
        void onContractDataExchanged(UserProfile buyerProfile);
        void onContractDialogCanceled();
    }
}
