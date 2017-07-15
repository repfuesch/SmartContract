package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction;
import android.content.Intent;

import org.jdeferred.Promise;

import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.message.MessageService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.broadcast.BroadCastService;

/**
 * Created by flo on 18.03.17.
 */

public class TransactionManagerImpl implements TransactionManager
{
    private static TransactionManagerImpl instance;

    public static TransactionManagerImpl create(BroadCastService broadCastService, MessageService messageService)
    {
        if(instance == null)
            instance = new TransactionManagerImpl(broadCastService, messageService);
        return instance;
    }

    private final BroadCastService broadCastService;
    private final MessageService messageService;

    private TransactionManagerImpl(BroadCastService localBroadcastManager, MessageService messageService)
    {
        this.broadCastService = localBroadcastManager;
        this.messageService = messageService;
    }

    private void notifyContractUpdated(String contractAddress)
    {
        Intent intent = new Intent();
        intent.setAction(ACTION_UPDATE_TRANSACTION);
        intent.putExtra(CONTRACT_ADDRESS, contractAddress);

        broadCastService.sendBroadcast(intent);
    }

    private void notifyContractCreated(ITradeContract contract)
    {
        Intent intent = new Intent();
        intent.setAction(ACTION_CREATE_TRANSACTION);
        intent.putExtra(CONTRACT_ADDRESS, contract.getContractAddress());
        intent.putExtra(CONTRACT_TYPE, contract.getContractType());

        broadCastService.sendBroadcast(intent);
    }

    public <T> void toTransaction(SimplePromise<T> transactionPromise, final String contractAddress)
    {
        transactionPromise.always(new AlwaysCallback<T>()
        {
            @Override
            public void onAlways(Promise.State state, T resolved, Throwable rejected) {
                if(rejected != null)
                {
                    messageService.showErrorMessage("Transaction failed.");
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
                    messageService.showErrorMessage("Creation of contract failed.");
                }else{
                    notifyContractCreated(resolved);
                }
            }
        });
    }
}
