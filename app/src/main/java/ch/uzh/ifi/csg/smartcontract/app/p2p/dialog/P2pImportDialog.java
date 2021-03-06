package ch.uzh.ifi.csg.smartcontract.app.p2p.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import java.io.File;
import ch.uzh.ifi.csg.smartcontract.app.R;
import ch.uzh.ifi.csg.smartcontract.app.common.controls.BusyIndicator;
import ch.uzh.ifi.csg.smartcontract.app.p2p.service.P2PBuyerService;
import ch.uzh.ifi.csg.smartcontract.app.p2p.service.UserProfileListener;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.ContractInfo;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.UserProfile;
import ch.uzh.ifi.csg.smartcontract.library.p2p.peer.P2pBuyerCallback;
import ch.uzh.ifi.csg.smartcontract.library.util.ImageHelper;

/**
 * Buyer/import implementation of the {@link P2pDialog}. Requests a P2P connection using the
 * {@link P2PBuyerService} and implements the {@link P2pBuyerCallback} interface to import a
 * contract over a P2P connection
 *
 */
public class P2pImportDialog extends P2pDialog implements P2pBuyerCallback
{
    private P2pImportListener importListener;
    private ContractInfo contractInfo;

    public P2pImportDialog()
    {
        super("Import Contract");
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_p2p_import_dialog;
    }

    /**
     * see {@link P2pDialog#onDialogCanceled()}
     */
    @Override
    protected void onDialogCanceled()
    {
        //disconnect from the other peer and inform the listener
        contextProvider.getP2PBuyerService().disconnect();
        if(importListener != null)
            importListener.onContractDialogCanceled();
    }

    /**
     * see {@link P2pDialog#onShowDialog()}
     */
    @Override
    protected void onShowDialog()
    {
        super.onShowDialog();
        contextProvider.getP2PBuyerService().requestConnection(P2pImportDialog.this);
        BusyIndicator.show(dialogContent);
    }

    /**
     * see {@link P2pBuyerCallback#onContractInfoReceived(ContractInfo)}
     *
     * @param info
     */
    @Override
    public void onContractInfoReceived(final ContractInfo info) {

        for(String imgSig : info.getImages().keySet())
        {
            //copy the images into the correct application path
            File newFile = ImageHelper.saveImageFile(info.getImages().get(imgSig), contextProvider.getSettingProvider().getImageDirectory());
            info.getImages().put(imgSig, newFile.getAbsolutePath());
        }

        if(info.getUserProfile().getProfileImagePath() != null)
        {
            //copy the images into the correct application path
            File newFile = ImageHelper.saveImageFile(info.getUserProfile().getProfileImagePath(), contextProvider.getSettingProvider().getImageDirectory());
            info.getUserProfile().setProfileImagePath(newFile.getAbsolutePath());
        }

        contractInfo = info;
    }

    /**
     * see {@link P2pBuyerCallback#onUserProfileRequested(UserProfileListener)}
     *
     * @param listener: Callback invoked when the UserProfile is ready to be sent.
     */
    @Override
    public void onUserProfileRequested(final UserProfileListener listener)
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

                        final UserProfile profile = new UserProfile();
                        profile.setVCard(localProfile.getVCard());

                        if(profileImageCheckbox.isChecked())
                        {
                            profile.setProfileImagePath(localProfile.getProfileImagePath());
                        }

                        //Update the listener
                        listener.onUserProfileReceived(profile);

                        BusyIndicator.show(dialogContent);
                    }
                });
            }
        });

    }

    protected void attachContext(Context context)
    {
        super.attachContext(context);

        if(context instanceof P2pImportListener)
        {
            importListener = (P2pImportListener)context;
        }else{
            throw new RuntimeException(context.toString() + " must implement P2pImportListener");
        }
    }

    /**
     * see {@link P2pBuyerCallback#onTransmissionComplete()}
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
                        //disconnect from the other peer and inform the listener
                        contextProvider.getP2PBuyerService().disconnect();
                        if(importListener != null)
                            importListener.onContractDataReceived(contractInfo);

                        dismiss();
                    }
                });
            }
        });
    }

    /**
     * Interface that must be implemented by the parent Activity that hosts the Dialog.
     * Provides information about the result of the {@link P2pImportDialog}
     */
    public interface P2pImportListener {
        /**
         * Returns the imported {@link ContractInfo} object
         *
         * @param contract
         */
        void onContractDataReceived(ContractInfo contract);

        /**
         * Invoked when the dialog was canceled by the user.
         */
        void onContractDialogCanceled();
    }
}
