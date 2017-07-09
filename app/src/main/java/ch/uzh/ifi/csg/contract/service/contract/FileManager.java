package ch.uzh.ifi.csg.contract.service.contract;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.csg.contract.common.FileUtil;
import ch.uzh.ifi.csg.contract.datamodel.Account;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.account.AccountManager;
import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import ch.uzh.ifi.csg.contract.service.serialization.SerializationService;

/**
 * Class for loading, saving and deleting contract data for an account from a JSON file
 */

public class FileManager implements ContractManager, AccountManager {

    private String accountDirectory;
    private Map<String, Account> accountMap;
    private SerializationService serializationService;

    public FileManager(String dataDirectory)
    {
        this.accountDirectory = dataDirectory;
        this.accountMap = new HashMap<>();
        this.serializationService = new GsonSerializationService();
        load();
    }

    private void load()
    {
        try{
            String accountData = FileUtil.readFile(new File(accountDirectory));
            accountMap = Deserialize(accountData);
            if(accountMap == null)
                accountMap = new HashMap<>();
        }catch(IOException e)
        {
            //todo:log
            e.printStackTrace();
        }
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
        String data = serializationService.serialize(accountMap);
        try {
            FileUtil.writeFile(data, new File(accountDirectory));
        } catch (IOException e) {
            //todo:log
            e.printStackTrace();
        }
    }

    @Override
    public void deleteContract(String contractAddress, String account)
    {
        ContractInfo toDelete = accountMap.get(account).getContracts().get(contractAddress);
        for(String path : toDelete.getImages().values())
        {
            new File(path).delete();
        }

        if(toDelete.getUserProfile().getProfileImagePath() != null)
            new File(toDelete.getUserProfile().getProfileImagePath()).delete();

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
        Map<String, Account> accountMap = serializationService.deserialize(jsonArray, mapType);
        if(mapType == null)
            return new HashMap<>();

        return accountMap;
    }
}
