package ch.uzh.ifi.csg.contract.p2p.peer;


import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service.UserProfileListener;

/**
 * Created by flo on 18.06.17.
 */

public interface P2pCallback {
    void onP2pInfoMessage(String message);
    void onP2pErrorMessage(String message);
    void onUserProfileReceived(UserProfile data);
    void onUserProfileRequested(UserProfileListener listener);
}
