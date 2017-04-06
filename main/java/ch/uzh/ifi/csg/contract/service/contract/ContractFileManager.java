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

import ch.uzh.ifi.csg.contract.common.FileManager;
import ezvcard.Ezvcard;
import ezvcard.VCard;

/**
 * Class for loading, saving and deleting contract data for an account in a JSON file
 */

public class ContractFileManager implements ContractManager {

    private Gson gson;
    private String contractDirectory;
    private Map<String, Map<String, ContractInfo>> contractMap;

    public ContractFileManager(String contractDirectory)
    {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(VCard.class, new VCardSerializer())
                .registerTypeAdapter(VCard.class, new VCardDeserializer())
                .create();

        this.contractDirectory = contractDirectory;
        this.contractMap = new HashMap<>();
        load();
    }

    private void load()
    {
        String accountData = FileManager.readFile(new File(contractDirectory));
        contractMap = Deserialize(accountData);
        if(contractMap == null)
            contractMap = new HashMap<>();
    }

    private void ensureAccount(String account)
    {
        if(!contractMap.containsKey(account))
            contractMap.put(account, new HashMap<String, ContractInfo>());
    }

    @Override
    public void saveContract(ContractInfo contract, String account)
    {
        ensureAccount(account);
        contractMap.get(account).put(contract.getContractAddress(), contract);
        save();
        /*
        String accountData = FileManager.readFile(new File(contractDirectory));
        List<AccountContractInfo> accounts = Deserialize(accountData);

        for(AccountContractInfo info : accounts)
        {
            if(info.getAccountId().equals(account))
            {
                for(ContractInfo contractInfo : info.getContractInfo())
                {
                    if(contractInfo.getContractAddress().equals(contract))
                    {
                        contractInfo = contract;
                        accountData = Serialize(accounts);
                        FileManager.writeFile(accountData, new File(contractDirectory));
                        return;
                    }

                }
                info.getContractInfo().add(contract);
                break;
            }
        }
*/

    }

    private void save()
    {
        String data = Serialize(contractMap);
        FileManager.writeFile(data, new File(contractDirectory));
    }

    @Override
    public void deleteContract(ContractInfo contract, String account)
    {
        ensureAccount(account);
        contractMap.get(account).remove(contract.getContractAddress());
        save();
        /*
        String accountData = FileManager.readFile(new File(contractDirectory));
        List<AccountContractInfo> accounts = Deserialize(accountData);

        ContractInfo toDelete = null;
        for(AccountContractInfo info : accounts)
        {
            if(info.getAccountId().equals(account))
            {
                for(ContractInfo contractInfo : info.getContractInfo())
                {
                    if(contractInfo.getContractAddress().equals(contract.getContractAddress()))
                    {
                        toDelete = contractInfo;
                        break;
                    }
                }
                if(toDelete != null)
                {
                    info.getContractInfo().remove(toDelete);
                    break;
                }
            }
        }

        accountData = Serialize(accounts);
        FileManager.writeFile(accountData, new File(contractDirectory));
        */
    }

    @Override
    public List<ContractInfo> getContracts(String account)
    {
        ensureAccount(account);
        return new ArrayList<>(contractMap.get(account).values());
        /*
        String accountData = FileManager.readFile(new File(contractDirectory));
        List<AccountContractInfo> accounts = Deserialize(accountData);
        for(AccountContractInfo info : accounts)
        {
            if(info.getAccountId().equals(account))
                return info.getContractInfo();
        }

        //create accountInfo for account if it does not exist
        AccountContractInfo info = new AccountContractInfo(account);
        accounts.add(info);
        accountData = Serialize(accounts);
        FileManager.writeFile(accountData, new File(contractDirectory));

        return new ArrayList<>();
        */
    }

    @Override
    public ContractInfo getContract(String address, String account)
    {
        ensureAccount(account);
        if(contractMap.get(account).containsKey(address))
            return contractMap.get(account).get(address);
        return null;

        /*
        List<ContractInfo> contracts = getContracts(account);
        for(ContractInfo info : contracts)
        {
            if(info.getContractAddress().equals(address))
                return info;
        }

        return null;
        */
    }

    private Map<String, Map<String, ContractInfo>> Deserialize(String jsonArray)
    {
        Type mapType = new TypeToken<HashMap<String, HashMap<String, ContractInfo>>>(){}.getType();
        Map<String, Map<String, ContractInfo>> contractMap = gson.fromJson(jsonArray, mapType);
        if(mapType == null)
            return new HashMap<>();

        return contractMap;
    }

    private String Serialize(Map<String, Map<String, ContractInfo>> accountInfo)
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
}
