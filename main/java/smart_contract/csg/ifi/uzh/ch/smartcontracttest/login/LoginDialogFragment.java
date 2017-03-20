package smart_contract.csg.ifi.uzh.ch.smartcontracttest.login;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.FailCallback;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ContractErrorHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ServiceProvider;

public class LoginDialogFragment extends DialogFragment
{
    public final static String ACTION_ACCOUNT_CHANGED = "ch.uzh.ifi.csg.smart_contract.account_changed";
    public final static String MESSAGE_ACCOUNT_CHANGED = "ch.uzh.ifi.csg.smart_contract.account";

    private ContractErrorHandler errorHandler;
    private String selectedAccount;

    private View contentView;
    private EditText passwordBox;
    private Spinner accountSpinner;
    private Button unlockButton;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        contentView = inflater.inflate(R.layout.fragment_login_dialog, null);
        passwordBox = (EditText)contentView.findViewById(R.id.editPassword);
        accountSpinner = (Spinner)contentView.findViewById(R.id.account_spinner);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(contentView);
        builder.setPositiveButton(R.string.unlock, null);
        builder.setNegativeButton(R.string.cancel, null);

        final DialogFragment fragment = this;
        builder.setTitle(R.string.login_title);

        // Create the AlertDialog object and return it
        final AlertDialog diag = builder.create();
        diag.setCanceledOnTouchOutside(false);

        initView(contentView);

        diag.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog)
            {
                unlockButton = diag.getButton(AlertDialog.BUTTON_POSITIVE);
                unlockButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        unlockButton.setEnabled(false);
                        onUnlockButtonClick();
                        unlockButton.setEnabled(true);
                    }
                });
            }
        });


        return diag;
    }

    public void initView(View v)
    {
        // Create an ArrayAdapter using the string array and a default spinner layout
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, new ArrayList<String>());

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //fetch all accounts asynchronously and populate the spinner
        ServiceProvider.getInstance().getAccountService().getAccounts()
                .done(new DoneCallback<List<String>>() {
                    @Override
                    public void onDone(final List<String> result) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.addAll(result);
                            }
                        });
                    }
                })
                .fail(new FailCallback() {
                    @Override
                    public void onFail(final Throwable result) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                errorHandler.handleError(result);
                            }
                        });
                    }
                });

        // Apply the adapter to the spinner
        accountSpinner.setAdapter(adapter);
        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                selectedAccount = (String)accountSpinner.getItemAtPosition(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        passwordBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordBox.setText("");
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof ContractErrorHandler) {
            errorHandler = (ContractErrorHandler) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ContractErrorHandler");
        }
    }


    private void onUnlockButtonClick()
    {
        final String password = passwordBox.getText().toString();

        ServiceProvider.getInstance().getAccountService().unlockAccount(selectedAccount, password)
                .done(new DoneCallback<Boolean>() {
                    @Override
                    public void onDone(final Boolean result) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if (result == null) {
                                    handleWrongPassword();
                                    return;
                                }

                                if (result)
                                {
                                    //listener.onAccountUnlocked(selectedAccount);
                                    Intent intent = new Intent();
                                    intent.setAction(ACTION_ACCOUNT_CHANGED);
                                    intent.putExtra(MESSAGE_ACCOUNT_CHANGED, selectedAccount);
                                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                                    dismiss();

                                } else {
                                    handleWrongPassword();
                                }
                            }
                        });
                    }
                }).fail(new FailCallback() {
                    @Override
                    public void onFail(final Throwable result) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handleWrongPassword();
                            }
                        });
                    }
        });
    }

    private void handleWrongPassword()
    {
        Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
        unlockButton.startAnimation(shake);
        passwordBox.setText("");
        passwordBox.requestFocus();
    }
}
