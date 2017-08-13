package ch.uzh.ifi.csg.smartcontract.library.p2p.peer;

/**
 * Created by flo on 17.06.17.
 */

public class ConnectionConfig {

    private boolean useIdentifiaction;

    public ConnectionConfig(boolean useIdentifiaction)
    {
        this.useIdentifiaction = useIdentifiaction;
    }

    public boolean isIdentificationUsed()
    {
        return useIdentifiaction;
    }
}
