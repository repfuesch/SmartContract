package ch.uzh.ifi.csg.smartcontract.app.common.service;

import ch.uzh.ifi.csg.smartcontract.library.service.account.AccountService;
import ch.uzh.ifi.csg.smartcontract.library.service.connection.EthConnectionService;
import ch.uzh.ifi.csg.smartcontract.library.service.contract.ContractService;
import ch.uzh.ifi.csg.smartcontract.library.service.exchange.EthConvertService;

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
