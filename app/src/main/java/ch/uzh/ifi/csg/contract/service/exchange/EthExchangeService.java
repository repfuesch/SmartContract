package ch.uzh.ifi.csg.contract.service.exchange;

import java.io.IOException;
import java.util.Map;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 * Created by flo on 07.04.17.
 */

public interface EthExchangeService
{
    SimplePromise<Map<Currency, Float>> getEthExchangeRatesAsync();
    Map<Currency, Float> getEthExchangeRates() throws Exception;
}
