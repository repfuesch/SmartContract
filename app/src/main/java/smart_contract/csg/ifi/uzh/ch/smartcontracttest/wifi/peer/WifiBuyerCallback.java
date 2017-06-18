package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;

/**
 * Created by flo on 16.06.17.
 */

public interface WifiBuyerCallback extends WifiCallback
{
    UserProfile getUserProfile();
    void onUserProfileReceived(UserProfile data);
    void onContractInfoReceived(ContractInfo contractInfo);
}
