package ch.uzh.ifi.csg.contract.p2p.peer;

/**
 * Created by flo on 18.06.17.
 */

public interface Peer {

    void start();
    void stop();

    interface OnPeerStoppedHandler
    {
        void OnPeerStopped();
    }
}
