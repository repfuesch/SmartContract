package ch.uzh.ifi.csg.contract.service;

import java.math.BigInteger;

import ch.uzh.ifi.csg.contract.service.account.AccountService;
import ch.uzh.ifi.csg.contract.service.account.CredentialProvider;
import ch.uzh.ifi.csg.contract.service.account.WalletAccountService;
import ch.uzh.ifi.csg.contract.service.connection.EthConnectionService;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import ch.uzh.ifi.csg.contract.service.exchange.EthExchangeService;

/**
 * Created by flo on 16.03.17.
 */

public interface EthServiceFactory
{
    AccountService createParityAccountService(String host, int port);

    AccountService createWalletAccountService(
            String host,
            int port,
            String walletDirectory,
            boolean useFullEncryption);

    ContractService createWalletContractService(
            String host,
            int port,
            String selectedAccount,
            BigInteger gasPrice,
            BigInteger gasLimit,
            int transactionAttempts,
            int transactionSleepDuration);

    ContractService createClientContractService(
            String host,
            int port,
            String selectedAccount,
            BigInteger gasPrice,
            BigInteger gasLimit,
            int transactionAttempts,
            int transactionSleepDuration);

    EthExchangeService createHttpExchangeService();

    EthConnectionService createConnectionService(String host, int port, int pollingInterval);
}
