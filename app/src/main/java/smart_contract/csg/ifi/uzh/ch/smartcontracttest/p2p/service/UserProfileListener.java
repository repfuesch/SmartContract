package smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service;

import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.p2p.peer.BuyerPeer;
import ch.uzh.ifi.csg.contract.p2p.peer.P2pBuyerCallback;

/**
 * Callback interface to handle the result of a UserProfile request.
 *
 * see {@link P2pBuyerCallback#onUserProfileRequested(UserProfileListener)}
 * see {@link BuyerPeer#awaitContractInfo()}
 */
public interface UserProfileListener {
    void onUserProfileReceived(UserProfile profile);
}
