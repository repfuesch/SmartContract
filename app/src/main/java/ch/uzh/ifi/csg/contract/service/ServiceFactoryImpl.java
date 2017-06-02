package ch.uzh.ifi.csg.contract.service;

import android.support.v4.content.LocalBroadcastManager;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.service.account.AccountFileManager;
import ch.uzh.ifi.csg.contract.service.account.AccountManager;
import ch.uzh.ifi.csg.contract.service.account.AccountService;
import ch.uzh.ifi.csg.contract.service.account.CredentialProvider;
import ch.uzh.ifi.csg.contract.service.account.CredentialProviderImpl;
import ch.uzh.ifi.csg.contract.service.account.ParityAccountService;
import ch.uzh.ifi.csg.contract.service.account.WalletAccountService;
import ch.uzh.ifi.csg.contract.service.connection.EthConnectionService;
import ch.uzh.ifi.csg.contract.service.connection.Web3ConnectionService;
import ch.uzh.ifi.csg.contract.service.contract.FileManager;
import ch.uzh.ifi.csg.contract.service.contract.ContractManager;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import ch.uzh.ifi.csg.contract.service.contract.Web3jContractService;
import ch.uzh.ifi.csg.contract.service.exchange.CryptoCompareDeserializer;
import ch.uzh.ifi.csg.contract.service.exchange.EthExchangeService;
import ch.uzh.ifi.csg.contract.service.exchange.JsonHttpExchangeService;
import ch.uzh.ifi.csg.contract.web3j.protocol.ParityClientFactory;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.params.HttpConnectionParams;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.AppContext;

/**
 * Created by flo on 16.03.17.
 */

public class ServiceFactoryImpl implements EthServiceFactory
{
    private final CredentialProvider credentialProvider;
    private final FileManager fileManager;
    private final AppContext appContext;

    public ServiceFactoryImpl(String accountDirectory, AppContext context)
    {
        this.credentialProvider = new CredentialProviderImpl();
        this.fileManager = new FileManager(accountDirectory);
        this.appContext = context;
    }

    @Override
    public AccountService createParityAccountService(String host, int port)
    {
        String endpoint = "http://" + host + ":" + port + "/";
        Parity parity = ParityClientFactory.build(new HttpService(endpoint));
        return new ParityAccountService(parity, fileManager);
    }

    @Override
    public AccountService createWalletAccountService(String host, int port, String walletDirectory, boolean useFullEncryption)
    {
        String endpoint = "http://" + host + ":" + port + "/";
        Web3j web3 = ParityClientFactory.build(new HttpService(endpoint));
        WalletAccountService accountService = new WalletAccountService(web3, fileManager, credentialProvider, walletDirectory, useFullEncryption);

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
            int transactionSleepDuration)
    {
        String endpoint = "http://" + host + ":" + port + "/";
        Web3j web3j = ParityClientFactory.build(new HttpService(endpoint));
        TransactionManager transactionManager = new RawTransactionManager(web3j, credentialProvider.getCredentials(), transactionAttempts, transactionSleepDuration);

        return new Web3jContractService(web3j, transactionManager, fileManager, gasPrice, gasLimit);
    }

    @Override
    public ContractService createClientContractService(
            String host,
            int port,
            String selectedAccount,
            BigInteger gasPrice,
            BigInteger gasLimit,
            int transactionAttempts,
            int transactionSleepDuration)
    {
        String endpoint = "http://" + host + ":" + port + "/";

        Parity parity = ParityClientFactory.build(new HttpService(endpoint));

        TransactionManager transactionManager =
                new ClientTransactionManager(
                        parity,
                        selectedAccount,
                        transactionAttempts,
                        transactionSleepDuration);

        return new Web3jContractService(parity, transactionManager, fileManager, gasPrice, gasLimit);
    }

    @Override
    public EthExchangeService createHttpExchangeService()
    {
        String url = "https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=BTC,USD,EUR";
        CloseableHttpClient client = HttpClients.custom().setConnectionManagerShared(true).build();
        return new JsonHttpExchangeService(client, url, new CryptoCompareDeserializer());
    }

    @Override
    public EthConnectionService createConnectionService(String host, int port, int pollingInterval)
    {
        String endpoint = "http://" + host + ":" + port + "/";
        Parity parity = ParityClientFactory.build(new HttpService(endpoint, buildHttpClient(2000)));

        return new Web3ConnectionService(parity, Async.getExecutorService(), LocalBroadcastManager.getInstance(appContext), pollingInterval);
    }

    private CloseableHttpClient buildHttpClient(int timeout)
    {
        //todo:set connection timeout
        CloseableHttpClient client =  HttpClients.custom().setConnectionManagerShared(true).build();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), timeout);
        return client;
    }
}