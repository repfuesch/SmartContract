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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import org.web3j.tx.Contract;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.csg.contract.contract.ContractType;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

/**
 *
 */
public class AddContractDialogFragment extends DialogFragment implements RadioGroup.OnCheckedChangeListener {

    private RadioGroup radioGroup;
    private RadioButton optionCreateContract;
    private RadioButton optionAddContract;
    private Spinner contractTypeSpinner;
    private String selectedContractAddress;
    private ContractType selectedContractType;
    private EditText contractAddressField;
    private LinearLayout contractAddressSection;
    private View contentView;

    private AddContractDialogListener dialogListener;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface AddContractDialogListener {
        void onAddContract(String contractAddress, ContractType type);
        void onCreateContract(ContractType type);
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
            throw new ClassCastException(activity.toString() + " must implement AddContractDialogListener");
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
        builder.setView(contentView, 36, 36, 36, 36);

        final DialogFragment fragment = this;
        builder.setTitle(R.string.add_contract_dialog_title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        // address was added manually
                        if(optionAddContract.isSelected())
                        {
                            selectedContractAddress = contractAddressField.getText().toString();
                            dialogListener.onAddContract(selectedContractAddress, selectedContractType);
                        }else{
                            dialogListener.onCreateContract(selectedContractType);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        // Create the AlertDialog object and return it
        Dialog diag = builder.create();

        contractAddressField = (EditText) contentView.findViewById(R.id.contract_address_field);
        contractAddressSection = (LinearLayout)contentView.findViewById(R.id.manual_contract_address_section);
        optionAddContract = (RadioButton) contentView.findViewById(R.id.option_add_contract);
        optionCreateContract = (RadioButton) contentView.findViewById(R.id.option_create_contract);
        radioGroup = (RadioGroup) contentView.findViewById(R.id.account_radio_group);
        radioGroup.setOnCheckedChangeListener(this);

        contractTypeSpinner = (Spinner) contentView.findViewById(R.id.contract_type_spinner);
        contractTypeSpinner.setAdapter(new ArrayAdapter<ContractType>(contentView.getContext(), android.R.layout.simple_list_item_1, ContractType.values()));
        contractTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedContractType = ContractType.values()[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedContractType = ContractType.Purchase;
            }
        });

        return diag;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

        if(checkedId == R.id.option_add_contract)
        {
            contractAddressSection.setVisibility(View.VISIBLE);
        }else{
            contractAddressSection.setVisibility(View.GONE);
        }
    }
}
