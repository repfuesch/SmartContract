package ch.uzh.ifi.csg.contract.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.csg.contract.common.FileManager;

/**
 * Created by flo on 06.03.17.
 */

public class ContractFileManager implements ContractManager {

    private Gson gson;
    private String accountDirectory;

    public ContractFileManager(String accountDirectory)
    {
        this.gson = new GsonBuilder().create();
        this.accountDirectory = accountDirectory;
    }

    @Override
    public void saveContract(ContractInfo contract, String account)
    {
        String accountData = FileManager.readFile(new File(accountDirectory));
        List<AccountInfo> accounts = Deserialize(accountData);

        for(AccountInfo info : accounts)
        {
            if(info.getAccountId().equals(account))
            {
                info.getContractInfo().add(contract);
                break;
            }
        }

        accountData = Serialize(accounts);
        FileManager.writeFile(accountData, new File(accountDirectory));
    }

    @Override
    public void deleteContract(ContractInfo contract, String account)
    {
        String accountData = FileManager.readFile(new File(accountDirectory));
        List<AccountInfo> accounts = Deserialize(accountData);

        ContractInfo toDelete = null;
        for(AccountInfo info : accounts)
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
        FileManager.writeFile(accountData, new File(accountDirectory));
    }

    @Override
    public List<ContractInfo> loadContracts(String account)
    {
        String accountData = FileManager.readFile(new File(accountDirectory));
        List<AccountInfo> accounts = Deserialize(accountData);
        for(AccountInfo info : accounts)
        {
            if(info.getAccountId().equals(account))
                return info.getContractInfo();
        }

        //create accountInfo for account if it does not exist
        AccountInfo info = new AccountInfo(account);
        accounts.add(info);
        accountData = Serialize(accounts);
        FileManager.writeFile(accountData, new File(accountDirectory));

        return new ArrayList<>();
    }

    public List<AccountInfo> Deserialize(String jsonArray)
    {
        Type listType = new TypeToken<ArrayList<AccountInfo>>(){}.getType();
        List<AccountInfo> accountList = gson.fromJson(jsonArray, listType);
        if(accountList == null)
            return new ArrayList<>();

        return accountList;
    }

    public String Serialize(List<AccountInfo> accountInfo)
    {
        return gson.toJson(accountInfo);
    }
}
