package ch.uzh.ifi.csg.contract.setting;

import java.math.BigInteger;

/**
 * Created by flo on 05.02.17.
 */

public class EthSettings {

    private transient String selectedAccount = "";
    private String rpcHost = "192.168.0.178";
    private Integer rpcPort = 8545;
    private Integer transactionAttempts = 40;
    private Integer transactionSleepDuration = 15000;
    private BigInteger gasPrice = BigInteger.valueOf(0);
    private BigInteger gasLimit = BigInteger.valueOf(1712388);

    public EthSettings()
    {
    }

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof EthSettings))
            return false;

        EthSettings other = (EthSettings)o;

        return this.selectedAccount.equals(other.getSelectedAccount()) &&
                this.getRpcEndpoint().equals(other.getRpcEndpoint()) &&
                this.transactionAttempts.equals(other.getTransactionAttempts()) &&
                this.transactionSleepDuration.equals(other.getTransactionSleepDuration()) &&
                this.gasPrice.equals(other.getGasPrice()) &&
                this.gasLimit.equals(other.getGasLimit());
    }

    @Override
    public int hashCode()
    {
        int hashCode = 1;

        hashCode = hashCode * 37 + this.selectedAccount.hashCode();
        hashCode = hashCode * 37 + this.getRpcEndpoint().hashCode();
        hashCode = hashCode * 37 + this.transactionAttempts.hashCode();
        hashCode = hashCode * 37 + this.transactionSleepDuration.hashCode();
        hashCode = hashCode * 37 + this.gasPrice.hashCode();
        hashCode = hashCode * 37 + this.gasLimit.hashCode();

        return hashCode;
    }

    public String getSelectedAccount() {
        return selectedAccount;
    }

    public String getRpcHost() {
        return rpcHost;
    }

    public Integer getRpcPort() {
        return rpcPort;
    }

    public String getRpcEndpoint()
    {
        return "http://" + rpcHost + ":" + rpcPort + "/";
    }

    public void setSelectedAccount(String selectedAccount) {
        this.selectedAccount = selectedAccount;
    }

    public void setRpcHost(String rpcHost) {
        this.rpcHost = rpcHost;
    }

    public void setRpcPort(int rpcPort) {
        this.rpcPort = rpcPort;
    }

    public Integer getTransactionAttempts() {
        return transactionAttempts;
    }

    public void setTransactionAttempts(int transactionAttempts) {
        this.transactionAttempts = transactionAttempts;
    }

    public Integer getTransactionSleepDuration() {
        return transactionSleepDuration;
    }

    public void setTransactionSleepDuration(int transactionSleepDuration) {
        this.transactionSleepDuration = transactionSleepDuration;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }
}
