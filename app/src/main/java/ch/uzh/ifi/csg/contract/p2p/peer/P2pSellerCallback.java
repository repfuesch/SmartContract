package ch.uzh.ifi.csg.contract.p2p.peer;

import java.util.List;

import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.ContractInfoListener;

/**
 * Created by flo on 17.06.17.
 */

public interface P2pSellerCallback extends P2pCallback
{
    void onContractInfoRequested(ContractInfoListener listener);
    void onUserProfileReceived(UserProfile data);
    void onPeersChanged(List<String> deviceNames);
}
