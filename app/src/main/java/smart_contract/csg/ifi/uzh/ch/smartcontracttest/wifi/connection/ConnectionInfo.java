package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.connection;

/**
 * Created by flo on 23.06.17.
 */

public class ConnectionInfo
{
    String groupOwnerAddress;
    boolean isGroupOwner;

    public ConnectionInfo(String groupOwnerAddress, int groupOwnerPort, boolean isGroupOwner)
    {
        this.groupOwnerAddress = groupOwnerAddress;
        this.groupOwnerPort = groupOwnerPort;
        this.isGroupOwner = isGroupOwner;
    }

    public ConnectionInfo(String groupOwnerAddress)
    {
        this.groupOwnerAddress = groupOwnerAddress;
    }

    public String getGroupOwnerAddress() {
        return groupOwnerAddress;
    }

    public int getGroupOwnerPort() {
        return groupOwnerPort;
    }

    public boolean isGroupOwner() {
        return isGroupOwner;
    }

    public void setGroupOwnerPort(int groupOwnerPort) {
        this.groupOwnerPort = groupOwnerPort;
    }

    int groupOwnerPort;

    public void setGroupOwner(boolean groupOwner) {
        isGroupOwner = groupOwner;
    }


}
