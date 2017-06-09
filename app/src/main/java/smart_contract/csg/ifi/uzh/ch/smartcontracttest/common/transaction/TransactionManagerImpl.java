package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import org.jdeferred.Promise;
import org.web3j.tx.*;

import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.MessageHandler;

/**
 * Created by flo on 18.03.17.
 */

public class TransactionManagerImpl implements TransactionManager
{
    private static TransactionManagerImpl instance;

    public static TransactionManagerImpl create(LocalBroadcastManager broadcastManager)
    {
        if(instance == null)
            instance = new TransactionManagerImpl(broadcastManager);
        return instance;
    }

    private final LocalBroadcastManager broadcastManager;

    private TransactionManagerImpl(LocalBroadcastManager localBroadcastManager)
    {
        this.broadcastManager = localBroadcastManager;
    }

    private void notifyContractUpdated(String contractAddress)
    {
        Intent intent = new Intent();
        intent.setAction(ACTION_HANDLE_TRANSACTION);
        intent.putExtra(CONTRACT_ADDRESS, contractAddress);
        intent.putExtra(CONTRACT_TRANSACTION_TYPE, CONTRACT_TRANSACTION_UPDATE);

        broadcastManager.sendBroadcast(intent);
    }

    private void notifyContractCreated(ITradeContract contract)
    {
        Intent intent = new Intent();
        intent.setAction(ACTION_HANDLE_TRANSACTION);
        intent.putExtra(CONTRACT_ADDRESS, contract.getContractAddress());
        intent.putExtra(CONTRACT_TYPE, contract.getContractType());
        intent.putExtra(CONTRACT_TRANSACTION_TYPE, CONTRACT_TRANSACTION_DEPLOY);

        broadcastManager.sendBroadcast(intent);
    }

    private void notifyError(String message)
    {
        Intent intent = new Intent();
        intent.setAction(MessageHandler.ACTION_SHOW_ERROR);
        intent.putExtra(MessageHandler.MESSAGE_SHOW_ERROR, message);
        broadcastManager.sendBroadcast(intent);
    }

    public <T> void toTransaction(SimplePromise<T> transactionPromise, final String contractAddress)
    {
        transactionPromise.always(new AlwaysCallback<T>()
        {
            @Override
            public void onAlways(Promise.State state, T resolved, Throwable rejected) {
                if(rejected != null)
                {
                    notifyError("Transaction failed. Reason: \n " + rejected.getMessage());
                }else{
                    notifyContractUpdated(contractAddress);
                }
            }
        });
    }

    public void toTransaction(SimplePromise<ITradeContract> deployPromise)
    {
        deployPromise.always(new AlwaysCallback<ITradeContract>()
        {
            @Override
            public void onAlways(Promise.State state, ITradeContract resolved, Throwable rejected) {
                if(rejected != null)
                {
                    notifyError("Creation of contract failed. Reason: \n " + rejected.getMessage());
                }else{
                    notifyContractCreated(resolved);
                }
            }
        });
    }
}
