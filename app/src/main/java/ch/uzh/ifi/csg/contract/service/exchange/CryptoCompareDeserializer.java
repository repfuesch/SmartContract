package ch.uzh.ifi.csg.contract.service.exchange;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by flo on 10.04.17.
 */

public class CryptoCompareDeserializer implements JsonDeserializer<Map<Currency, BigDecimal>>
{
        @Override
        public Map<Currency, BigDecimal> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            Map<Currency, BigDecimal> exchangeMap = new HashMap<>();
            JsonObject object = json.getAsJsonObject();
            JsonElement euroElement = object.get("EUR");
            if(euroElement != null)
                exchangeMap.put(Currency.EUR, euroElement.getAsBigDecimal());
            JsonElement dollarElement = object.get("USD");
            if(dollarElement != null)
                exchangeMap.put(Currency.USD, dollarElement.getAsBigDecimal());

            return exchangeMap;
        }
}
