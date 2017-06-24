package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service.UserProfileListener;

/**
 * Created by flo on 16.06.17.
 */

public interface WifiBuyerCallback extends WifiCallback
{
    void onUserProfileRequested(UserProfileListener listener);
    void onUserProfileReceived(UserProfile data);
    void onContractInfoReceived(ContractInfo contractInfo);
}
