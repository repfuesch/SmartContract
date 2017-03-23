package ch.uzh.ifi.csg.contract.web3j.protocol;

import org.web3j.protocol.Web3jService;
import org.web3j.protocol.parity.Parity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by flo on 23.03.17.
 */

public class ParityClientFactory {

    public static Parity build(Web3jService web3jService) {
        return build(web3jService, 15000, Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    public static Parity build(Web3jService web3jService, long pollingInterval, ScheduledExecutorService executorService) {
        return new ParityClientImpl(web3jService, pollingInterval, executorService);
    }
}
