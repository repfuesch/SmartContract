package smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import java.io.File;

import ch.uzh.ifi.csg.contract.common.ImageHelper;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;
import ch.uzh.ifi.csg.contract.p2p.peer.P2pBuyerCallback;

/**
 * Created by flo on 24.06.17.
 */

public class P2pImportDialog extends P2pDialog implements P2pBuyerCallback
{
    private P2pImportListener importListener;
    private ContractInfo contractInfo;

    public P2pImportDialog()
    {
        super("Import Dialog");
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
        setCancelable(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);

        contextProvider.getP2PBuyerService().requestConnection(P2pImportDialog.this);

        BusyIndicator.show(dialogContent);
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
                contractInfo = new ContractInfo(info.getContractType(), info.getContractAddress(), userProfile, info.getImages());

                if(importListener != null)
                    importListener.onContractDataReceived(contractInfo);

                dismiss();
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

    public static interface P2pImportListener {
        void onContractDataReceived(ContractInfo contract);
        void onContractDialogCanceled();
    }
}
