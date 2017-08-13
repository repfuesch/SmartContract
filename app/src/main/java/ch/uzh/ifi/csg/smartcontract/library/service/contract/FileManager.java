package ch.uzh.ifi.csg.smartcontract.library.service.contract;

import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.csg.smartcontract.library.util.FileUtil;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.Account;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.ContractInfo;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.UserProfile;
import ch.uzh.ifi.csg.smartcontract.library.service.account.AccountManager;
import ch.uzh.ifi.csg.smartcontract.library.service.serialization.GsonSerializationService;
import ch.uzh.ifi.csg.smartcontract.library.service.serialization.SerializationService;

/**
 * Class that implements the {@link ContractManager} and {@link AccountManager} interface.
 *
 * It stores {@link Account} and {@link ContractInfo} objects in a map and it uses the
 * {@link GsonSerializationService} to serialize and deserialize them. The {@link FileUtil} is used
 * to persist the data on the file system.
 */

public class FileManager implements ContractManager, AccountManager {

    private String accountDirectory;
    private Map<String, Account> accountMap;
    private SerializationService serializationService;
    private File accountFile;

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
            accountFile = new File(accountDirectory + File.separator + "accounts");
            if(!accountFile.exists())
                accountFile.createNewFile();

            String accountData = FileUtil.readFile(accountFile);
            accountMap = Deserialize(accountData);
            if(accountMap == null)
                accountMap = new HashMap<>();
        }catch(IOException e)
        {
            Log.e("service", "failed to load data from the file system", e);
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
            FileUtil.writeFile(data, accountFile);
        } catch (IOException e) {
            Log.e("service", "failed to save data on the file system", e);
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

        if(toDelete.getUserProfile() != null && toDelete.getUserProfile().getProfileImagePath() != null)
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
