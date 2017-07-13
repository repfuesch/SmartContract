package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by flo on 13.07.17.
 */

public class LocalBroadcastService implements BroadCastService
{
    private static LocalBroadcastService instance;
    private Context context;

    public static LocalBroadcastService create(Context context)
    {
        if(instance == null)
            instance = new LocalBroadcastService(context);
        return instance;
    }

    public LocalBroadcastService(Context context)
    {
        this.context = context;
    }

    @Override
    public void registerReceiver(BroadcastReceiver receiver, IntentFilter intentFilter) {
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, intentFilter);
    }

    @Override
    public void sendBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
