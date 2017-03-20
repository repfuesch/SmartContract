package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

import ch.uzh.ifi.csg.contract.service.AccountUnlockService;
import ch.uzh.ifi.csg.contract.service.ContractService;
import ch.uzh.ifi.csg.contract.service.EthServiceFactory;
import ch.uzh.ifi.csg.contract.service.ParityServiceFactory;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.setting.SettingsProvider;

/**
 * Created by flo on 18.03.17.
 */

public class ServiceProvider
{
    private static ContractService contractService;
    private static AccountUnlockService accountService;
    private static final EthServiceFactory serviceFactory = new ParityServiceFactory();

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

    public AccountUnlockService getAccountService()
    {
        return accountService;
    }

    public void initServices(SettingsProvider settingsProvider)
    {
        accountService = serviceFactory.createAccountUnlockService(settingsProvider.getHost(), settingsProvider.getPort());

        if(settingsProvider.getSelectedAccount() != null)
        {
            contractService = serviceFactory.createContractService(
                    settingsProvider.getHost(),
                    settingsProvider.getPort(),
                    settingsProvider.getSelectedAccount(),
                    settingsProvider.getGasPrice(),
                    settingsProvider.getGasLimit(),
                    settingsProvider.getTransactionAttempts(),
                    settingsProvider.getTransactionSleepDuration());
        }
    }

}
