package smart_contract.csg.ifi.uzh.ch.smartcontracttest.account;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

/**
 * A dialog fragment for creating a new account.
 */
public class AccountCreateDialogFragment extends DialogFragment implements TextWatcher{

    private EditText accountNameField;
    private EditText passwordField;
    private AccountCreateListener listener;

    private AlertDialog diag;

    public AccountCreateDialogFragment()
    {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setTitle(R.string.title_create_account);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.fragment_account_create_dialog, null);
        accountNameField = (EditText) contentView.findViewById(R.id.field_account_name);
        passwordField = (EditText) contentView.findViewById(R.id.field_password);

        builder.setView(contentView);
        builder.setPositiveButton(R.string.action_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                String password = passwordField.getText().toString();
                String accountName = accountNameField.getText().toString();
                listener.onAccountCreate(accountName, password);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        // Create the AlertDialog object and return it
        diag = builder.create();
        diag.show();

        diag.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);

        accountNameField.addTextChangedListener(this);
        passwordField.addTextChangedListener(this);

        return diag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof AccountCreateListener)
        {
            listener = (AccountCreateListener) context;
        }else{
            throw new RuntimeException(context.toString() + " must implement AccountCreateListener");
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if(!accountNameField.getText().toString().isEmpty() && !passwordField.getText().toString().isEmpty())
        {
            diag.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setEnabled(true);
        }else{
            diag.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setEnabled(false);
        }
    }

    public static interface AccountCreateListener
    {
        void onAccountCreate(String accountName, String password);
    }
}
