package ch.uzh.ifi.csg.smartcontract.library.p2p.peer;

import ch.uzh.ifi.csg.smartcontract.library.datamodel.ContractInfo;
import ch.uzh.ifi.csg.smartcontract.app.p2p.dialog.P2pImportDialog;
import ch.uzh.ifi.csg.smartcontract.app.p2p.service.UserProfileListener;

/**
 * Buyer/import interface that is implemented by UI components that receive contract information over
 * a wireless channel.
 *
 * See {@link P2pImportDialog}
 * See {@link BuyerPeer}
 */
public interface P2pBuyerCallback extends P2pCallback
{
    /**
     * Callback method that is executed by the {@link BuyerPeer} after it received the contract info
     * from the seller.
     *
     * @param contractInfo: object that stores the contract details
     */
    void onContractInfoReceived(ContractInfo contractInfo);

    /**
     * Callback method executed by the {@link BuyerPeer} when the UserProfile of the unlocked
     * account is requested by the seller.
     *
     * @param listener: Callback invoked when the UserProfile is ready to be sent.
     */
    void onUserProfileRequested(UserProfileListener listener);
}
