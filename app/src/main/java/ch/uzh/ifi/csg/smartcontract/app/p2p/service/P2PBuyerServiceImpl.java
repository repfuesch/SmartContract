package ch.uzh.ifi.csg.smartcontract.app.p2p.service;

import java.util.List;
import ch.uzh.ifi.csg.smartcontract.app.p2p.connection.ConnectionInfo;
import ch.uzh.ifi.csg.smartcontract.app.p2p.connection.P2PConnectionManager;
import ch.uzh.ifi.csg.smartcontract.library.p2p.peer.BuyerPeer;
import ch.uzh.ifi.csg.smartcontract.library.p2p.peer.P2pBuyerCallback;
import ch.uzh.ifi.csg.smartcontract.library.service.serialization.GsonSerializationService;


/**
 * Buyer implementation of the {@link P2PService}.
 */
public class P2PBuyerServiceImpl extends P2PServiceBase<P2pBuyerCallback> implements P2PBuyerService {

    public P2PBuyerServiceImpl(P2PConnectionManager connectionManager)
    {
        super(connectionManager);
    }

    @Override
    public void onPeersChanged(List<String> deviceList) {
    }

    protected void startPeer(ConnectionInfo connectionInfo)
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
        peer = new BuyerPeer(
                new GsonSerializationService(),
                callback,
                port,
                hostname);

        peer.start();
    }
}
