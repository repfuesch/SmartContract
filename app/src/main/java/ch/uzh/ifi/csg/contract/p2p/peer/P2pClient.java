package ch.uzh.ifi.csg.contract.p2p.peer;

import java.io.IOException;

import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;

/**
 * Created by flo on 18.06.17.
 */

public interface P2pClient
{
    void sendProfile(final UserProfile userProfile) throws IOException;
    void sendConnectionConfiguration(final ConnectionConfig config) throws IOException;
    void sendContract(final ContractInfo contractInfo) throws IOException;
    void sendTransmissionConfirmed(TransmissionConfirmedResponse response) throws IOException;
}
