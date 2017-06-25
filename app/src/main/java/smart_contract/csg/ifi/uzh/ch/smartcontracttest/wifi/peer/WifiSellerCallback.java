package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer;

import java.util.List;

import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service.ContractInfoListener;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.service.UserProfileListener;

/**
 * Created by flo on 17.06.17.
 */

public interface WifiSellerCallback extends WifiCallback
{
    void onUserProfileRequested(UserProfileListener listener);
    void onContractInfoRequested(ContractInfoListener listener);
    void onUserProfileReceived(UserProfile data);
    void onPeersChanged(List<String> deviceNames);
    void onTransmissionConfirmed();
}
