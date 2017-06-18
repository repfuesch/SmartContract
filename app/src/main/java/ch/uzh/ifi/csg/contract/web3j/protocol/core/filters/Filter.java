package ch.uzh.ifi.csg.contract.web3j.protocol.core.filters;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.filters.Callback;
import org.web3j.protocol.core.filters.FilterException;
import org.web3j.protocol.core.methods.response.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthUninstallFilter;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Custom Filter implementation using the ScheduledExecutorService for the polling task
 * instead of while(true) loop and Thread.sleep().
 */

public abstract class Filter<T>
{
    protected final Web3j web3j;
    protected final Callback<T> callback;

    private final ScheduledExecutorService executorService;
    private ScheduledFuture<?> future;

    private volatile BigInteger filterId;

    public Filter(Web3j web3j, Callback<T> callback, ScheduledExecutorService executorService) {
        this.web3j = web3j;
        this.callback = callback;
        this.executorService = executorService;
    }

    /**
     * We don't block the thread in this method. Instead we use the ScheduledExecutorService to
     * periodically execute the polling task.
     */
    public void run(long blockTime) {
        try {
            final EthFilter ethFilter = sendRequest();
            if (ethFilter.hasError()) {
                throwException(ethFilter.getError());
            }

            filterId = ethFilter.getFilterId();

            future = executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        EthLog ethLog = web3j.ethGetFilterChanges(filterId).sendAsync().get();
                        if (ethLog.hasError()) {
                            throwException(ethFilter.getError());
                        }

                        process(ethLog.getLogs());
                    }catch (InterruptedException e) {
                        throwException(e);
                    } catch (ExecutionException e) {
                        throwException(e);
                    }
                }
            }, 0, blockTime, TimeUnit.MILLISECONDS);

        } catch (InterruptedException e) {
            throwException(e);
        } catch (ExecutionException e) {
            throwException(e);
        }
    }

    abstract EthFilter sendRequest() throws ExecutionException, InterruptedException;

    abstract void process(List<EthLog.LogResult> logResults);

    /**
     * We cancel the polling task by canceling the ScheduledFuture object
     */
    public void cancel() {

        if(future == null)
            return;

        future.cancel(false);

        EthUninstallFilter ethUninstallFilter = null;
        try {
            ethUninstallFilter = web3j.ethUninstallFilter(filterId).sendAsync().get();
        } catch (InterruptedException e) {
            throwException(e);
        } catch (ExecutionException e) {
            throwException(e);
        }

        if (ethUninstallFilter.hasError()) {
            throwException(ethUninstallFilter.getError());
        }

        if (!ethUninstallFilter.isUninstalled()) {
            throwException(ethUninstallFilter.getError());
        }
    }

    void throwException(Response.Error error) {
        throw new FilterException("Invalid request: " + error.getMessage());
    }

    void throwException(Throwable cause) {
        throw new FilterException("Error sending request", cause);
    }
}
