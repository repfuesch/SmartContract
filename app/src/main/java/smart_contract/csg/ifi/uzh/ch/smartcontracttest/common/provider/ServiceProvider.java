package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider;

import ch.uzh.ifi.csg.contract.service.account.AccountService;
import ch.uzh.ifi.csg.contract.service.connection.EthConnectionService;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import ch.uzh.ifi.csg.contract.service.exchange.EthConvertService;

/**
 * Interface to access Services that depend on the application settings.
 */
public interface ServiceProvider
{
    ContractService getContractService();
    AccountService getAccountService();
    EthConvertService getExchangeService();
    EthConnectionService getConnectionService();
}
