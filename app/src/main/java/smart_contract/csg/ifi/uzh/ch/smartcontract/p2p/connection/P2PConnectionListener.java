package smart_contract.csg.ifi.uzh.ch.smartcontract.p2p.connection;

import java.util.List;

import smart_contract.csg.ifi.uzh.ch.smartcontract.p2p.service.P2PService;

/**
 * Callback interface that provides information about the state of P2P connections and devices.
 *
 * see {@link P2PService}
 * see {@link P2PConnectionManager}
 */
public interface P2PConnectionListener {

    /**
     * Invoked when the connection to the other device is interrupted
     */
    void onConnectionLost();

    /**
     * Invoked when the list of available devices in the environment changed
     * @param deviceList: List of devices
     */
    void onPeersChanged(List<String> deviceList);

    /**
     * Invoked when a connection to another device has been established.
     *
     * @param connectionInfo: information about the group owner
     */
    void onConnectionEstablished(ConnectionInfo connectionInfo);

    /**
     * Invoked when an error occurred during the connection setup
     *
     * @param message
     */
    void onConnectionError(String message);
}
