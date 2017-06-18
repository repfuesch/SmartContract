package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;

/**
 * Created by flo on 17.06.17.
 */

public class ConnectionListDialogFragment extends DialogFragment
{
    public static final String DEVICE_LIST_MESSAGE = "DEVICE_LIST_MESSAGE";

    private List<WifiP2pDevice> deviceList;
    private WifiP2pDevice selectedDevice;
    private ConnectionListDialogListener listener;

    public ConnectionListDialogFragment()
    {
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        deviceList = (List<WifiP2pDevice>) args.getSerializable(DEVICE_LIST_MESSAGE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.fragment_connection_list_dialog, null);
        contentView.setBackgroundColor(Color.TRANSPARENT);

        Spinner connectionListSpinner = (Spinner) contentView.findViewById(R.id.connection_list_spinner);
        List<String> deviceNames = new ArrayList<>(deviceList.size());
        for(WifiP2pDevice device : deviceList)
        {
            deviceNames.add(device.deviceName);
        }

        connectionListSpinner.setAdapter(new ArrayAdapter<String>(contentView.getContext(), android.R.layout.simple_list_item_1, deviceNames));
        connectionListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedDevice = deviceList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedDevice = null;
            }
        });

        builder.setTitle("Select a device");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(selectedDevice != null)
                    listener.onDeviceSelected(selectedDevice);
            }
        }).setNegativeButton(R.string.cancel, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(contentView);

        // Create the AlertDialog object and return it
        final AlertDialog diag = builder.create();

        return diag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        attachContext(context);
    }

    private void attachContext(Context context)
    {
        if(context instanceof ApplicationContextProvider)
        {
            listener = (ConnectionListDialogListener) (((ApplicationContextProvider) context).getWifiManager());
        }else{
            throw new RuntimeException(context.toString() + " must implement ConnectionListDialogListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attachContext(activity);
    }

    public static interface ConnectionListDialogListener {
        void onDeviceSelected(WifiP2pDevice device);
    }
}
