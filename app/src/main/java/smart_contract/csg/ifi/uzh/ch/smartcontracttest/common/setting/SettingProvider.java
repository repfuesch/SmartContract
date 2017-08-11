package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.setting;

import org.web3j.tx.TransactionManager;

import java.math.BigInteger;

/**
 * Provider interface to access the {@link android.content.SharedPreferences} and
 * static settings of the application.
 */
public interface SettingProvider
{
    /**
     * Returns the directory in the internal storage in which the wallet files for accounts are
     * stored. This setting is static,
     * @return
     */
    String getWalletFileDirectory();

    /**
     * Indicates whether strong encryption should be used for wallet files
     * @return
     */
    boolean useStrongWalletFileEncryption();

    /**
     * Returns the polling interval to check the connection to the host
     * @return
     */
    int getHostPollingInterval();

    /**
     * Indicates if accounts are loaded from the data directory of the Ethereum account
     * @return
     */
    boolean useRemoteAccountManagement();

    /**
     * Returns the host name of the Ethereum client.
     * @return
     */
    String getHost();

    /**
     * Returns the Sleep duration for transactions that the {@link TransactionManager}
     * uses.
     *
     * @return
     */
    int getTransactionSleepDuration();

    /**
     * Returns the number of attempts for a trnsaction that the {@link TransactionManager} uses
     * @return
     */
    int getTransactionAttempts();

    /**
     * Returns the gas limit that the {@link TransactionManager} uses in transactions.
     * @return
     */
    BigInteger getGasLimit();

    /**
     * Returns the gas price that the {@link TransactionManager} uses in transactions.
     * @return
     */
    BigInteger getGasPrice();

    /**
     * Returns the address of the currently unlocked account or an emptry string when no accout is
     * unlocked.
     *
     * @return
     */
    String getSelectedAccount();

    /**
     * Returns the port used by the Ethereum client on teh remote host.
     * @return
     */
    int getPort();

    /**
     * returns the driectory in the internal storage in which accounts are stored. This setting is
     * static.
     * @return
     */
    String getAccountDirectory();

    /**
     * returns the internal directory in which images are stored. This setting is static.
     * @return
     */
    String getImageDirectory();
}
