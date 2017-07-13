package smart_contract.csg.ifi.uzh.ch.smartcontracttest.mocks;

import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.service.account.AccountService;
import ch.uzh.ifi.csg.contract.service.connection.EthConnectionService;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import ch.uzh.ifi.csg.contract.service.exchange.EthConvertService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ServiceProvider;

import static org.mockito.Mockito.mock;

/**
 * Created by flo on 13.07.17.
 */

public class TestServiceProvider implements ServiceProvider
{
    private static ContractService contractService = mock(ContractService.class);
    private static AccountService accountService = mock(AccountService.class);
    private static EthConvertService convertService = mock(EthConvertService.class);
    private static EthConnectionService connectionService = mock(EthConnectionService.class);


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
