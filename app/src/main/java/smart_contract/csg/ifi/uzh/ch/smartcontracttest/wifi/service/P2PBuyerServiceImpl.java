package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service;

import android.net.wifi.p2p.WifiP2pDevice;

import java.util.List;

import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.connection.ConnectionInfo;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.connection.P2PConnectionListener;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.connection.P2PConnectionManager;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.BuyerPeer;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.BuyerPeerImpl;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.TradingClient;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.TradingPeer;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiBuyerCallback;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiClient;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiResponse;

/**
 * Created by flo on 23.06.17.
 */

public class P2PBuyerServiceImpl implements P2PBuyerService, P2PConnectionListener, TradingPeer.OnTradingPeerStoppedHandler {

    private final P2PConnectionManager connectionManager;
    private WifiBuyerCallback callback;
    private TradingPeer buyerPeer;

    public P2PBuyerServiceImpl(P2PConnectionManager connectionManager)
    {
        this.connectionManager = connectionManager;
    }

    @Override
    public void requestConnection(WifiBuyerCallback callback) {
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
            callback.onWifiResponse(new WifiResponse(false, null, "Connection to other Peer lost"));
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
            //TradingClient client = new WifiClient(new GsonSerializationService());
            startBuyerPeer(connectionInfo.getGroupOwnerPort(), null);

        }else{

           // TradingClient client = new WifiClient(connectionInfo.getGroupOwnerAddress(), connectionInfo.getGroupOwnerPort(), new GsonSerializationService());
            startBuyerPeer(connectionInfo.getGroupOwnerPort(), connectionInfo.getGroupOwnerAddress());
        }
    }

    private void startBuyerPeer(Integer port, String hostname)
    {
        buyerPeer = new BuyerPeerImpl(
                new GsonSerializationService(),
                callback,
                this,
                port,
                hostname);

        buyerPeer.start();
    }

    @Override
    public void onConnectionError(String message) {
        callback.onWifiResponse(new WifiResponse(false, null, message));
    }

    @Override
    public void OnTradingPeerStopped()
    {
        buyerPeer = null;
        connectionManager.stopListening();
        connectionManager.disconnect();
    }
}
