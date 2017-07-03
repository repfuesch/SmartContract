package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service;

import java.util.List;
import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.connection.ConnectionInfo;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.connection.P2PConnectionListener;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.connection.P2PConnectionManager;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.SellerPeer;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.SellerPeerImpl;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.TradingClient;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.TradingPeer;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiClient;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiResponse;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiSellerCallback;

/**
 * Created by flo on 23.06.17.
 */

public class P2PSellerServiceImpl implements P2PSellerService, P2PConnectionListener, TradingPeer.OnTradingPeerStoppedHandler {

    private final P2PConnectionManager connectionManager;
    private WifiSellerCallback callback;
    private boolean useIdentification;
    private TradingPeer sellerPeer;

    public P2PSellerServiceImpl(P2PConnectionManager connectionManager)
    {
        this.connectionManager = connectionManager;
    }

    @Override
    public void onConnectionLost() {
        callback.onWifiResponse(new WifiResponse(false, null, "Connection to other Peer lost"));
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
            //TradingClient client = new WifiClient(new GsonSerializationService());
            startSellerPeer(connectionInfo.getGroupOwnerPort(), null);

        }else{
            //TradingClient client = new WifiClient(connectionInfo.getGroupOwnerAddress(), connectionInfo.getGroupOwnerPort(), new GsonSerializationService());
            startSellerPeer(connectionInfo.getGroupOwnerPort(), connectionInfo.getGroupOwnerAddress());
        }
    }

    private void startSellerPeer(Integer port, String hostname)
    {
        sellerPeer = new SellerPeerImpl(
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
        callback.onWifiResponse(new WifiResponse(false, null, message));
    }

    @Override
    public void OnTradingPeerStopped() {
        sellerPeer = null;
        connectionManager.stopListening();
        connectionManager.disconnect();
    }

    @Override
    public void requestConnection(WifiSellerCallback callback, boolean useIdentification)
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
