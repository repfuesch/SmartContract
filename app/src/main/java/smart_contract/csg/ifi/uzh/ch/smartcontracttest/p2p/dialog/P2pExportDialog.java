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
import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import ch.uzh.ifi.csg.contract.service.serialization.SerializationService;
import ch.uzh.ifi.csg.contract.util.ImageHelper;
import ezvcard.VCard;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;
import ch.uzh.ifi.csg.contract.p2p.peer.P2pSellerCallback;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.ContractInfoListener;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.P2PSellerService;

/**
 * Seller/export implementation of the {@link P2pDialog}. Requests a P2P connection using the
 * {@link P2PSellerService} and implements the {@link P2pSellerCallback} interface to export the
 * contract that is provided in the {@link #setArguments(Bundle)} method.
 *
 */
public class P2pExportDialog extends P2pDialog implements P2pSellerCallback
{
    public static String MESSAGE_CONTRACT_DATA = "ch.uzh.ifi.csg.smart_contract.exchange.contract_data";

    private P2pExportListener exportListener;

    private ContractInfo contractInfo;
    private List<String> deviceList;
    private Spinner deviceListSpinner;
    private String selectedDevice;

    public P2pExportDialog()
    {
        super("Export Contract");

        deviceList = new ArrayList<>();
    }

    /**
     * Expects the serialized {@link ContractInfo} object to export in the arguments
     *
     * @param args: the arguments
     */
    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        String json = args.getString(MESSAGE_CONTRACT_DATA);
        SerializationService serializationService = new GsonSerializationService();
        contractInfo = serializationService.deserialize(json, ContractInfo.class);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_p2p_export_dialog;
    }

    /**
     * see {@link P2pDialog#onDialogCanceled()}
     */
    @Override
    protected void onDialogCanceled()
    {
        //disconnect from the other device
        contextProvider.getP2PSellerService().disconnect();
        if(exportListener != null)
            exportListener.onContractDialogCanceled();
    }

    /**
     * see {@link P2pDialog#onShowDialog()}
     */
    @Override
    protected void onShowDialog()
    {
        super.onShowDialog();

        //init spinner containing list of device names
        deviceListSpinner = (Spinner) dialog.findViewById(R.id.connection_list_spinner);
        deviceListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedDevice = deviceList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedDevice = deviceList.get(0);
            }
        });

        //requests a P2P connection and registers this object as callback
        contextProvider.getP2PSellerService().requestConnection(P2pExportDialog.this);
        dialogInfo.setText("Waiting for peer list");
        BusyIndicator.show(dialogContent);
    }

    /**
     * see {@link P2pSellerCallback#onUserProfileReceived(UserProfile)}
     */
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

    /**
     * Checks if the device names have changed and updates the device list
     *
     * @param devices: the list of device names
     *
     * @return 'false' if the devices did not change, 'true' otherwise
     */
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

    /**
     * see {@link P2pSellerCallback#onContractInfoRequested(ContractInfoListener)}
     *
     * @param listener: callback to receive the contract details
     */
    @Override
    public void onContractInfoRequested(final ContractInfoListener listener)
    {
        //make sure that no profile info is set
        contractInfo.getUserProfile().setVCard(new VCard());
        contractInfo.getUserProfile().setProfileImagePath("");

        if(contractInfo.isVerifyIdentity())
        {
            //let the user choose which information she wants to share
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

                            //send the contract info to the callback
                            listener.onContractInfoReceived(contractInfo);

                            BusyIndicator.show(dialogContent);
                        }
                    });
                }
            });
        }else{
            //send the contract info to the callback
            listener.onContractInfoReceived(contractInfo);
        }
    }

    /**
     * see {@link P2pSellerCallback#onPeersChanged(List)}
     *
     * @param deviceNames: A list of device names
     */
    @Override
    public void onPeersChanged(final List<String> deviceNames) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(deviceNames.size() == 0)
                    return;

                if(!updateDeviceList(deviceNames))
                    return;

                //enable selection of device
                selectedDevice = deviceList.get(0);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        okButton.setEnabled(false);

                        Async.run(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                //try to connect to the selected device
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

    /**
     * see {@link P2pSellerCallback#onTransmissionComplete()}
     */
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
                        //disconnect from device and return the result to the exportListener
                        contextProvider.getP2PSellerService().disconnect();
                        if(exportListener != null)
                            exportListener.onContractDataExchanged(userProfile);

                        dismiss();
                    }
                });
            }
        });
    }

    /**
     * Interface to retrieve the results of the {@link P2pExportDialog}
     */
    public interface P2pExportListener
    {
        /**
         * Invoked after the contract is sent and the (optional) {@link UserProfile} of the buyer
         * has been received.
         *
         * @param buyerProfile: the profile of the buyer (can be null)
         */
        void onContractDataExchanged(UserProfile buyerProfile);

        /**
         * Invoked when the user canceled the dialog
         */
        void onContractDialogCanceled();
    }
}
