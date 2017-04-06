package ch.uzh.ifi.csg.contract.service.account;

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

import org.web3j.crypto.Credentials;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import ch.uzh.ifi.csg.contract.common.FileManager;
import ch.uzh.ifi.csg.contract.service.contract.ContractFileManager;
import ezvcard.Ezvcard;
import ezvcard.VCard;

/**
 * Class for loading and saving accounts in a JSON file
 */

public class AccountFileManager implements AccountManager {

    private Gson gson;
    private String accountDir;
    private List<Account> accounts;

    public AccountFileManager(String accountDir)
    {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(VCard.class, new VCardSerializer())
                .registerTypeAdapter(VCard.class, new VCardDeserializer())
                .create();

        this.accountDir = accountDir;
        this.accounts = loadAccounts();
    }

    private List<Account> loadAccounts()
    {
        String accountData = FileManager.readFile(new File(accountDir));
        List<Account> accounts = deserialize(accountData);
        return accounts;
    }

    @Override
    public List<Account> getAccounts()
    {
        return accounts;
    }

    @Override
    public void save()
    {
        String accountData = serialize(accounts);
        FileManager.writeFile(accountData, new File(accountDir));
    }

    private List<Account> deserialize(String jsonArray)
    {
        Type listType = new TypeToken<ArrayList<Account>>(){}.getType();
        List<Account> accountList = gson.fromJson(jsonArray, listType);
        if(accountList == null)
            return new ArrayList<>();

        return accountList;
    }

    private String serialize(List<Account> accounts)
    {
        return gson.toJson(accounts);
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
}
