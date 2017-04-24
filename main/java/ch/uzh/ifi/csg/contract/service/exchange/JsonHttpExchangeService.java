package ch.uzh.ifi.csg.contract.service.exchange;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
 * Created by flo on 07.04.17.
 */

public class JsonHttpExchangeService implements EthExchangeService
{
    private final CloseableHttpClient httpClient;
    private final Gson gson;
    private final String url;

    public JsonHttpExchangeService(CloseableHttpClient httpClient, String url, JsonDeserializer<Map<Currency, Float>> deserializer)
    {
        this.url = url;
        this.httpClient = httpClient;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<HashMap<Currency, Float>>(){}.getType(), deserializer)
                .create();
    }

    private Header[] buildHeaders() {
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Content-Type", "application/json; charset=UTF-8"));
        return headers.toArray(new Header[0]);
    }

    @Override
    public SimplePromise<Map<Currency, Float>> getEthExchangeRates()
    {
        return Async.toPromise(new Callable<Map<Currency, Float>>() {
            @Override
            public Map<Currency, Float> call() throws Exception
            {
                HttpGet request = new HttpGet(url);
                request.setHeaders(buildHeaders());

                try {
                    return httpClient.execute(request, new ResponseHandler<Map<Currency, Float>>() {
                        @Override
                        public Map<Currency, Float> handleResponse(HttpResponse response) throws IOException {
                            String responseData = readResponse(response);
                            Map<Currency, Float> result = gson.fromJson(responseData, new TypeToken<HashMap<Currency, Float>>(){}.getType());
                            return result;
                        }
                    });

                } catch(ClientProtocolException ex)
                {
                    return null;
                } catch(IOException ex)
                {
                    return null;
                } finally {
                    httpClient.close();
                }
            }
        });
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
}
