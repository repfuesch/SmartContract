package ch.uzh.ifi.csg.contract.service;

import android.support.v4.content.ContextCompat;

import org.jdeferred.Promise;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;

import java.io.File;
import java.math.BigInteger;

import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.service.account.Account;
import ch.uzh.ifi.csg.contract.service.account.AccountFileManager;
import ch.uzh.ifi.csg.contract.service.account.AccountManager;
import ch.uzh.ifi.csg.contract.service.account.AccountService;
import ch.uzh.ifi.csg.contract.service.account.CredentialProvider;
import ch.uzh.ifi.csg.contract.service.account.CredentialProviderImpl;
import ch.uzh.ifi.csg.contract.service.account.ParityAccountService;
import ch.uzh.ifi.csg.contract.service.account.WalletAccountService;
import ch.uzh.ifi.csg.contract.service.contract.ContractFileManager;
import ch.uzh.ifi.csg.contract.service.contract.ContractManager;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import ch.uzh.ifi.csg.contract.service.contract.Web3jContractService;
import ch.uzh.ifi.csg.contract.web3j.protocol.ParityClientFactory;

/**
 * Created by flo on 16.03.17.
 */

public class ServiceFactoryImpl implements EthServiceFactory
{
    private final CredentialProvider credentialProvider;

    public ServiceFactoryImpl()
    {
        this.credentialProvider = new CredentialProviderImpl();
    }

    @Override
    public AccountService createParityAccountService(String host, int port, String accountDirectory)
    {
        String endpoint = "http://" + host + ":" + port + "/";
        Parity parity = ParityClientFactory.build(new HttpService(endpoint));
        AccountManager accountManager = new AccountFileManager(accountDirectory);
        return new ParityAccountService(parity, accountManager);
    }

    @Override
    public AccountService createWalletAccountService(String host, int port, String accountDirectory, String walletDirectory, boolean useFullEncryption)
    {
        String endpoint = "http://" + host + ":" + port + "/";
        Web3j web3 = ParityClientFactory.build(new HttpService(endpoint));
        AccountManager accountManager = new AccountFileManager(accountDirectory);
        WalletAccountService accountService = new WalletAccountService(web3, accountManager, credentialProvider, walletDirectory, useFullEncryption);

        return accountService;
    }

    @Override
    public ContractService createWalletContractService(
            String host,
            int port,
            String selectedAccount,
            BigInteger gasPrice,
            BigInteger gasLimit,
            int transactionAttempts,
            int transactionSleepDuration,
            String contractDirectory)
    {
        String endpoint = "http://" + host + ":" + port + "/";
        Web3j web3j = ParityClientFactory.build(new HttpService(endpoint));
        TransactionManager transactionManager = new RawTransactionManager(web3j, credentialProvider.getCredentials(), transactionAttempts, transactionSleepDuration);
        ContractManager contractManager = new ContractFileManager(contractDirectory);

        return new Web3jContractService(web3j, transactionManager, contractManager, gasPrice, gasLimit);
    }

    @Override
    public ContractService createClientContractService(
            String host,
            int port,
            String selectedAccount,
            BigInteger gasPrice,
            BigInteger gasLimit,
            int transactionAttempts,
            int transactionSleepDuration,
            String contractDirectory)
    {
        String endpoint = "http://" + host + ":" + port + "/";
        Parity parity = ParityClientFactory.build(new HttpService(endpoint));

        TransactionManager transactionManager =
                new ClientTransactionManager(
                        parity,
                        selectedAccount,
                        transactionAttempts,
                        transactionSleepDuration);

        ContractManager contractManager = new ContractFileManager(contractDirectory);

        return new Web3jContractService(parity, transactionManager, contractManager, gasPrice, gasLimit);
    }
}
