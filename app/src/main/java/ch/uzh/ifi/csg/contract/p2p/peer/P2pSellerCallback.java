package ch.uzh.ifi.csg.contract.p2p.peer;

import java.util.List;

import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import smart_contract.csg.ifi.uzh.ch.smartcontract.p2p.dialog.P2pExportDialog;
import smart_contract.csg.ifi.uzh.ch.smartcontract.p2p.service.ContractInfoListener;
import smart_contract.csg.ifi.uzh.ch.smartcontract.p2p.service.P2PSellerService;
import smart_contract.csg.ifi.uzh.ch.smartcontract.p2p.service.P2PSellerServiceImpl;

/**
 * Seller/export interface that is implemented by UI components that want to send contract
 * information over the network.
 *
 * See {@link P2pExportDialog}
 * See {@link SellerPeer}
 */
public interface P2pSellerCallback extends P2pCallback
{
    /**
     * Invoked by the {@link SellerPeer} when it is ready to send the contract
     *
     * @param listener: callback to receive the contract details
     */
    void onContractInfoRequested(ContractInfoListener listener);

    /**
     * Invoked by the {@link SellerPeer} when it received the profile from the buyer
     *
     * @param profile: The received UserProfile object
     */
    void onUserProfileReceived(UserProfile profile);

    /**
     * Invoked by the {@link P2PSellerService} implementation when the available devices in the
     * environment have changed.
     *
     * See {@link P2PSellerServiceImpl#onPeersChanged(List)}
     *
     * @param deviceNames: A list of device names
     */
    void onPeersChanged(List<String> deviceNames);
}
