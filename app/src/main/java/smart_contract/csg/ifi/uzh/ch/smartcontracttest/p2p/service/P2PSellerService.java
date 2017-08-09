package smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service;

import ch.uzh.ifi.csg.contract.p2p.peer.P2pSellerCallback;

/**
 * See {@link P2PService}
 */
public interface P2PSellerService extends P2PService<P2pSellerCallback>
{
    /**
     * Tries to establish a P2P connection to the device that has the provided name
     *
     * @param deviceName: The name of the device to connect
     */
    void connect(String deviceName);
}
