package ch.uzh.ifi.csg.contract.service;

import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.ParityFactory;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;

import ch.uzh.ifi.csg.contract.setting.EthSettings;
/**
 * Created by flo on 16.03.17.
 */

public class ParityServiceFactory implements EthServiceFactory
{
    @Override
    public AccountUnlockService createAccountUnlockService(String host, int port)
    {
        String endpoint = "http://" + host + ":" + port + "/";
        Parity parity = ParityFactory.build(new HttpService(endpoint));
        return new ParityAccountUnlockService(parity);
    }

    @Override
    public ContractService createContractService(String host, int port, String selectedAccount, BigInteger gasPrice, BigInteger gasLimit, int transactionAttempts, int transactionSleepDuration)
    {
        String endpoint = "http://" + host + ":" + port + "/";
        Parity parity = ParityFactory.build(new HttpService(endpoint));
        TransactionManager transactionManager =
                new ClientTransactionManager(
                        parity,
                        selectedAccount,
                        transactionAttempts,
                        transactionSleepDuration);
        return new ParityContractService(parity, transactionManager, gasPrice, gasLimit);
    }
}
