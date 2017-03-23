package ch.uzh.ifi.csg.contract.web3j.protocol.core.filters;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.filters.Callback;
import org.web3j.protocol.core.filters.FilterException;
import org.web3j.protocol.core.methods.response.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by flo on 23.03.17.
 */

public class LogFilter extends Filter<Log> {

    private final org.web3j.protocol.core.methods.request.EthFilter ethFilter;

    public LogFilter(
            Web3j web3j, Callback<Log> callback,
            org.web3j.protocol.core.methods.request.EthFilter ethFilter,
            ScheduledExecutorService executorService) {
        super(web3j, callback, executorService);

        this.ethFilter = ethFilter;
    }

    @Override
    EthFilter sendRequest() throws ExecutionException, InterruptedException {
        return web3j.ethNewFilter(ethFilter).sendAsync().get();
    }

    @Override
    void process(List<EthLog.LogResult> logResults) {
        for (EthLog.LogResult logResult : logResults) {
            if (logResult instanceof EthLog.LogObject) {
                Log log = ((EthLog.LogObject) logResult).get();
                callback.onEvent(log);
            } else {
                throw new FilterException(
                        "Unexpected result type: " + logResult.get() + " required LogObject");
            }
        }
    }
}
