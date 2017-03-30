package ch.uzh.ifi.csg.contract.service;

import java.math.BigInteger;

import ch.uzh.ifi.csg.contract.service.account.AccountService;
import ch.uzh.ifi.csg.contract.service.account.CredentialProvider;
import ch.uzh.ifi.csg.contract.service.account.WalletAccountService;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;

/**
 * Created by flo on 16.03.17.
 */

public interface EthServiceFactory
{
    AccountService createParityAccountService(String host, int port);

    AccountService createWalletAccountService(
            String host,
            int port,
            String accountDirectory,
            String walletDirectory,
            boolean useFullEncryption);

    ContractService createWalletContractService(
            String host,
            int port,
            String selectedAccount,
            BigInteger gasPrice,
            BigInteger gasLimit,
            int transactionAttempts,
            int transactionSleepDuration,
            String contractDirectory);

    ContractService createClientContractService(
            String host,
            int port,
            String selectedAccount,
            BigInteger gasPrice,
            BigInteger gasLimit,
            int transactionAttempts,
            int transactionSleepDuration,
            String contractDirectory);
}
