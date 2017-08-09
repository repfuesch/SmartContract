package ch.uzh.ifi.csg.contract.service.exchange;

import java.math.BigDecimal;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 * Service interface to retrieve ether exchange rates for selected currencies and to directly
 * convert currencies to ether and vice versa
 */
public interface EthConvertService
{
    /**
     * Returns the exchange rate for the provided currency
     *
     * @param exchangeCurrency
     * @return
     */
    SimplePromise<BigDecimal> getExchangeRate(final Currency exchangeCurrency);

    /**
     * Converts the amount of ether into the provided currency
     *
     * @param amountEther
     * @param exchangeCurrency
     * @return
     */
    SimplePromise<BigDecimal> convertToCurrency(final BigDecimal amountEther, final Currency exchangeCurrency);

    /**
     * Converts the amount of currency into ether
     *
     * @param amountCurrency
     * @param exchangeCurrency
     * @return
     */
    SimplePromise<BigDecimal> convertToEther(final BigDecimal amountCurrency, final Currency exchangeCurrency);
}
