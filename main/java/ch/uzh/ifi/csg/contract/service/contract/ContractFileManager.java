package ch.uzh.ifi.csg.contract.service.contract;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.csg.contract.common.FileManager;

/**
 * Class for loading, saving and deleting contract data for an account in a JSON file
 */

public class ContractFileManager implements ContractManager {

    private Gson gson;
    private String contractDirectory;

    public ContractFileManager(String contractDirectory)
    {
        this.gson = new GsonBuilder().create();
        this.contractDirectory = contractDirectory;
    }

    @Override
    public void saveContract(ContractInfo contract, String account)
    {
        String accountData = FileManager.readFile(new File(contractDirectory));
        List<AccountContractInfo> accounts = Deserialize(accountData);

        for(AccountContractInfo info : accounts)
        {
            if(info.getAccountId().equals(account))
            {
                info.getContractInfo().add(contract);
                break;
            }
        }

        accountData = Serialize(accounts);
        FileManager.writeFile(accountData, new File(contractDirectory));
    }

    @Override
    public void deleteContract(ContractInfo contract, String account)
    {
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
    }

    @Override
    public List<ContractInfo> getContracts(String account)
    {
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
    }

    @Override
    public ContractInfo getContract(String address, String account)
    {
        List<ContractInfo> contracts = getContracts(account);
        for(ContractInfo info : contracts)
        {
            if(info.getContractAddress().equals(address))
                return info;
        }

        return null;
    }

    private List<AccountContractInfo> Deserialize(String jsonArray)
    {
        Type listType = new TypeToken<ArrayList<AccountContractInfo>>(){}.getType();
        List<AccountContractInfo> accountList = gson.fromJson(jsonArray, listType);
        if(accountList == null)
            return new ArrayList<>();

        return accountList;
    }

    private String Serialize(List<AccountContractInfo> accountInfo)
    {
        return gson.toJson(accountInfo);
    }
}
