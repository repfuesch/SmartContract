package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

/**
 * {@link BroadCastService} implementation
 */
public class LocalBroadcastService implements BroadCastService
{
    private Context context;

    public LocalBroadcastService(Context context)
    {
        this.context = context;
    }

    @Override
    public void registerReceiver(BroadcastReceiver receiver, IntentFilter intentFilter) {
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, intentFilter);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    @Override
    public void sendBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
