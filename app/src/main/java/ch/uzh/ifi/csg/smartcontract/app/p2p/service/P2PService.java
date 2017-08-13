package ch.uzh.ifi.csg.smartcontract.app.p2p.service;

import ch.uzh.ifi.csg.smartcontract.library.p2p.peer.P2pCallback;

/**
 * Service interface used by UI components to request a P2P connection or to disconnect an existing
 * connection
 */
public interface P2PService<T extends P2pCallback> {
    /**
     * Disconnects an existing connection to another device.
     * See {@link P2PServiceBase#disconnect()}
     */
    void disconnect();

    /**
     * Requests a P2P connection to another device and registers a callback to send and receive data
     *
     * See {@link P2PServiceBase#requestConnection(P2pCallback)}
     *
     * @param callback: Callback used to send and receive data
     */
    void requestConnection(T callback);
}
