package smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.connection;

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
