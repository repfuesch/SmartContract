package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiBuyerCallback;

/**
 * Created by flo on 23.06.17.
 */

public interface P2PBuyerService {
    void connect(WifiBuyerCallback callback);
    void disconnect();
}
