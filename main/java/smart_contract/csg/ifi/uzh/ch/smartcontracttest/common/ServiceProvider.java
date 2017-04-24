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
import ch.uzh.ifi.csg.contract.service.connection.EthConnectionService;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import ch.uzh.ifi.csg.contract.service.EthServiceFactory;
import ch.uzh.ifi.csg.contract.service.ServiceFactoryImpl;
import ch.uzh.ifi.csg.contract.service.exchange.EthExchangeService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsProvider;

/**
 * Created by flo on 18.03.17.
 */

public class ServiceProvider
{
    private static ContractService contractService;
    private static AccountService accountService;
    private static EthExchangeService exchangeService;
    private static EthServiceFactory serviceFactory;
    private static EthConnectionService connectionService;

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

    public EthExchangeService getExchangeService() {return exchangeService; }

    public EthConnectionService getConnectionService() {return connectionService; }

    public void initServices(SettingsProvider settingsProvider)
    {
        if(serviceFactory == null)
            serviceFactory = new ServiceFactoryImpl(settingsProvider.getAccountDirectory());

        if(connectionService != null)
            connectionService.stopPolling();

        accountService = serviceFactory.createParityAccountService(
                settingsProvider.getHost(),
                settingsProvider.getPort()
                );

        exchangeService = serviceFactory.createHttpExchangeService();

        //connectionService = serviceFactory.createConnectionService(settingsProvider.getHost(), settingsProvider.getPort(), 5000);

        if(settingsProvider.getSelectedAccount() != null)
        {
            contractService = serviceFactory.createClientContractService(
                    settingsProvider.getHost(),
                    settingsProvider.getPort(),
                    settingsProvider.getSelectedAccount(),
                    settingsProvider.getGasPrice(),
                    settingsProvider.getGasLimit(),
                    settingsProvider.getTransactionAttempts(),
                    settingsProvider.getTransactionSleepDuration());
        }

        //connectionService.startPolling();

        /*
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
    */
    }

}
