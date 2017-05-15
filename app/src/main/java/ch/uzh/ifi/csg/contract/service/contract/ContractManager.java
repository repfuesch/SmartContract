package ch.uzh.ifi.csg.contract.service.contract;

import java.util.List;

import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;

/**
 * Created by flo on 06.03.17.
 */

public interface ContractManager
{
    void saveContract(ContractInfo contract, String account);
    void deleteContract(ContractInfo contract, String account);
    List<ContractInfo> getContracts(String account);
    ContractInfo getContract(String address, String account);
}
