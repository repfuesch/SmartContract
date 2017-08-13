package ch.uzh.ifi.csg.contract.p2p.peer;

import smart_contract.csg.ifi.uzh.ch.smartcontract.p2p.connection.ConnectionInfo;
import smart_contract.csg.ifi.uzh.ch.smartcontract.p2p.service.P2PBuyerServiceImpl;
import smart_contract.csg.ifi.uzh.ch.smartcontract.p2p.service.P2PSellerServiceImpl;

/**
 * Interface for a Peer that sends and receives data over a network connection. Its "start" method
 * is invoked when a network connection has been established to another Peer and its "stop" method
 * is usually invoked after the transmission is complete or when an error occurred during
 * transmission or when the connection is disconnected unexpectedly.
 *
 * see {@link P2PSellerServiceImpl#startPeer(ConnectionInfo)}
 * see {@link P2PBuyerServiceImpl#startPeer(ConnectionInfo)}
 */
public interface Peer
{
    void start();
    void stop();
}
