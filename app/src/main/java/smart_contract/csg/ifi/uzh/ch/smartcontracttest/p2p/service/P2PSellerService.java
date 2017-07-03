package smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service;

import ch.uzh.ifi.csg.contract.p2p.peer.P2pSellerCallback;

/**
 * Created by flo on 23.06.17.
 */

public interface P2PSellerService
{
    void requestConnection(P2pSellerCallback callback, boolean useIdentification);
    void connect(String deviceName);
    void disconnect();
}
