package ch.uzh.ifi.csg.contract.service.connection;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 * Created by flo on 10.04.17.
 */

public interface EthConnectionService
{
    public static String ACTION_HANDLE_CONNECTION_DOWN= "ch.uzh.ifi.csg.smart_contract.connection.down";
    public static String ACTION_HANDLE_CONNECTION_UP = "ch.uzh.ifi.csg.smart_contract.connection.up";

    void startPolling();
    void stopPolling();
    boolean hasConnection();
}
