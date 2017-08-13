package ch.uzh.ifi.csg.smartcontract.library.p2p.peer;

import java.io.IOException;

import ch.uzh.ifi.csg.smartcontract.library.datamodel.ContractInfo;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.UserProfile;

/**
 * Interface to send Profile- and Contract data
 */
public interface P2pClient
{
    void sendProfile(final UserProfile userProfile) throws IOException;
    void sendContract(final ContractInfo contractInfo) throws IOException;
}
