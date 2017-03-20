package ch.uzh.ifi.csg.contract.transaction;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.exceptions.TransactionTimeoutException;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

/**
 * <p>TransactionManager implementation for using an Ethereum node to transact.
 *
 * <p><b>Note</b>: accounts must be unlocked on the node for transactions to be successful.
 */
public class PersonalTransactionManager extends TransactionManager {

    private final Web3j web3j;
    private String fromAddress;

    public PersonalTransactionManager(Web3j web3j, String fromAddress)
    {
        super(web3j);
        this.web3j = web3j;
        this.fromAddress = fromAddress;
    }

    public void setAddress(String address)
    {
        fromAddress = address;
    }

    @Override
    public EthSendTransaction sendTransaction(
            BigInteger gasPrice,
            BigInteger gasLimit,
            String to,
            String data,
            BigInteger value) throws ExecutionException, InterruptedException, TransactionTimeoutException
    {
        Transaction transaction = new Transaction(
                fromAddress, null, gasPrice, gasLimit, to, value, data);

        return web3j.ethSendTransaction(transaction)
                .sendAsync().get();
    }

    @Override
    public String getFromAddress() {
        return fromAddress;
    }
}
