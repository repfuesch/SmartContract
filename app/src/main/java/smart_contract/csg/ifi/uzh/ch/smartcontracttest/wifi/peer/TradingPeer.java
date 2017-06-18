package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

/**
 * Created by flo on 18.06.17.
 */

public interface TradingPeer {

    void start();
    void stop();

    interface OnTradingPeerStoppedHandler
    {
        void OnTradingPeerStopped();
    }
}
