package ch.uzh.ifi.csg.smartcontract.app.p2p.service;

import java.util.List;

import ch.uzh.ifi.csg.smartcontract.app.p2p.connection.ConnectionInfo;
import ch.uzh.ifi.csg.smartcontract.app.p2p.connection.P2PConnectionListener;
import ch.uzh.ifi.csg.smartcontract.app.p2p.connection.P2PConnectionManager;
import ch.uzh.ifi.csg.smartcontract.library.p2p.peer.P2pCallback;
import ch.uzh.ifi.csg.smartcontract.library.p2p.peer.Peer;

/**
 * Base class for all {@link P2PService} implementations. Implements the
 * {@link P2PConnectionListener} to communicate with the {@link P2PConnectionManager} instance.
 *
 */
public abstract class P2PServiceBase<T extends P2pCallback> implements P2PService<T>, P2PConnectionListener
{
    protected final P2PConnectionManager connectionManager;
    protected T callback;
    protected Peer peer;

    public P2PServiceBase(P2PConnectionManager connectionManager)
    {
        this.connectionManager = connectionManager;
    }

    protected abstract void startPeer(ConnectionInfo info);

    @Override
    public void onConnectionError(String message) {
        if(callback != null)
            callback.onP2pErrorMessage(message);
    }

    @Override
    public void requestConnection(T callback)
    {
        this.callback = callback;
        connectionManager.startListening(this);
        callback.onP2pInfoMessage("Waiting for connection request");
    }

    @Override
    public void disconnect()
    {
        callback = null;
        if(peer != null)
            peer.stop();

        peer = null;
        connectionManager.stopListening();
        connectionManager.disconnect();
    }

    @Override
    public void onConnectionLost() {
        if(callback != null)
            callback.onP2pErrorMessage("Connection to other Peer lost");
    }

    @Override
    public abstract void onPeersChanged(List<String> deviceList);

    @Override
    public void onConnectionEstablished(ConnectionInfo connectionInfo) {

        if(callback == null)
            return;

        if(peer != null)
            return;

        startPeer(connectionInfo);
    }
}
