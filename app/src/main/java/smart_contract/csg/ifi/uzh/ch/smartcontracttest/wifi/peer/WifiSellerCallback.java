package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;

/**
 * Created by flo on 17.06.17.
 */

public interface WifiSellerCallback extends WifiCallback
{
    UserProfile getUserProfile();
    ContractInfo getContractInfo();
    void onUserProfileReceived(UserProfile data);
}
