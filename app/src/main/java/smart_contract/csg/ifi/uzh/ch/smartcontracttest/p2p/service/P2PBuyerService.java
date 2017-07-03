package smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service;

import ch.uzh.ifi.csg.contract.p2p.peer.P2pBuyerCallback;

/**
 * Created by flo on 23.06.17.
 */

public interface P2PBuyerService {
    void requestConnection(P2pBuyerCallback callback);
    void disconnect();
}
