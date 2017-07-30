package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;

/**
 * Created by flo on 27.04.17.
 */

public interface TransactionHandler
{
    public static final String ACTION_TRANSACTION_CREATED = "ch.uzh.ifi.csg.smart_contract.transaction.create";
    public static final String ACTION_TRANSACTION_UPDATED = "ch.uzh.ifi.csg.smart_contract.transaction.update";
    public static final String CONTRACT_ADDRESS = "ch.uzh.ifi.csg.smart_contract.contract_address";
    public static final String CONTRACT_TYPE = "ch.uzh.ifi.csg.smart_contract.contract_type";

    <T> void toTransaction(SimplePromise<T> promise, final String contractAddress);
    void toDeployTransaction(SimplePromise<ITradeContract> promise, ContractInfo contractInfo, String account, ContractService contractService);
    boolean hasOpenTransactions();
}
