package ch.uzh.ifi.csg.contract.p2p.peer;

import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.UserProfileListener;

/**
 * Created by flo on 16.06.17.
 */

public interface P2pBuyerCallback extends P2pCallback
{
    void onContractInfoReceived(ContractInfo contractInfo);
    void onUserProfileRequested(UserProfileListener listener);
}
