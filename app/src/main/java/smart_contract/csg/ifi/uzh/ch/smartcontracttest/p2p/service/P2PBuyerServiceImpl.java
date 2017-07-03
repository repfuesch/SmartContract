package smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service;

import java.util.List;

import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.connection.ConnectionInfo;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.connection.P2PConnectionListener;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.connection.P2PConnectionManager;
import ch.uzh.ifi.csg.contract.p2p.peer.BuyerPeer;
import ch.uzh.ifi.csg.contract.p2p.peer.Peer;
import ch.uzh.ifi.csg.contract.p2p.peer.P2pBuyerCallback;

/**
 * Created by flo on 23.06.17.
 */

public class P2PBuyerServiceImpl implements P2PBuyerService, P2PConnectionListener, Peer.OnPeerStoppedHandler {

    private final P2PConnectionManager connectionManager;
    private P2pBuyerCallback callback;
    private Peer buyerPeer;

    public P2PBuyerServiceImpl(P2PConnectionManager connectionManager)
    {
        this.connectionManager = connectionManager;
    }

    @Override
    public void requestConnection(P2pBuyerCallback callback) {
        this.callback = callback;
        connectionManager.startListening(this);
    }

    @Override
    public void disconnect()
    {
        callback = null;
        if(buyerPeer != null)
            buyerPeer.stop();

        buyerPeer = null;
        connectionManager.stopListening();
        connectionManager.disconnect();
    }

    @Override
    public void onConnectionLost() {
        if(callback != null)
            callback.onP2pErrorMessage("Connection to other Peer lost");
    }

    @Override
    public void onPeersChanged(List<String> deviceList) {
    }

    @Override
    public void onConnectionEstablished(ConnectionInfo connectionInfo) {

        if(callback == null)
            return;

        if(buyerPeer != null)
            return;

        startPeer(connectionInfo);
    }

    private void startPeer(ConnectionInfo connectionInfo)
    {
        if(connectionInfo.isGroupOwner())
        {
            startBuyerPeer(connectionInfo.getGroupOwnerPort(), null);

        }else{

            startBuyerPeer(connectionInfo.getGroupOwnerPort(), connectionInfo.getGroupOwnerAddress());
        }
    }

    private void startBuyerPeer(Integer port, String hostname)
    {
        buyerPeer = new BuyerPeer(
                new GsonSerializationService(),
                callback,
                this,
                port,
                hostname);

        buyerPeer.start();
    }

    @Override
    public void onConnectionError(String message) {
        callback.onP2pErrorMessage("Cannot connect to the other peer");
    }

    @Override
    public void OnPeerStopped()
    {
        buyerPeer = null;
        connectionManager.stopListening();
        connectionManager.disconnect();
    }
}
