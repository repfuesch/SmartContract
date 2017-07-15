package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.broadcast;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by flo on 13.07.17.
 */

public interface BroadCastService {

    void registerReceiver(BroadcastReceiver receiver, IntentFilter intentFilter);
    void unregisterReceiver(BroadcastReceiver receiver);
    void sendBroadcast(Intent intent);

}

