package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.File;

import ch.uzh.ifi.csg.contract.service.account.AccountService;
import ch.uzh.ifi.csg.contract.service.account.CredentialProviderImpl;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import ch.uzh.ifi.csg.contract.service.EthServiceFactory;
import ch.uzh.ifi.csg.contract.service.ServiceFactoryImpl;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsProvider;

/**
 * Created by flo on 18.03.17.
 */

public class ServiceProvider
{
    private static ContractService contractService;
    private static AccountService accountService;
    private static final EthServiceFactory serviceFactory = new ServiceFactoryImpl();

    private final static ServiceProvider instance;

    static {
        instance = new ServiceProvider();
    }

    public static ServiceProvider getInstance()
    {
        return instance;
    }

    public ContractService getContractService()
    {
        return contractService;
    }

    public AccountService getAccountService()
    {
        return accountService;
    }

    public void initServices(SettingsProvider settingsProvider)
    {
        /*
        accountService = serviceFactory.createParityAccountService(settingsProvider.getHost(), settingsProvider.getPort());
        if(settingsProvider.getSelectedAccount() != null)
        {
            contractService = serviceFactory.createClientContractService(
                    settingsProvider.getHost(),
                    settingsProvider.getPort(),
                    settingsProvider.getSelectedAccount(),
                    settingsProvider.getGasPrice(),
                    settingsProvider.getGasLimit(),
                    settingsProvider.getTransactionAttempts(),
                    settingsProvider.getTransactionSleepDuration(),
                    AppContext.getContext().getApplicationContext().getFilesDir() + "/contracts");
        }
        */

        accountService = serviceFactory.createWalletAccountService(
                settingsProvider.getHost(),
                settingsProvider.getPort(),
                AppContext.getContext().getApplicationContext().getFilesDir() + "/accounts",
                Environment.getExternalStorageDirectory() + "/Ethereum/keystore",
                false);

        if(settingsProvider.getSelectedAccount() != null)
        {
            contractService = serviceFactory.createWalletContractService(
                    settingsProvider.getHost(),
                    settingsProvider.getPort(),
                    settingsProvider.getSelectedAccount(),
                    settingsProvider.getGasPrice(),
                    settingsProvider.getGasLimit(),
                    settingsProvider.getTransactionAttempts(),
                    settingsProvider.getTransactionSleepDuration(),
                    AppContext.getContext().getApplicationContext().getFilesDir() + "/contracts");
        }

    }

}
