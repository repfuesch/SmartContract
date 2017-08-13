package smart_contract.csg.ifi.uzh.ch.smartcontract.common.broadcast;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Service interface to access the functionality of the
 * {@link android.support.v4.content.LocalBroadcastManager}
 */
public interface BroadCastService
{
    /**
     * Register a receive for any local broadcasts that match the given IntentFilter.
     *
     * @param receiver The BroadcastReceiver to handle the broadcast.
     * @param intentFilter Selects the Intent broadcasts to be received.
     *
     * @see #unregisterReceiver
     */
    void registerReceiver(BroadcastReceiver receiver, IntentFilter intentFilter);

    /**
     * Unregister a previously registered BroadcastReceiver.  <em>All</em>
     * filters that have been registered for this BroadcastReceiver will be
     * removed.
     *
     * @param receiver The BroadcastReceiver to unregister.
     *
     * @see #registerReceiver
     */
    void unregisterReceiver(BroadcastReceiver receiver);

    /**
     * Broadcast the given intent to all interested BroadcastReceivers.  This
     * call is asynchronous; it returns immediately, and you will continue
     * executing while the receivers are run.
     *
     * @param intent The Intent to broadcast; all receivers matching this
     *     Intent will receive the broadcast.
     *
     * @see #registerReceiver
     */
    void sendBroadcast(Intent intent);
}

