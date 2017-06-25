package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.connection;

import android.net.wifi.p2p.WifiP2pDevice;

import java.util.List;

/**
 * Created by flo on 23.06.17.
 */

public interface P2PConnectionListener {

    void onConnectionLost();
    void onPeersChanged(List<String> deviceList);
    void onConnectionEstablished(ConnectionInfo connectionInfo);
    void onConnectionError(String message);
}
