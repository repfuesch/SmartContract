package ch.uzh.ifi.csg.smartcontract.app.p2p.service;

import java.util.List;
import ch.uzh.ifi.csg.smartcontract.app.p2p.connection.ConnectionInfo;
import ch.uzh.ifi.csg.smartcontract.app.p2p.connection.P2PConnectionManager;
import ch.uzh.ifi.csg.smartcontract.library.p2p.peer.P2pSellerCallback;
import ch.uzh.ifi.csg.smartcontract.library.p2p.peer.SellerPeer;
import ch.uzh.ifi.csg.smartcontract.library.service.serialization.GsonSerializationService;


/**
 * Seller implementation of the {@link P2PService}.
 */
public class P2PSellerServiceImpl extends P2PServiceBase<P2pSellerCallback> implements P2PSellerService {

    public P2PSellerServiceImpl(P2PConnectionManager connectionManager)
    {
        super(connectionManager);
    }

    @Override
    public void onPeersChanged(final List<String> deviceList)
    {
        if(callback == null)
            return;

        if(peer != null)
            return;

        callback.onPeersChanged(deviceList);
    }

    @Override
    public void connect(String deviceName) {
        connectionManager.connect(deviceName);
    }

    protected void startPeer(ConnectionInfo connectionInfo)
    {
        if(connectionInfo.isGroupOwner())
        {
            startSellerPeer(connectionInfo.getGroupOwnerPort(), null);

        }else{
            startSellerPeer(connectionInfo.getGroupOwnerPort(), connectionInfo.getGroupOwnerAddress());
        }
    }

    private void startSellerPeer(Integer port, String hostname)
    {
        peer = new SellerPeer(
                new GsonSerializationService(),
                callback,
                port,
                hostname);

        peer.start();
    }
}
