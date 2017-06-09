package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;

/**
 * Created by flo on 27.04.17.
 */

public interface TransactionManager
{
    public static final String ACTION_HANDLE_TRANSACTION = "ch.uzh.ifi.csg.smart_contract.transaction";
    public static final String CONTRACT_TRANSACTION_TYPE = "ch.uzh.ifi.csg.smart_contract.contract_action";
    public static final String CONTRACT_TRANSACTION_DEPLOY = "ch.uzh.ifi.csg.smart_contract.contract_deploy";
    public static final String CONTRACT_TRANSACTION_UPDATE = "ch.uzh.ifi.csg.smart_contract.contract_update";
    public static final String CONTRACT_ADDRESS = "ch.uzh.ifi.csg.smart_contract.contract_address";
    public static final String CONTRACT_TYPE = "ch.uzh.ifi.csg.smart_contract.contract_type";

    <T> void toTransaction(SimplePromise<T> promise, final String contractAddress);
    void toTransaction(SimplePromise<ITradeContract> promise);
}
