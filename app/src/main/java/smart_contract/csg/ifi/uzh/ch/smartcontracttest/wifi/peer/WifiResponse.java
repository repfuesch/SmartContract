package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

/**
 * Created by flo on 17.06.17.
 */

public class WifiResponse
{
    private boolean success;
    private String reasonPhrase;
    private Throwable reason;

    public WifiResponse(boolean success, Throwable reason, String reasonPhrase)
    {
        this.reasonPhrase = reasonPhrase;
        this.success = success;
        this.reason = reason;
    }

    public boolean isSuccessful(){
        return success;
    }

    public String getReasonPhrase() {return reasonPhrase;}

    public Throwable getError()
    {
        return reason;
    }
}


