package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

import java.io.IOException;
import java.net.InetAddress;

import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;

/**
 * Created by flo on 18.06.17.
 */

public interface TradingClient
{
    void setHost(String address);
    String getHost();
    void setPort(int port);

    void waitConnectionAvailable();
    void sendProfile(final UserProfile userProfile) throws IOException;
    void sendConnectionRequest(final ConnectionRequest request) throws IOException;
    void sendConnectionConfiguration(final ConnectionConfig config) throws IOException;
    void sendContract(final ContractInfo contractInfo) throws IOException;
    void sendRequestResponse(final RequestResponse requestResponse) throws IOException;
    void sendTransmissionConfirmed(TransmissionConfirmedResponse response) throws IOException;
    void sendFile(String absolutPath) throws IOException;
}
