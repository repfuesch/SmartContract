package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service;

import ch.uzh.ifi.csg.contract.datamodel.UserProfile;

/**
 * Created by flo on 24.06.17.
 */

public interface UserProfileListener {
    void onUserProfileReceived(UserProfile profile);
}