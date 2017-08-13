package ch.uzh.ifi.csg.smartcontract.library.service.connection;

/**
 * Service interface to start- and stop checking the connection status to the Ethereum client.
 */
public interface EthConnectionService
{
    String ACTION_HANDLE_CONNECTION_DOWN= "ch.uzh.ifi.csg.smart_contract.connection.down";
    String ACTION_HANDLE_CONNECTION_UP = "ch.uzh.ifi.csg.smart_contract.connection.up";

    void startPolling();
    void stopPolling();
    boolean hasConnection();
}
