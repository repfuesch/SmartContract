package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiSellerCallback;

/**
 * Created by flo on 23.06.17.
 */

public interface P2PSellerService
{
    void requestConnection(WifiSellerCallback callback, boolean useIdentification);
    void connect(String deviceName);
    void disconnect();
}
