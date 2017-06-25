package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.connection;

import android.app.Activity;
import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by flo on 23.06.17.
 */

public interface P2PConnectionManager
{
    void startListening(P2PConnectionListener connectionListener);
    void stopListening();
    void connect(String deviceName);
    void disconnect();
}
