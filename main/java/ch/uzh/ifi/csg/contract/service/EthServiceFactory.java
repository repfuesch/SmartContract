package ch.uzh.ifi.csg.contract.service;

import java.math.BigInteger;

import ch.uzh.ifi.csg.contract.setting.EthSettings;

/**
 * Created by flo on 16.03.17.
 */

public interface EthServiceFactory
{
    AccountUnlockService createAccountUnlockService(String host, int port);

    ContractService createContractService(
            String host,
            int port,
            String selectedAccount,
            BigInteger gasPrice,
            BigInteger gasLimit,
            int transactionAttempts,
            int transactionSleepDuration);
}
