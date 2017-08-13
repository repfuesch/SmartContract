package ch.uzh.ifi.csg.smartcontract.library.service;

import java.math.BigInteger;

import ch.uzh.ifi.csg.smartcontract.library.service.account.AccountService;
import ch.uzh.ifi.csg.smartcontract.library.service.account.ParityAccountService;
import ch.uzh.ifi.csg.smartcontract.library.service.account.WalletAccountService;
import ch.uzh.ifi.csg.smartcontract.library.service.connection.EthConnectionService;
import ch.uzh.ifi.csg.smartcontract.library.service.connection.Web3ConnectionService;
import ch.uzh.ifi.csg.smartcontract.library.service.contract.ContractService;
import ch.uzh.ifi.csg.smartcontract.library.service.contract.Web3jContractService;
import ch.uzh.ifi.csg.smartcontract.library.service.exchange.EthConvertService;
import ch.uzh.ifi.csg.smartcontract.library.service.exchange.JsonHttpConvertService;

/**
 * Factory interface to create concrete {@link AccountService}, {@link ContractService},
 * {@link EthConvertService} and {@link EthConnectionService} instances.
 */
public interface EthServiceFactory
{
    /**
     * Create an instance of {@link ParityAccountService}
     *
     * @param host
     * @param port
     * @return
     */
    AccountService createParityAccountService(String host, int port);

    /**
     * Returns an instance of {@link WalletAccountService}
     *
     * @param host
     * @param port
     * @param useFullEncryption: indicates if the {@link org.web3j.crypto.WalletUtils} uses strong
                                 encryption to encrypt the wallet files
     * @return
     */
    AccountService createWalletAccountService(
            String host,
            int port,
            boolean useFullEncryption);

    /**
     * Returns a {@link Web3jContractService} instance that uses the
     * {@link org.web3j.tx.RawTransactionManager} with the provided transaction parameters
     * to sign transactions.
     *
     * @param host
     * @param port
     * @param selectedAccount
     * @param gasPrice
     * @param gasLimit
     * @param transactionAttempts
     * @param transactionSleepDuration
     * @return
     */
    ContractService createWalletContractService(
            String host,
            int port,
            String selectedAccount,
            BigInteger gasPrice,
            BigInteger gasLimit,
            int transactionAttempts,
            int transactionSleepDuration);

    /**
     * Returns a {@link Web3jContractService} instance that uses the
     * {@link org.web3j.tx.ClientTransactionManager} with the provided transaction parameters
     * to sign transactions.
     *
     * @param host
     * @param port
     * @param selectedAccount
     * @param gasPrice
     * @param gasLimit
     * @param transactionAttempts
     * @param transactionSleepDuration
     * @return
     */
    ContractService createClientContractService(
            String host,
            int port,
            String selectedAccount,
            BigInteger gasPrice,
            BigInteger gasLimit,
            int transactionAttempts,
            int transactionSleepDuration);

    /**
     * Create an instance of {@link JsonHttpConvertService}
     * @return
     */
    EthConvertService createHttpExchangeService();

    /**
     * Returns an instance of {@link Web3ConnectionService}
     *
     * @param host
     * @param port
     * @return
     */
    EthConnectionService createConnectionService(String host, int port, int pollingInterval);
}
