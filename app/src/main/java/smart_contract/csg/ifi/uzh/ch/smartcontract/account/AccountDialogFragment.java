package smart_contract.csg.ifi.uzh.ch.smartcontract.account;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import ch.uzh.ifi.csg.contract.util.ImageHelper;
import smart_contract.csg.ifi.uzh.ch.smartcontract.R;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.permission.PermissionProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.provider.ApplicationContext;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.provider.ApplicationContextProvider;

import static android.app.Activity.RESULT_OK;

/**
 * A dialog fragment to create new accounts or to create accounts from an existing wallet file
 */
public class AccountDialogFragment extends DialogFragment implements TextWatcher{

    private EditText accountNameField;
    private EditText passwordField;
    private RadioButton optionCreateAccount;
    private RadioButton optionImportAccount;
    private LinearLayout accountImportView;
    private TextView walletImportField;

    private Uri walletFileUri;
    private AccountDialogListener listener;
    private ApplicationContext applicationContext;

    private AlertDialog diag;

    public AccountDialogFragment()
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
        walletImportField = (TextView)contentView.findViewById(R.id.field_import_path);
        optionCreateAccount = (RadioButton)contentView.findViewById(R.id.option_create_account);
        optionImportAccount = (RadioButton)contentView.findViewById(R.id.option_import_account);
        accountImportView = (LinearLayout)contentView.findViewById(R.id.account_import_view);
        optionCreateAccount.toggle();
        RadioGroup radioGroup = (RadioGroup)contentView.findViewById(R.id.account_radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if(checkedId == R.id.option_create_account)
                {
                    accountImportView.setVisibility(View.GONE);
                }else {
                    accountImportView.setVisibility(View.VISIBLE);
                }
            }
        });

        ImageButton importButton = (ImageButton) contentView.findViewById(R.id.import_account_button);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //request permission to access external storage
                PermissionProvider permissionProvider = applicationContext.getPermissionProvider();

                if(!permissionProvider.hasPermission(PermissionProvider.READ_STORAGE))
                {
                    permissionProvider.requestPermission(PermissionProvider.READ_STORAGE);
                    return;
                }

                //open file browser
                ImageHelper.openFile(AccountDialogFragment.this);
            }
        });

        builder.setView(contentView);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                String accountName = accountNameField.getText().toString();
                String password = passwordField.getText().toString();
                if(optionCreateAccount.isChecked())
                {
                    listener.onAccountCreated(accountName, password);
                }else{
                    listener.onAccountImported(accountName, password, walletFileUri);
                }
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
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        switch (requestCode)
        {
            case ImageHelper.PICK_FILE_REQUEST_CODE:

                //handle the result of the file pick intent
                if (resultCode == RESULT_OK) {
                    Uri resultUri = intent.getData();
                    if(resultUri != null)
                    {
                        AccountDialogFragment dialog = (AccountDialogFragment)getFragmentManager().findFragmentByTag("AccountDialog");
                        if(dialog != null)
                        {
                            walletImportField.setText(resultUri.getPath());
                            walletFileUri = resultUri;

                            if(!accountNameField.getText().toString().isEmpty())
                                diag.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof AccountDialogListener)
        {
            listener = (AccountDialogListener) context;
        }else{
            throw new RuntimeException(context.toString() + " must implement AccountDialogListener");
        }

        if(context instanceof ApplicationContextProvider)
        {
            applicationContext = ((ApplicationContextProvider) context).getAppContext();
        }else{
            throw new RuntimeException(context.toString() + " must implement ApplicationContextProvider");
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

        if(optionCreateAccount.isChecked())
        {
            if(!accountNameField.getText().toString().isEmpty() && !passwordField.getText().toString().isEmpty())
            {
                diag.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(true);
            }else{
                diag.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(false);
            }
        }else{

            if(!accountNameField.getText().toString().isEmpty() && !walletImportField.getText().toString().isEmpty() && !passwordField.getText().toString().isEmpty())
            {
                diag.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(true);
            }else{
                diag.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(false);
            }
        }
    }

    /**
     * Callback interface that must be implemented by the host Activity. Notifies host about
     * accounts that have to be created.
     */
    public interface AccountDialogListener
    {
        /**
         * Invoked when the user requests the creation of an account with the specified credentials
         *
         * @param accountName
         * @param password
         */
        void onAccountCreated(String accountName, String password);

        /**
         * Invoked when the user requests the creation of an account with the specified credentials
         * from the specified walletFileUri
         *
         * @param accountName
         * @param password
         * @param walletFileUri
         */
        void onAccountImported(String accountName, String password, Uri walletFileUri);
    }
}
