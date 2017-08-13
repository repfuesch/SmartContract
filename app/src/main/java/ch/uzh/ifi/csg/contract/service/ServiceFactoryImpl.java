package ch.uzh.ifi.csg.contract.service;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.ParityFactory;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.service.account.AccountService;
import ch.uzh.ifi.csg.contract.service.account.CredentialProvider;
import ch.uzh.ifi.csg.contract.service.account.CredentialProviderImpl;
import ch.uzh.ifi.csg.contract.service.account.ParityAccountService;
import ch.uzh.ifi.csg.contract.service.account.WalletAccountService;
import ch.uzh.ifi.csg.contract.service.account.WalletWrapper;
import ch.uzh.ifi.csg.contract.service.connection.EthConnectionService;
import ch.uzh.ifi.csg.contract.service.connection.Web3ConnectionService;
import ch.uzh.ifi.csg.contract.service.contract.FileManager;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import ch.uzh.ifi.csg.contract.service.contract.Web3jContractService;
import ch.uzh.ifi.csg.contract.service.exchange.CryptoCompareDeserializer;
import ch.uzh.ifi.csg.contract.service.exchange.EthConvertService;
import ch.uzh.ifi.csg.contract.service.exchange.JsonHttpConvertService;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.AppContext;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.provider.ApplicationContext;

/**
 * Implementation of the {@link EthServiceFactory}
 */
public class ServiceFactoryImpl implements EthServiceFactory
{
    private static final CredentialProvider credentialProvider = new CredentialProviderImpl();

    private final FileManager fileManager;
    private final ApplicationContext appContext;

    public ServiceFactoryImpl(String accountDirectory, AppContext context)
    {
        this.fileManager = new FileManager(accountDirectory);
        this.appContext = context;
    }

    @Override
    public AccountService createParityAccountService(String host, int port)
    {
        String endpoint = "http://" + host + ":" + port + "/";
        Parity parity = ParityFactory.build(new HttpService(endpoint));
        return new ParityAccountService(parity, fileManager);
    }

    @Override
    public AccountService createWalletAccountService(String host, int port, boolean useFullEncryption)
    {
        String endpoint = "http://" + host + ":" + port + "/";
        Web3j web3 = ParityFactory.build(new HttpService(endpoint));
        WalletAccountService accountService = new WalletAccountService(web3, fileManager, credentialProvider, appContext.getSettingProvider().getWalletFileDirectory(), useFullEncryption, new WalletWrapper());
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
        Web3j web3j = ParityFactory.build(new HttpService(endpoint, buildHttpClient(1000)));
        TransactionManager transactionManager = new RawTransactionManager(web3j, credentialProvider.getCredentials(), transactionAttempts, transactionSleepDuration);

        return new Web3jContractService(web3j, transactionManager, fileManager, appContext.getTransactionManager(), gasPrice, gasLimit, credentialProvider.getCredentials().getAddress());
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
        Parity parity = ParityFactory.build(new HttpService(endpoint, buildHttpClient(1000)));
        TransactionManager transactionManager =
                new ClientTransactionManager(
                        parity,
                        selectedAccount,
                        transactionAttempts,
                        transactionSleepDuration);

        return new Web3jContractService(parity, transactionManager, fileManager, appContext.getTransactionManager(), gasPrice, gasLimit, selectedAccount);
    }

    @Override
    public EthConvertService createHttpExchangeService()
    {
        String url = "https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=BTC,USD,EUR";
        return new JsonHttpConvertService(buildHttpClient(1000), url, new CryptoCompareDeserializer());
    }

    @Override
    public EthConnectionService createConnectionService(String host, int port, int pollingInterval)
    {
        String endpoint = "http://" + host + ":" + port + "/";
        Parity parity = ParityFactory.build(new HttpService(endpoint, buildHttpClient(1000)));

        return new Web3ConnectionService(parity, Async.getScheduledExecutorService(), appContext.getBroadCastService(), pollingInterval);
    }

    /**
     * Creates a {@link CloseableHttpClient} with a specific connection timeout
     * @param timeout
     * @return
     */
    private CloseableHttpClient buildHttpClient(int timeout)
    {
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom().setConnectionRequestTimeout(timeout).setConnectTimeout(timeout).setSocketTimeout(timeout);
        CloseableHttpClient client = HttpClients.custom().setConnectionManagerShared(true).setDefaultRequestConfig(requestConfigBuilder.build()).build();
        return client;
    }
}
