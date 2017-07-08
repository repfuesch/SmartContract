package ch.uzh.ifi.csg.contract.service.exchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 * Created by flo on 07.04.17.
 */

public interface EthConvertService
{
    SimplePromise<BigDecimal> getExchangeRate(final Currency exchangeCurrency);
    SimplePromise<BigDecimal> convertToCurrency(final BigDecimal amountEther, final Currency exchangeCurrency);
    SimplePromise<BigDecimal> convertToEther(final BigDecimal amountCurrency, final Currency exchangeCurrency);
}
