package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider;

import java.math.BigInteger;

/**
 * Created by flo on 27.04.17.
 */

public interface SettingProvider
{
    String getWalletFileDirectory();
    String getWalletFileEncryptionStrength();
    String getHost();
    int getTransactionSleepDuration();
    int getTransactionAttempts();
    BigInteger getGasLimit();
    BigInteger getGasPrice();
    String getSelectedAccount();
    int getPort();
    String getAccountDirectory();
}
