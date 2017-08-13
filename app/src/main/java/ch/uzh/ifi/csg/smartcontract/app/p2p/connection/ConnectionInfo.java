package ch.uzh.ifi.csg.smartcontract.app.p2p.connection;


import ch.uzh.ifi.csg.smartcontract.library.p2p.peer.Peer;

/**
 * Class that contains information about the role of a {@link Peer} in a Wi-Fi direct group
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
