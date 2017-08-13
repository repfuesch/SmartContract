package smart_contract.csg.ifi.uzh.ch.smartcontract.common.transaction;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;

/**
 * Inerface for handling the result of contract transactions beyond Activity boundaries.
 */
public interface TransactionHandler
{
    String ACTION_TRANSACTION_CREATED = "ch.uzh.ifi.csg.smart_contract.transaction.create";
    String ACTION_TRANSACTION_UPDATED = "ch.uzh.ifi.csg.smart_contract.transaction.update";
    String CONTRACT_ADDRESS = "ch.uzh.ifi.csg.smart_contract.contract_address";
    String CONTRACT_TYPE = "ch.uzh.ifi.csg.smart_contract.contract_type";

    /**
     * Handles the contract transaction specified by the promise object. Informs observers about
     * the success/error of the transaction when it has completed.
     *
     * @param promise
     * @param contractAddress
     * @param <T>
     */
    <T> void toTransaction(SimplePromise<T> promise, final String contractAddress);

    /**
     * Handles the contract deploy transaction specified by the promise that wraps the deployment
     * operation. Informs observers about the success/error of the transaction when it has completed
     * and guarantees that the contract details are saved in case of network errors.
     *
     * @param promise
     * @param contractInfo
     * @param account
     * @param contractService
     * @param <T>
     */
    <T extends ITradeContract> void toDeployTransaction(SimplePromise<T> promise, ContractInfo contractInfo, String account, ContractService contractService);
}
