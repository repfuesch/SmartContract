package ch.uzh.ifi.csg.contract.service.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ezvcard.Ezvcard;
import ezvcard.VCard;

/**
 * Created by flo on 16.06.17.
 */

public class GsonSerializationService implements SerializationService
{
    private Gson gson;

    public GsonSerializationService()
    {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(VCard.class, new VCardSerializer())
                .registerTypeAdapter(VCard.class, new VCardDeserializer())
                .registerTypeAdapter(new TypeToken<HashMap<String, ContractInfo>>(){}.getType(), new ContractMapDeserializer())
                .registerTypeAdapter(new TypeToken<HashMap<String, String>>(){}.getType(), new StringMapDeserializer())
                .create();
    }

    @Override
    public String serialize(Object data) {
        return gson.toJson(data);
    }

    @Override
    public <T> T deserialize(String data, Type type) {
        return gson.fromJson(data, type);
    }

    static class VCardSerializer implements JsonSerializer<VCard> {
        @Override
        public JsonElement serialize(VCard card, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(card.writeJson());
        }
    }

    static class VCardDeserializer implements JsonDeserializer<VCard> {

        @Override
        public VCard deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Ezvcard.parseJson(json.getAsString()).first();
        }
    }

    static class ContractMapDeserializer implements JsonDeserializer<Map<String, ContractInfo>> {

        @Override
        public Map<String, ContractInfo> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            Type mapType = new TypeToken<HashMap<String, ContractInfo>>(){}.getType();
            Map<String, ContractInfo> contractMap = context.deserialize(json, mapType);
            return contractMap;
        }
    }

    static class StringMapDeserializer implements JsonDeserializer<Map<String, String>> {

        @Override
        public Map<String, String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            Type mapType = new TypeToken<LinkedHashMap<String, String>>(){}.getType();
            Map<String, String> stringMap = context.deserialize(json, mapType);
            return stringMap;
        }
    }
}
