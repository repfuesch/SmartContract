package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

/**
 * Created by flo on 16.03.17.
 */

public class MessageDialogFragment extends DialogFragment
{
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String EXCEPTION_MESSAGE = "EXCEPTION_MESSAGE";
    public static final String MESSAGE = "MESSAGE";

    private String message;
    private Throwable exception;

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
            }
        });

        builder.setMessage(message);

        // Create the AlertDialog object and return it
        final AlertDialog diag = builder.create();

        return diag;
    }
}
