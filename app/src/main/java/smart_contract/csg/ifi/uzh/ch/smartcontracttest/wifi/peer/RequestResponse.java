package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

/**
 * Created by flo on 18.06.17.
 */

public class RequestResponse {

    private boolean accepted;

    public RequestResponse(boolean accepted)
    {

        this.accepted = accepted;
    }

    public boolean isAccepted() {
        return accepted;
    }
}
