package ch.uzh.ifi.csg.contract.service.exchange;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.message.BasicHeader;

/**
 * {@link EthConvertService} implementation that uses an online exchange service to retrieve
 * real-time ether exchange rates.
 */
public class JsonHttpConvertService implements EthConvertService
{
    private final CloseableHttpClient httpClient;
    private final Gson gson;
    private final String url;

    /**
     *
     * @param httpClient
     * @param url: A url pointing to an ether exchange service
     * @param deserializer: A {@link JsonDeserializer} used to deserilaize the results retrieved
     *                    from the exchange service
     */
    public JsonHttpConvertService(CloseableHttpClient httpClient, String url, JsonDeserializer<Map<Currency, BigDecimal>> deserializer)
    {
        this.url = url;
        this.httpClient = httpClient;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<HashMap<Currency, BigDecimal>>(){}.getType(), deserializer)
                .create();
    }

    private Header[] buildHeaders() {
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Content-Type", "application/json; charset=UTF-8"));
        return headers.toArray(new Header[0]);
    }

    private Map<Currency, BigDecimal> getEthExchangeRates() throws Exception
    {
        HttpGet request = new HttpGet(url);
        request.setHeaders(buildHeaders());

        try {
            HttpResponse response = httpClient.execute(request);

            ResponseHandler<Map<Currency, BigDecimal>> handler = new ResponseHandler<Map<Currency, BigDecimal>>() {
                @Override
                public Map<Currency, BigDecimal> handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    String responseData = readResponse(response);
                    Map<Currency, BigDecimal> result = gson.fromJson(responseData, new TypeToken<HashMap<Currency, BigDecimal>>(){}.getType());
                    return result;
                }
            };

            return handler.handleResponse(response);

        } finally {
            httpClient.close();
        }
    }

    private String readResponse(HttpResponse response) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 1000);
        String jsonString = "";
        while(reader.ready())
        {
            jsonString += reader.readLine();
        }
        return jsonString;
    }

    @Override
    public SimplePromise<BigDecimal> getExchangeRate(final Currency exchangeCurrency) {
        return Async.toPromise(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() throws Exception {
                Map<Currency, BigDecimal> exchangeMap = getEthExchangeRates();
                return exchangeMap.get(exchangeCurrency);
            }
        });
    }

    @Override
    public SimplePromise<BigDecimal> convertToCurrency(final BigDecimal amountEther, final Currency exchangeCurrency) {
        return Async.toPromise(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() throws Exception {
                Map<Currency, BigDecimal> exchangeMap = getEthExchangeRates();
                return amountEther.multiply(exchangeMap.get(exchangeCurrency));
            }
        });
    }

    @Override
    public SimplePromise<BigDecimal> convertToEther(final BigDecimal amountCurrency, final Currency exchangeCurrency) {
        return Async.toPromise(new Callable<BigDecimal>() {
            @Override
            public BigDecimal call() throws Exception {
                Map<Currency, BigDecimal> exchangeMap = getEthExchangeRates();
                return amountCurrency.divide(exchangeMap.get(exchangeCurrency), 18, BigDecimal.ROUND_HALF_EVEN);
            }
        });
    }
}
