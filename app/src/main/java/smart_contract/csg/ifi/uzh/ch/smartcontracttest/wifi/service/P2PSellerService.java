package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiSellerCallback;

/**
 * Created by flo on 23.06.17.
 */

public interface P2PSellerService {
    void connect(WifiSellerCallback callback, boolean useIdentification);
    void disconnect();
}
