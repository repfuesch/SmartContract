package ch.uzh.ifi.csg.contract.service.contract;

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

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.csg.contract.datamodel.Account;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.account.AccountManager;
import ezvcard.Ezvcard;
import ezvcard.VCard;

/**
 * Class for loading, saving and deleting contract data for an account from a JSON file
 */

public class FileManager implements ContractManager, AccountManager {

    private Gson gson;
    private String accountDirectory;
    private Map<String, Account> accountMap;

    public FileManager(String dataDirectory)
    {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(VCard.class, new VCardSerializer())
                .registerTypeAdapter(VCard.class, new VCardDeserializer())
                .registerTypeAdapter(new TypeToken<HashMap<String, ContractInfo>>(){}.getType(), new ContractMapDeserializer())
                .registerTypeAdapter(new TypeToken<HashMap<String, String>>(){}.getType(), new StringMapDeserializer())
                .create();

        this.accountDirectory = dataDirectory;
        this.accountMap = new HashMap<>();
        load();
    }

    private void load()
    {
        String accountData = ch.uzh.ifi.csg.contract.common.FileManager.readFile(new File(accountDirectory));
        accountMap = Deserialize(accountData);
        if(accountMap == null)
            accountMap = new HashMap<>();
    }

    @Override
    public void saveContract(ContractInfo contract, String account)
    {
        accountMap.get(account).getContracts().put(contract.getContractAddress(), contract);
        save();
    }

    @Override
    public List<Account> getAccounts() {
        return new ArrayList<>(accountMap.values());
    }

    @Override
    public void addAccount(Account account) {
        accountMap.put(account.getId(), account);
        save();
    }

    @Override
    public Account getAccount(String accountId) {
        return accountMap.get(accountId);
    }

    @Override
    public void saveAccountProfile(String accountId, UserProfile profile) {
        accountMap.get(accountId).setProfile(profile);
        save();
    }

    private void save()
    {
        String data = Serialize(accountMap);
        ch.uzh.ifi.csg.contract.common.FileManager.writeFile(data, new File(accountDirectory));
    }

    @Override
    public void deleteContract(String contractAddress, String account)
    {
        accountMap.get(account).getContracts().remove(contractAddress);
        save();
    }

    @Override
    public List<ContractInfo> getContracts(String account)
    {
        return new ArrayList<>(accountMap.get(account).getContracts().values());
    }

    @Override
    public ContractInfo getContract(String address, String account)
    {

        if(accountMap.get(account).getContracts().containsKey(address))
            return accountMap.get(account).getContracts().get(address);
        return null;

    }

    private Map<String, Account> Deserialize(String jsonArray)
    {
        Type mapType = new TypeToken<HashMap<String, Account>>(){}.getType();
        Map<String, Account> accountMap = gson.fromJson(jsonArray, mapType);
        if(mapType == null)
            return new HashMap<>();

        return accountMap;
    }

    private String Serialize(Map<String, Account> accountInfo)
    {
        return gson.toJson(accountInfo);
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
            Type mapType = new TypeToken<HashMap<String, String>>(){}.getType();
            Map<String, String> stringMap = context.deserialize(json, mapType);
            return stringMap;
        }
    }
}
