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

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
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
/*
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

    private final WifiP2pManager p2pManager;
    private final WifiP2pManager.Channel p2pChannel;
    private IntentFilter intentFilter;

    private TradingPeer tradingPeer;

    private ConnectionListDialogFragment connectionDialog;
    private ArrayList<WifiP2pDevice> deviceList;
    private WifiP2pDevice selectedDevice;
    private boolean isConnected;

    private boolean isGroupOwner;
    private InetAddress groupOwnerAddress;
    private int groupOwnerPort;

    private WifiBuyerCallback buyerCallback;
    private WifiSellerCallback sellerCallback;
    private boolean identificationRequired;

    private ScheduledFuture discoveryTask;

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
    }

    public void detach()
    {
        activity.unregisterReceiver(this);
        activity = null;
        messageHandler = null;
        if(tradingPeer != null)
            tradingPeer.stop();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();

    if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            // Wifi P2P is enabled
            if(discoveryTask == null || discoveryTask.isCancelled())
            {
                discoveryTask = Async.getExecutorService().scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        p2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onFailure(int reasonCode) {
                                //buyerCallback.onConnectionRequest(false);
                            }
                        });
                    }
                }, 0, 1000, TimeUnit.MILLISECONDS);
            }
        } else {
            // Wi-Fi P2P is not enabled
            if(discoveryTask != null && !discoveryTask.isCancelled())
                discoveryTask.cancel(true);
        }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // buyerCallback on PeerListListener.onPeersAvailable()
            //if (p2pManager != null)
                //p2pManager.requestPeers(p2pChannel, this);
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
                isConnected = true;
                p2pManager.requestConnectionInfo(p2pChannel, this);
            }else{
                //handle disconnection of peer
                isConnected = false;
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

        if(sellerCallback != null && selectedDevice == null && tradingPeer == null && connectionDialog == null)
        {
            if(deviceList.size() > 0)
            {
                connectionDialog = new ConnectionListDialogFragment();
                Bundle args = new Bundle();
                args.putSerializable(ConnectionListDialogFragment.DEVICE_LIST_MESSAGE, deviceList);
                connectionDialog.setArguments(args);
                connectionDialog.show(activity.getSupportFragmentManager(), "ConnectionDialogFragment");
            }
        }
    }

    @Override
    public void requestBuyerConnection(final WifiSellerCallback callback, boolean useIdentification)
    {
        this.sellerCallback = callback;
        this.identificationRequired = useIdentification;
        if (p2pManager != null)
        {
            p2pManager.requestPeers(p2pChannel, WifiManagerImpl.this);
        }
    }

    public void requestContractData(WifiBuyerCallback callback)
    {
        this.buyerCallback = callback;
        startPeer();
    }

    @Override
    public void onDeviceSelected(final WifiP2pDevice device) {
        //requestConnection to selected device
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        p2pManager.requestConnection(p2pChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //success logic
            }

            @Override
            public void onFailure(int reason) {
                //failure logic
                sellerCallback.onWifiResponse(new WifiResponse(false, null, "Cannot establish connection to peer!"));
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

            findFreePort("localhost")
                    .done(new DoneCallback<Integer>() {
                        @Override
                        public void onDone(Integer result) {
                            groupOwnerPort = result;
                            startPeer();
                        }
                    });

        } else if (info.groupFormed) {
            isGroupOwner = false;

            findFreePort(groupOwnerAddress.getHostAddress())
                    .done(new DoneCallback<Integer>() {
                        @Override
                        public void onDone(Integer result) {
                            groupOwnerPort = result;
                            startPeer();
                        }
                    });
        }
    }

    private void startPeer()
    {
        if(isGroupOwner)
        {
            TradingClient client = new WifiClient(new GsonSerializationService());

            if(buyerCallback != null)
            {
                startBuyerPeer(groupOwnerPort, client);
            }else if(sellerCallback != null){
                startSellerPeer(groupOwnerPort, client);
            }
        }else{

            TradingClient client = new WifiClient(groupOwnerAddress.getHostAddress(), groupOwnerPort, new GsonSerializationService());

            if(buyerCallback != null)
            {
                startBuyerPeer(null, client);
            }else if(sellerCallback != null){
                startSellerPeer(null, client);
            }
        }
    }

    private static SimplePromise<Integer> findFreePort(final String host)
    {
        return Async.toPromise(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                for(int i = 5000; i < 10000; ++i)
                {
                    try {
                        (new Socket(host, i)).close();

                        // Successful connection means the port is taken.
                        continue;
                    }
                    catch(SocketException e) {
                        // Could not requestConnection.
                        return i;
                    } catch (IOException e) {
                        continue;
                    }
                }

                return -1;
            }
        });
    }

    private void startSellerPeer(Integer port, TradingClient client)
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

    private void startBuyerPeer(Integer port, TradingClient client)
    {
        this.tradingPeer = new BuyerPeer(
                new GsonSerializationService(),
                buyerCallback,
                client,
                this,
                port);

        tradingPeer.start();
    }

    private void deletePersistentGroups(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netid = 0; netid < 32; netid++) {
                        methods[i].invoke(p2pManager, p2pChannel, netid, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //When a running peer server stops, we reset the state of the WifiManager
    @Override
    public void OnTradingPeerStopped()
    {
        tradingPeer = null;
        buyerCallback = null;
        sellerCallback = null;
        selectedDevice = null;
        connectionDialog = null;
        groupOwnerAddress = null;

        if(isConnected)
        {
            deletePersistentGroups();
            p2pManager.removeGroup(p2pChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(int i) {
                    System.out.println("fail");
                }
            });
        }

    }
}
*/