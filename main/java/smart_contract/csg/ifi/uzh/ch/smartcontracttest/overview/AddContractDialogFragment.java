package smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

/**
 *
 */
public class AddContractDialogFragment extends DialogFragment
{
    private String selectedContractSource;
    private String selectedContractAddress;
    private EditText contractAddressField;
    private LinearLayout contractAddressSection;
    private Spinner spinner;
    private View contentView;
    private AddContractDialogListener dialogListener;


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface AddContractDialogListener {
        public void onAddContract(DialogFragment dialog, String contractAddress);
        public void onContractDialogCanceled(DialogFragment dialog);
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            dialogListener = (AddContractDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        contentView = inflater.inflate(R.layout.dialog_add_contract, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(contentView);

        final DialogFragment fragment = this;
        builder.setTitle(R.string.add_contract_dialog_title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // address was added manually
                        if(selectedContractSource.equals(getString(R.string.add_contract_manually)))
                        {
                            selectedContractAddress = contractAddressField.getText().toString();
                            dialogListener.onAddContract(fragment, selectedContractAddress);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialogListener.onContractDialogCanceled(fragment);
                    }
                });

        // Create the AlertDialog object and return it
        Dialog diag = builder.create();
        contractAddressField = (EditText) contentView.findViewById(R.id.contract_address_field);
        spinner = (Spinner)contentView.findViewById(R.id.contract_source_spinner);
        contractAddressSection = (LinearLayout)contentView.findViewById(R.id.manual_contract_address_section);
        initSpinner(diag);
        return diag;
    }


    private void initSpinner(Dialog diag)
    {
        final List<String> contractSources = new ArrayList<>();
        contractSources.add(getString(R.string.add_contract_manually));
        contractSources.add(getString(R.string.add_contract_automatically));
        spinner.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, contractSources));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                selectedContractSource = contractSources.get(i);
                if(selectedContractSource.equals(getString(R.string.add_contract_manually)))
                {
                    contractAddressSection.setVisibility(View.VISIBLE);
                }else{
                    contractAddressSection.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                contractAddressSection.setVisibility(View.GONE);
            }
        });
    }
}
