package smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
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
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityChangedListener;


/**
 * Created by flo on 23.06.17.
 */
public class WifiConnectionManager extends BroadcastReceiver implements P2PConnectionManager, WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener, ActivityChangedListener
{
    private final WifiP2pManager p2pManager;
    private final WifiP2pManager.Channel p2pChannel;
    private IntentFilter intentFilter;

    private ArrayList<WifiP2pDevice> deviceList;
    private ScheduledFuture peerDiscoveryTask;
    private boolean isConnected;

    private P2PConnectionListener connectionListener;
    private ConnectionInfo connectionInfo;

    public WifiConnectionManager(WifiP2pManager manager, WifiP2pManager.Channel channel)
    {
        this.p2pChannel = channel;
        this.p2pManager = manager;
        this.deviceList = new ArrayList<>();

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

    @Override
    public void startListening(P2PConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
        startPeerDiscovery();

        if(connectionInfo != null)
        {
            connectionListener.onConnectionEstablished(connectionInfo);
        }
    }

    @Override
    public void stopListening() {
        if(peerDiscoveryTask != null && !peerDiscoveryTask.isCancelled())
            peerDiscoveryTask.cancel(true);
    }

    @Override
    public void connect(String deviceName)
    {
        WifiP2pConfig config = new WifiP2pConfig();
        WifiP2pDevice selectedDevice = null;
        for(WifiP2pDevice device : deviceList)
        {
            if(device.deviceName.equals(deviceName))
            {
                selectedDevice = device;
                break;
            }
        }

        if(selectedDevice == null)
            return;

        config.deviceAddress = selectedDevice.deviceAddress;
        p2pManager.connect(p2pChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //success logic
                System.out.println();
            }

            @Override
            public void onFailure(int reason) {
                //failure logic
                if(connectionListener != null)
                    connectionListener.onConnectionError("Cannot establish connection to peer!");
            }
        });
    }

    public void disconnect()
    {
        if (p2pManager != null && p2pChannel != null) {
            p2pManager.requestGroupInfo(p2pChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && p2pManager != null && p2pChannel != null
                            && group.isGroupOwner()) {
                        p2pManager.removeGroup(p2pChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d("P2P", "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d("P2P", "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });
        }
    }

    private void startPeerDiscovery()
    {
        if(peerDiscoveryTask == null || peerDiscoveryTask.isCancelled())
        {
            peerDiscoveryTask = Async.getScheduledExecutorService().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    p2pManager.discoverPeers(p2pChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            p2pManager.requestPeers(p2pChannel, WifiConnectionManager.this);
                            Log.d("P2P", "discoverPeers onSuccess -");
                        }

                        @Override
                        public void onFailure(int reasonCode) {
                            Log.d("P2P", "discoverPeers onFailure -");
                        }
                    });
                }
            }, 0, 1000, TimeUnit.MILLISECONDS);
        }
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
                if(peerDiscoveryTask != null && !peerDiscoveryTask.isCancelled())
                    peerDiscoveryTask.cancel(true);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

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
                if(peerDiscoveryTask != null && !peerDiscoveryTask.isCancelled())
                    peerDiscoveryTask.cancel(true);

                p2pManager.requestConnectionInfo(p2pChannel, this);
            }else{
                //handle disconnection of peer
                isConnected = false;
                connectionInfo = null;
                if(connectionListener != null)
                    connectionListener.onConnectionLost();
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

        if(isConnected)
            return;

        List<String> deviceNames = new ArrayList<>();
        deviceList.clear();
        Iterator<WifiP2pDevice> deviceIterator = wifiP2pDeviceList.getDeviceList().iterator();
        while(deviceIterator.hasNext())
        {
            WifiP2pDevice device = deviceIterator.next();
            deviceNames.add(device.deviceName);
            deviceList.add(device);
        }

        if(connectionListener != null && !isConnected)
        {
            connectionListener.onPeersChanged(deviceNames);
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        // InetAddress from WifiP2pInfo struct.

        String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
        isConnected = true;

        connectionInfo = new ConnectionInfo(groupOwnerAddress);

        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {
            // We are the group owner and the initiator of the connection
            // we do not know the address of the other peer and thus, we expect the connection request from the other peer
            connectionInfo.setGroupOwner(true);

            findFreePort("localhost")
                    .done(new DoneCallback<Integer>() {
                        @Override
                        public void onDone(Integer result) {
                            connectionInfo.setGroupOwnerPort(result);
                            if(connectionListener != null)
                                connectionListener.onConnectionEstablished(connectionInfo);
                        }
                    });

        } else if (info.groupFormed) {
            connectionInfo.setGroupOwner(false);

            findFreePort(connectionInfo.getGroupOwnerAddress())
                    .done(new DoneCallback<Integer>() {
                        @Override
                        public void onDone(Integer result) {
                            connectionInfo.setGroupOwnerPort(result);
                            if(connectionListener != null)
                                connectionListener.onConnectionEstablished(connectionInfo);
                        }
                    });
        }
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

    @Override
    public void onActivityResumed(ActivityBase activity) { activity.registerReceiver(this, intentFilter); }

    @Override
    public void onActivityStopped(ActivityBase activity) {
        activity.unregisterReceiver(this);
    }
}
