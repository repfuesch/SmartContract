package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider;

import ch.uzh.ifi.csg.contract.service.account.AccountService;
import ch.uzh.ifi.csg.contract.service.connection.EthConnectionService;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import ch.uzh.ifi.csg.contract.service.EthServiceFactory;
import ch.uzh.ifi.csg.contract.service.ServiceFactoryImpl;
import ch.uzh.ifi.csg.contract.service.exchange.EthConvertService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.AppContext;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.setting.SettingProvider;

/**
 * Manages and provides Ethereum related service instances.
 */
public class EthServiceProvider implements ServiceProvider
{
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

    public EthServiceProvider(AppContext context)
    {
        appContext = context;
    }

    /**
     * Re-initializes all service objects from the SettingProvider
     *
     * @param settingsProvider
     */
    public void initServices(SettingProvider settingsProvider)
    {
        serviceFactory = new ServiceFactoryImpl(settingsProvider.getAccountDirectory(), appContext);

        if(connectionService != null)
            connectionService.stopPolling();

        //initialize the account and contract service based on the settings of the user
        if(settingsProvider.useRemoteAccountManagement())
        {
            accountService = serviceFactory.createParityAccountService(
                    settingsProvider.getHost(),
                    settingsProvider.getPort()
            );

            if(!settingsProvider.getSelectedAccount().isEmpty()) {

                contractService = serviceFactory.createClientContractService(
                        settingsProvider.getHost(),
                        settingsProvider.getPort(),
                        settingsProvider.getSelectedAccount(),
                        settingsProvider.getGasPrice(),
                        settingsProvider.getGasLimit(),
                        settingsProvider.getTransactionAttempts(),
                        settingsProvider.getTransactionSleepDuration());
            }
        }else{

            accountService = serviceFactory.createWalletAccountService(
                    settingsProvider.getHost(),
                    settingsProvider.getPort(),
                    settingsProvider.useStrongWalletFileEncryption());

            if(!settingsProvider.getSelectedAccount().isEmpty()) {

                contractService = serviceFactory.createWalletContractService(
                        settingsProvider.getHost(),
                        settingsProvider.getPort(),
                        settingsProvider.getSelectedAccount(),
                        settingsProvider.getGasPrice(),
                        settingsProvider.getGasLimit(),
                        settingsProvider.getTransactionAttempts(),
                        settingsProvider.getTransactionSleepDuration());
            }
        }

        exchangeService = serviceFactory.createHttpExchangeService();

        connectionService = serviceFactory.createConnectionService(settingsProvider.getHost(), settingsProvider.getPort(), settingsProvider.getHostPollingInterval());
        connectionService.startPolling();

    }
}
