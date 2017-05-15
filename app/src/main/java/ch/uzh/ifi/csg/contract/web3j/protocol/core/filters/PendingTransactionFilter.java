package ch.uzh.ifi.csg.contract.web3j.protocol.core.filters;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.filters.*;
import org.web3j.protocol.core.methods.response.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Handler for working with transaction filter requests.
 */
public class PendingTransactionFilter extends Filter<String> {

    public PendingTransactionFilter(Web3j web3j, Callback<String> callback, ScheduledExecutorService executorService) {
        super(web3j, callback, executorService);
    }

    @Override
    EthFilter sendRequest() throws ExecutionException, InterruptedException {
        return web3j.ethNewPendingTransactionFilter().sendAsync().get();
    }

    @Override
    void process(List<EthLog.LogResult> logResults) {
        for (EthLog.LogResult logResult : logResults) {
            if (logResult instanceof EthLog.Hash) {
                String blockHash = ((EthLog.Hash) logResult).get();
                callback.onEvent(blockHash);
            } else {
                throw new FilterException(
                        "Unexpected result type: " + logResult.get() + ", required Hash");
            }
        }
    }
}
