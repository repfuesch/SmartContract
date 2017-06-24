package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityChangedListener;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.connection.ConnectionInfo;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.connection.P2PConnectionListener;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.connection.P2PConnectionManager;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.dialog.ConnectionListDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.SellerPeer;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.TradingClient;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.TradingPeer;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiClient;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiResponse;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiSellerCallback;

/**
 * Created by flo on 23.06.17.
 */

public class P2PSellerServiceImpl implements P2PSellerService, P2PConnectionListener, TradingPeer.OnTradingPeerStoppedHandler, ConnectionListDialogFragment.ConnectionListDialogListener, ActivityChangedListener {

    private final P2PConnectionManager connectionManager;
    private WifiSellerCallback callback;
    private boolean useIdentification;
    private TradingPeer sellerPeer;

    private AppCompatActivity activity;
    private ConnectionListDialogFragment connectionDialog;

    public P2PSellerServiceImpl(P2PConnectionManager connectionManager)
    {
        this.connectionManager = connectionManager;
    }

    @Override
    public void onConnectionLost() {
        callback.onWifiResponse(new WifiResponse(false, null, "Connection to other Peer lost"));
    }

    @Override
    public void onPeersChanged(final List<WifiP2pDevice> deviceList)
    {
        if(activity == null)
            return;

        if(callback == null)
            return;

        if(sellerPeer != null)
            return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(connectionDialog == null)
                {
                    connectionDialog = new ConnectionListDialogFragment();
                    Bundle args = new Bundle();
                    args.putSerializable(ConnectionListDialogFragment.DEVICE_LIST_MESSAGE, new ArrayList<>(deviceList));
                    connectionDialog.setArguments(args);
                    connectionDialog.show(activity.getSupportFragmentManager(), "ConnectionDialogFragment");

                }else
                {
                    connectionDialog.updateDeviceList(deviceList);
                }
            }
        });
    }

    @Override
    public void onConnectionEstablished(ConnectionInfo connectionInfo) {

        if(callback == null)
            return;

        if(connectionInfo.isGroupOwner())
        {
            TradingClient client = new WifiClient(new GsonSerializationService());
            startSellerPeer(connectionInfo.getGroupOwnerPort(), client);

        }else{

            TradingClient client = new WifiClient(connectionInfo.getGroupOwnerAddress(), connectionInfo.getGroupOwnerPort(), new GsonSerializationService());
            startSellerPeer(null, client);
        }
    }

    private void startSellerPeer(Integer port, TradingClient client)
    {
        sellerPeer = new SellerPeer(
                new GsonSerializationService(),
                callback,
                client,
                this,
                useIdentification,
                port);

        sellerPeer.start();
    }

    @Override
    public void onConnectionError(String message) {
        callback.onWifiResponse(new WifiResponse(false, null, message));
    }

    @Override
    public void OnTradingPeerStopped() {
        sellerPeer = null;
        connectionDialog = null;
        connectionManager.stopListening();
        connectionManager.disconnect();
    }

    @Override
    public void connect(WifiSellerCallback callback, boolean useIdentification)
    {
        this.callback = callback;
        this.useIdentification = useIdentification;
        connectionManager.startListening(this);
    }

    @Override
    public void disconnect()
    {
        callback = null;
        if(sellerPeer != null)
            sellerPeer.stop();

        sellerPeer = null;
        connectionManager.stopListening();
        connectionManager.disconnect();
    }

    @Override
    public void onDeviceSelected(WifiP2pDevice device) {
        //connectionManager.stopListening();
        connectionDialog = null;
        connectionManager.connect(device);
    }

    @Override
    public void onDialogCancelled() {
        connectionManager.stopListening();
        sellerPeer = null;
        connectionDialog = null;
    }

    @Override
    public void onActivityResumed(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onActivityStopped(AppCompatActivity activity) {
        this.activity = null;
    }
}
