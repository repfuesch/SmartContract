package ch.uzh.ifi.csg.smartcontract.app.common.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import ch.uzh.ifi.csg.smartcontract.app.R;

/**
 * A DialogFragment that is used to display error- and info messages to the user
 */
public class MessageDialogFragment extends DialogFragment
{
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String EXCEPTION_MESSAGE = "EXCEPTION_MESSAGE";
    public static final String MESSAGE = "MESSAGE";

    private String message;
    private Throwable exception;
    private AlertDialog diag;

    private MessageDialogListener listener;

    public MessageDialogFragment()
    {
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        if(args.containsKey(ERROR_MESSAGE))
        {
            message = args.getString(ERROR_MESSAGE);
        }else if(args.containsKey(MESSAGE))
        {
            message = args.getString(MESSAGE);
        }else{
            exception = (Throwable)args.getSerializable(EXCEPTION_MESSAGE);
            if(exception != null)
                message = exception.getMessage();
        }
    }

    public void setDialogListener(MessageDialogListener listener)
    {
        this.listener = listener;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        builder.setTitle("Message");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                diag.dismiss();
            }
        });

        builder.setMessage(message);

        diag = builder.create();

        return diag;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if(listener != null)
            listener.onDialogClosed();

        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        if(listener != null)
            listener.onDialogClosed();

        super.onDismiss(dialog);
    }

    /**
     * Callback interface that notifies the implementing instance when this dialog is closed
     */
    public interface MessageDialogListener
    {
        void onDialogClosed();
    }
}
