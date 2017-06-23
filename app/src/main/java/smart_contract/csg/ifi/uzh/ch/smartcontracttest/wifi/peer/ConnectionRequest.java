package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

/**
 * Created by flo on 18.06.17.
 */

public class ConnectionRequest {

    private int listeningPort;

    public ConnectionRequest(int listeningPort)
    {
        this.listeningPort = listeningPort;
    }

    public int getListeningPort() {
        return listeningPort;
    }
}
