package smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.service;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;

/**
 * Created by flo on 24.06.17.
 */

public interface ContractInfoListener {
    void onContractInfoReceived(ContractInfo contractInfo);
}