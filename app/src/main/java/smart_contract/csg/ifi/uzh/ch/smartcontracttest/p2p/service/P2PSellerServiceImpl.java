package smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service;

import java.util.List;
import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.connection.ConnectionInfo;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.connection.P2PConnectionListener;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.connection.P2PConnectionManager;
import ch.uzh.ifi.csg.contract.p2p.peer.Peer;
import ch.uzh.ifi.csg.contract.p2p.peer.SellerPeer;
import ch.uzh.ifi.csg.contract.p2p.peer.P2pSellerCallback;

/**
 * Created by flo on 23.06.17.
 */

public class P2PSellerServiceImpl implements P2PSellerService, P2PConnectionListener, Peer.OnPeerStoppedHandler {

    private final P2PConnectionManager connectionManager;
    private P2pSellerCallback callback;
    private boolean useIdentification;
    private Peer sellerPeer;

    public P2PSellerServiceImpl(P2PConnectionManager connectionManager)
    {
        this.connectionManager = connectionManager;
    }

    @Override
    public void onConnectionLost() {
        callback.onP2pErrorMessage("Connection to other Peer lost");
    }

    @Override
    public void onPeersChanged(final List<String> deviceList)
    {
        if(callback == null)
            return;

        if(sellerPeer != null)
            return;

        callback.onPeersChanged(deviceList);
    }

    @Override
    public void onConnectionEstablished(ConnectionInfo connectionInfo) {

        if(callback == null)
            return;

        if(connectionInfo.isGroupOwner())
        {
            startSellerPeer(connectionInfo.getGroupOwnerPort(), null);

        }else{
            startSellerPeer(connectionInfo.getGroupOwnerPort(), connectionInfo.getGroupOwnerAddress());
        }
    }

    private void startSellerPeer(Integer port, String hostname)
    {
        sellerPeer = new SellerPeer(
                new GsonSerializationService(),
                callback,
                this,
                port,
                hostname,
                useIdentification);

        sellerPeer.start();
    }

    @Override
    public void onConnectionError(String message) {
        callback.onP2pErrorMessage("Can not connect to the other peer");
    }

    @Override
    public void OnPeerStopped() {
        sellerPeer = null;
        connectionManager.stopListening();
        connectionManager.disconnect();
    }

    @Override
    public void requestConnection(P2pSellerCallback callback, boolean useIdentification)
    {
        this.callback = callback;
        this.useIdentification = useIdentification;
        connectionManager.startListening(this);
    }

    @Override
    public void connect(String deviceName) {
        connectionManager.connect(deviceName);
    }

    @Override
    public void disconnect()
    {
        callback = null;
        if(sellerPeer != null)
            sellerPeer.stop();

        sellerPeer = null;
        connectionManager.stopListening();
        connectionManager.disconnect();
    }
}
