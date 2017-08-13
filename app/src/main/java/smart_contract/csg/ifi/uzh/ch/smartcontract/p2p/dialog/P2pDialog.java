package smart_contract.csg.ifi.uzh.ch.smartcontract.p2p.dialog;

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

import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import smart_contract.csg.ifi.uzh.ch.smartcontract.R;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.provider.ApplicationContext;
import ch.uzh.ifi.csg.contract.p2p.peer.P2pCallback;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.provider.ApplicationContextProvider;

/**
 * Base {@link DialogFragment} that implements the {@link P2pCallback} interface
 */
public abstract class P2pDialog extends DialogFragment implements P2pCallback {

    protected ApplicationContext contextProvider;
    protected UserProfile userProfile;
    protected AlertDialog dialog;
    protected LinearLayout dialogContent;
    protected TextView dialogInfo;
    protected LinearLayout verifyProfileView;
    protected CheckBox profileImageCheckbox;
    protected Button cancelButton;
    protected Button okButton;

    private String title;

    public P2pDialog(String dialogTitle)
    {
        this.title = dialogTitle;

    }

    /**
     * Returns the LayoutId of the derived Fragment
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * Has to be implemented by derived classes to handle dialog cancelation
     */
    protected abstract void onDialogCanceled();

    /**
     * Executed after the DialogFragment is displayed
     */
    protected void onShowDialog()
    {
        setCancelable(false);
        okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        okButton.setEnabled(false);
        cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        cancelButton.setEnabled(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(getLayoutId(), null);
        contentView.setBackgroundColor(Color.TRANSPARENT);

        //find UI components
        verifyProfileView = (LinearLayout) contentView.findViewById(R.id.select_profile_details_view);
        dialogContent = (LinearLayout) contentView.findViewById(R.id.dialog_content);
        dialogInfo = (TextView) contentView.findViewById(R.id.dialog_info);
        profileImageCheckbox = (CheckBox) contentView.findViewById(R.id.checkbox_profile_image);

        builder.setTitle(title);
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onDialogCanceled();
            }
        });

        builder.setView(contentView);
        dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                onShowDialog();
            }
        });

        return dialog;
    }

    /**
     * see {@link P2pCallback#onP2pErrorMessage(String)}
     * @param message
     */
    @Override
    public void onP2pErrorMessage(final String message) {
        if(getActivity() == null)
            return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogInfo.setText(message);
            }
        });
    }

    /**
     * see {@link P2pCallback#onP2pInfoMessage(String)}
     * @param message
     */
    @Override
    public void onP2pInfoMessage(final String message) {
        if(getActivity() == null)
            return;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogInfo.setText(message);
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attachContext(activity);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        attachContext(context);
    }

    protected void attachContext(Context context)
    {
        if(context instanceof ApplicationContextProvider)
        {
            contextProvider = ((ApplicationContextProvider)context).getAppContext();
        }else{
            throw new RuntimeException(context.toString() + " must implement ApplicationContextProvider");
        }
    }
}
