package smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.p2p.peer.P2pSellerCallback;
import ch.uzh.ifi.csg.contract.p2p.peer.SellerPeer;

/**
 * Callback interface to handle the result of a ContractInfo request. Invoked by
 * the {@link P2pSellerCallback} implementation when the ContractInfo data is ready to be sent over
 * the network.
 *
 * see {@link P2pSellerCallback#onContractInfoRequested(ContractInfoListener)}
 * see {@link SellerPeer}
 */
public interface ContractInfoListener {
    void onContractInfoReceived(ContractInfo contractInfo);
}
