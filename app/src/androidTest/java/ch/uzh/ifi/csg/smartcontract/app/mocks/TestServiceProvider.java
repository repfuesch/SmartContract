package ch.uzh.ifi.csg.smartcontract.app.mocks;

import ch.uzh.ifi.csg.smartcontract.library.service.account.AccountService;
import ch.uzh.ifi.csg.smartcontract.library.service.connection.EthConnectionService;
import ch.uzh.ifi.csg.smartcontract.library.service.contract.ContractService;
import ch.uzh.ifi.csg.smartcontract.library.service.exchange.EthConvertService;
import ch.uzh.ifi.csg.smartcontract.app.common.service.ServiceProvider;

import static org.mockito.Mockito.mock;

/**
 * Test {@link ServiceProvider} that returns mocked service instances
 */
public class TestServiceProvider implements ServiceProvider
{
    private static ContractService contractService;
    private static AccountService accountService;
    private static EthConvertService convertService;
    private static EthConnectionService connectionService;

    public TestServiceProvider()
    {
        contractService = mock(ContractService.class);
        accountService = mock(AccountService.class);
        convertService = mock(EthConvertService.class);
        connectionService = mock(EthConnectionService.class);
    }

    @Override
    public ContractService getContractService() {
        return contractService;
    }

    @Override
    public AccountService getAccountService() {
        return accountService;
    }

    @Override
    public EthConvertService getExchangeService() {
        return convertService;
    }

    @Override
    public EthConnectionService getConnectionService() {
        return connectionService;
    }
}
