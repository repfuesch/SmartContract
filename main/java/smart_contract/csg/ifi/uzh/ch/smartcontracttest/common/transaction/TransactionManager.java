package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 * Created by flo on 27.04.17.
 */

public interface TransactionManager
{
    <T> void toTransaction(SimplePromise<T> promise, final String contractAddress);
}
