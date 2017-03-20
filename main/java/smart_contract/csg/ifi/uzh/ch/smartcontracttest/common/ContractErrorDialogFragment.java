package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

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

public class ContractErrorDialogFragment extends DialogFragment
{
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String EXCEPTION_MESSAGE = "EXCEPTION_MESSAGE";

    private String errorMessage;
    private Throwable exception;
    private ExceptionConfirmedListener listener;

    public ContractErrorDialogFragment()
    {
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        errorMessage = args.getString(ERROR_MESSAGE);
        exception = (Throwable)args.getSerializable(EXCEPTION_MESSAGE);
        if(exception != null)
            errorMessage = exception.getMessage();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        builder.setTitle(R.string.error_title);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(exception != null)
                    listener.onExceptionConfirmed(exception);
            }
        });

        builder.setMessage("An error occurred while executing a transaction: \n" + errorMessage);

        // Create the AlertDialog object and return it
        final AlertDialog diag = builder.create();

        return diag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof ExceptionConfirmedListener)
        {
            listener = (ExceptionConfirmedListener) context;
        }else{
            throw new RuntimeException(context.toString() + " must implement ExceptionConfirmedListener");
        }
    }

    public static interface ExceptionConfirmedListener
    {
        void onExceptionConfirmed(Throwable throwable);
    }
}
