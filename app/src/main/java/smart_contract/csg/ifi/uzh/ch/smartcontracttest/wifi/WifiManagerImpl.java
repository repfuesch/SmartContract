package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.MessageHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.dialog.ConnectionListDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.BuyerPeer;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.SellerPeer;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.TradingClient;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.TradingPeer;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiBuyerCallback;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiClient;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiResponse;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiSellerCallback;

/**
 * Created by flo on 16.06.17.
 */

public class WifiManagerImpl
        extends BroadcastReceiver
        implements
        WifiP2pManager.PeerListListener,
        WifiManager,
        ConnectionListDialogFragment.ConnectionListDialogListener,
        WifiP2pManager.ConnectionInfoListener,
        TradingPeer.OnTradingPeerStoppedHandler
{

    private static WifiManagerImpl instance;

    public static WifiManagerImpl create(WifiP2pManager manager, WifiP2pManager.Channel channel)
    {
        if(instance == null)
            instance = new WifiManagerImpl(manager, channel);
        return instance;
    }

    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private IntentFilter intentFilter;

    private TradingPeer tradingPeer;

    private ArrayList<WifiP2pDevice> deviceList;
    private boolean isConnected;
    private boolean isGroupOwner;
    private InetAddress groupOwnerAddress;

    private WifiBuyerCallback buyerCallback;
    private WifiSellerCallback sellerCallback;
    private boolean identificationRequired;

    private AppCompatActivity activity;
    private MessageHandler messageHandler;

    public WifiManagerImpl(WifiP2pManager manager, WifiP2pManager.Channel channel)
    {
        super();
        this.p2pManager = manager;
        this.p2pChannel = channel;
        deviceList = new ArrayList<>();

        initIntentFilter();
    }

    private void initIntentFilter()
    {
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    public void attach(AppCompatActivity activity, MessageHandler messageHandler)
    {
        this.activity = activity;
        this.messageHandler = messageHandler;
        activity.registerReceiver(this, intentFilter);

        p2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                if (p2pManager != null)
                {
                    p2pManager.requestPeers(p2pChannel, WifiManagerImpl.this);
                }
            }

            @Override
            public void onFailure(int reasonCode) {
                //buyerCallback.onConnectionRequest(false);
            }
        });
    }

    public void detach()
    {
        activity.unregisterReceiver(this);
        activity = null;
        messageHandler = null;
        if(tradingPeer != null)
            tradingPeer.stop();
    }

    public List<WifiP2pDevice> getDeviceList() {
        return deviceList;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();

    if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            // Wifi P2P is enabled
        } else {
            // Wi-Fi P2P is not enabled
        }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // buyerCallback on PeerListListener.onPeersAvailable()
            if (p2pManager != null)
                p2pManager.requestPeers(p2pChannel, this);
           // }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            if (p2pManager == null)
                return;

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP
                p2pManager.requestConnectionInfo(p2pChannel, this);
            }else{
                //handle disconnection of peer
                if(tradingPeer != null)
                    tradingPeer.stop();
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList)
    {
        deviceList.clear();
        Iterator<WifiP2pDevice> deviceIterator = wifiP2pDeviceList.getDeviceList().iterator();
        while(deviceIterator.hasNext())
        {
            deviceList.add(deviceIterator.next());
        }
    }

    @Override
    public void requestBuyerConnection(final WifiSellerCallback callback, boolean useIdentification)
    {
        this.sellerCallback = callback;
        this.identificationRequired = useIdentification;

        //if connection is requested by user, show connection list dialog
        if(deviceList != null)
        {
            ConnectionListDialogFragment connectionDialog = new ConnectionListDialogFragment();
            Bundle args = new Bundle();
            args.putSerializable(ConnectionListDialogFragment.DEVICE_LIST_MESSAGE, deviceList);
            connectionDialog.setArguments(args);
            connectionDialog.show(activity.getSupportFragmentManager(), "ConnectionDialogFragment");
        }
    }

    public void requestContractData(WifiBuyerCallback callback)
    {
        this.buyerCallback = callback;
        if(isConnected && isGroupOwner)
        {
            TradingClient client = new WifiClient(8887, new GsonSerializationService());
            startBuyerPeer(8888, client);
        }else if(isConnected && !isGroupOwner)
        {
            TradingClient client = new WifiClient(groupOwnerAddress, 8888, new GsonSerializationService());
            startBuyerPeer(8887, client);
        }

    }

    @Override
    public void onDeviceSelected(final WifiP2pDevice device) {
        //connect to selected device
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        p2pManager.connect(p2pChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //success logic
            }

            @Override
            public void onFailure(int reason) {
                //failure logic
                buyerCallback = null;
                buyerCallback.onWifiResponse(new WifiResponse(false, null, "Cannot establish connection to peer!"));
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info)
    {
        if(tradingPeer != null)
            return;

        // InetAddress from WifiP2pInfo struct.
        groupOwnerAddress = info.groupOwnerAddress;
        isConnected = true;

        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {
            // We are the group owner and the initiator of the connection
            // we do not know the address of the other peer and thus, we expect the connection request from the other peer
            isGroupOwner = true;
            TradingClient client = new WifiClient(8885, new GsonSerializationService());

            if(sellerCallback != null)
            {
                startSellerPeer(8888, client);
            }else{
                startBuyerPeer(8888, client);
            }

        } else if (info.groupFormed) {
            isGroupOwner = false;

            TradingClient client = new WifiClient(groupOwnerAddress, 8888, new GsonSerializationService());

            if(sellerCallback != null)
            {
                startSellerPeer(8885, client);
            }else{
                startBuyerPeer(8885, client);
            }
        }
    }

    private void startSellerPeer(int port, TradingClient client)
    {
        this.tradingPeer = new SellerPeer(
                new GsonSerializationService(),
                sellerCallback,
                client,
                this,
                this.identificationRequired,
                port);

        tradingPeer.start();
    }

    private void startBuyerPeer(int port, TradingClient client)
    {
        this.tradingPeer = new BuyerPeer(
                new GsonSerializationService(),
                buyerCallback,
                client,
                this,
                port);

        tradingPeer.start();
    }

    //When a running peer server stopps, we reset the state of the WifiManager
    @Override
    public void OnTradingPeerStopped()
    {
        tradingPeer = null;
        buyerCallback = null;
        sellerCallback = null;
    }
}
