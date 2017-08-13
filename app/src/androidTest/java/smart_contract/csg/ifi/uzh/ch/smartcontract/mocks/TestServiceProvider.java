package smart_contract.csg.ifi.uzh.ch.smartcontract.mocks;

import ch.uzh.ifi.csg.contract.service.account.AccountService;
import ch.uzh.ifi.csg.contract.service.connection.EthConnectionService;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import ch.uzh.ifi.csg.contract.service.exchange.EthConvertService;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.provider.ServiceProvider;

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
