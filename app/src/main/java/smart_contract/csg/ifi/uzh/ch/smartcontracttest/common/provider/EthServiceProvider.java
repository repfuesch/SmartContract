package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider;

import ch.uzh.ifi.csg.contract.service.account.AccountService;
import ch.uzh.ifi.csg.contract.service.connection.EthConnectionService;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import ch.uzh.ifi.csg.contract.service.EthServiceFactory;
import ch.uzh.ifi.csg.contract.service.ServiceFactoryImpl;
import ch.uzh.ifi.csg.contract.service.exchange.EthConvertService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.AppContext;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.setting.SettingProviderImpl;

/**
 * Created by flo on 18.03.17.
 */

public class EthServiceProvider implements ServiceProvider
{
    private static EthServiceProvider instance;

    public static EthServiceProvider create(AppContext context)
    {
        instance = new EthServiceProvider(context);
        return instance;
    }

    private final AppContext appContext;
    private ContractService contractService;
    private AccountService accountService;
    private EthConvertService exchangeService;
    private EthServiceFactory serviceFactory;
    private EthConnectionService connectionService;

    public ContractService getContractService()
    {
        return contractService;
    }
    public AccountService getAccountService()
    {
        return accountService;
    }
    public EthConvertService getExchangeService() {return exchangeService; }
    public EthConnectionService getConnectionService() {return connectionService; }

    private EthServiceProvider(AppContext context)
    {
        appContext = context;
    }

    public void initServices(SettingProviderImpl settingsProvider)
    {
        if(serviceFactory == null)
            serviceFactory = new ServiceFactoryImpl(settingsProvider.getAccountDirectory(), appContext);

        if(connectionService != null)
            connectionService.stopPolling();

        accountService = serviceFactory.createParityAccountService(
                settingsProvider.getHost(),
                settingsProvider.getPort()
                );

        exchangeService = serviceFactory.createHttpExchangeService();

        connectionService = serviceFactory.createConnectionService(settingsProvider.getHost(), settingsProvider.getPort(), 5000);
        connectionService.startPolling();

        if(settingsProvider.getSelectedAccount() != "")
        {
            contractService = serviceFactory.createClientContractService(
                    settingsProvider.getHost(),
                    settingsProvider.getPort(),
                    settingsProvider.getSelectedAccount(),
                    settingsProvider.getGasPrice(),
                    settingsProvider.getGasLimit(),
                    settingsProvider.getTransactionAttempts(),
                    settingsProvider.getTransactionSleepDuration());


            //todo: remove the following lines
            //delete all contracts for account
            /*
            for(ITradeContract contract : contractService.loadContracts(settingsProvider.getSelectedAccount()).get())
            {
                contractService.removeContract(contract, settingsProvider.getSelectedAccount());
            }*/

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
