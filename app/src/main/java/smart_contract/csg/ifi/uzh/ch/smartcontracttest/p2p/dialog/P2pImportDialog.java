package smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import java.io.File;

import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.util.ImageHelper;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;
import ch.uzh.ifi.csg.contract.p2p.peer.P2pBuyerCallback;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.UserProfileListener;

/**
 * Created by flo on 24.06.17.
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

    @Override
    protected void onDialogCanceled()
    {
        contextProvider.getP2PBuyerService().disconnect();
        if(importListener != null)
            importListener.onContractDialogCanceled();
    }

    @Override
    protected void onShowDialog()
    {
        super.onShowDialog();
        contextProvider.getP2PBuyerService().requestConnection(P2pImportDialog.this);
        BusyIndicator.show(dialogContent);
    }

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
                        if(importListener != null)
                            importListener.onContractDataReceived(contractInfo);

                        dismiss();
                    }
                });
            }
        });
    }

    public static interface P2pImportListener {
        void onContractDataReceived(ContractInfo contract);
        void onContractDialogCanceled();
    }
}
