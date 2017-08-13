package smart_contract.csg.ifi.uzh.ch.smartcontract;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import ch.uzh.ifi.csg.contract.service.exchange.CryptoCompareDeserializer;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import ch.uzh.ifi.csg.contract.service.exchange.JsonHttpConvertService;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpUriRequest;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit-Tests for the {@link JsonHttpConvertService} class
 */
@RunWith(MockitoJUnitRunner.class)
public class EthConvertServiceTest {

    private static final String RESPONSE_CONTENT = "{\"USD\":220.3,\"EUR\":195.15}";

    @Mock
    private CloseableHttpClient httpClient;

    private JsonHttpConvertService testee;

    /**
     * Sets up a fake HTTP response that contains the {@link #RESPONSE_CONTENT} JSON string
     */
    @Before
    public void setup() throws IOException
    {
        //instantiate testee
        testee = new JsonHttpConvertService(httpClient, "https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=USD,EUR", new CryptoCompareDeserializer());

        //fake HTTP respone
        CloseableHttpResponse fakeResponse= mock(CloseableHttpResponse.class);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(fakeResponse);
        HttpEntity fakeEntity = mock(HttpEntity.class);
        when(fakeResponse.getEntity()).thenReturn(fakeEntity);
        when(fakeEntity.getContent()).thenReturn(new ByteArrayInputStream(RESPONSE_CONTENT.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Checks that the correct exchange rate is returned for a given currency
     */
    @Test
    public void getExchangeRate_WhenCalled_ThenReturnsCorrectRateForCurrency()
    {
        //act
        BigDecimal rate = testee.getExchangeRate(Currency.USD).get();

        //assert
        assertThat(rate, is(new BigDecimal("220.3")));
    }

    /**
     * Checks that the amount of ether is correctly converted to the specified currency
     */
    @Test
    public void convertToCurrency_WhenCalled_ThenReturnsCorrectValue()
    {
        //arrange
        BigDecimal amountEther = new BigDecimal("2.256874");

        //act
        BigDecimal amountCurrency = testee.convertToCurrency(amountEther, Currency.USD).get();

        //assert
        assertThat(amountCurrency, is(new BigDecimal("220.3").multiply(amountEther)));
    }

    /**
     * Checks that the amount of currency is correctly converted to ether
     */
    @Test
    public void convertToEther_WhenCalled_ThenReturnsCorrectValue()
    {
        //arrange
        BigDecimal amountCurrency = new BigDecimal("500");

        //act
        BigDecimal amountEther = testee.convertToEther(amountCurrency, Currency.EUR).get();

        //assert
        BigDecimal expected = amountCurrency.divide(new BigDecimal("195.15"), 18, BigDecimal.ROUND_HALF_EVEN);
        assertThat(amountEther, is(expected));
    }
}
