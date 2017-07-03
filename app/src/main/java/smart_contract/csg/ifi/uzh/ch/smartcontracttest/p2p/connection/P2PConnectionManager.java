package smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.connection;

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
