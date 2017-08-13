package ch.uzh.ifi.csg.smartcontract.library.service.contract;

import java.util.List;

import ch.uzh.ifi.csg.smartcontract.library.datamodel.ContractInfo;

/**
 * Interface to manage contracts on the file system
 */
public interface ContractManager
{
    /**
     * Stores the provided contract for the referenced accountId
     *
     * @param contract: the contract to store
     * @param accountId: the referenced account
     */
    void saveContract(ContractInfo contract, String accountId);
    /**
     * Deletes a contract referenced by its address that belongs to the referenced account. Note
     * that this method does only remove the contract from the files system and not from the block-
     * chain!
     *
     * @param contractAddress: the contract to delete
     * @param accountId: the referenced account
     */
    void deleteContract(String contractAddress, String accountId);

    /**
     * Returns a list of of contracts belonging to the referenced account
     *
     * @param accountId: the referenced account
     * @return
     */
    List<ContractInfo> getContracts(String accountId);

    /**
     * Returns a specific contract by its address and account
     *
     * @param address: the address of the contract
     * @param accountId: the account that owns the contract
     * @return
     */
    ContractInfo getContract(String address, String accountId);
}
