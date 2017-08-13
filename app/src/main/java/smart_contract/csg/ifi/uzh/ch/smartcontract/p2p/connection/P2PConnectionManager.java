package smart_contract.csg.ifi.uzh.ch.smartcontract.p2p.connection;

/**
 * Interface to discover devices in the environment and to connect/disconnect to them
 */
public interface P2PConnectionManager
{
    /**
     * Starts discovering peers in the environment of the device and accepts a callback for
     * subsequent updates.
     *
     * @param connectionListener: see {@link P2PConnectionListener}
     */
    void startListening(P2PConnectionListener connectionListener);

    /**
     * Stops discovering peers
     */
    void stopListening();

    /**
     * Tries to connect to the device with the specified name
     *
     * @param deviceName
     */
    void connect(String deviceName);

    /**
     * Disconnect an established P2P connection
     */
    void disconnect();
}
