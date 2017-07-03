package ch.uzh.ifi.csg.contract.p2p.peer;

import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;

/**
 * Created by flo on 16.06.17.
 */

public interface P2pBuyerCallback extends P2pCallback
{
    void onContractInfoReceived(ContractInfo contractInfo);
}
