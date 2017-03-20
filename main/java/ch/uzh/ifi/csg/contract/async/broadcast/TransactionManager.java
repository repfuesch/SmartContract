package ch.uzh.ifi.csg.contract.async.broadcast;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import org.jdeferred.Promise;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.AppContext;

/**
 * Created by flo on 18.03.17.
 */

public class TransactionManager
{
    public static final String ACTION_HANDLE_TRANSACTION = "ch.uzh.ifi.csg.smart_contract.transaction";
    public static final String CONTRACT_TRANSACTION_TYPE = "ch.uzh.ifi.csg.smart_contract.contract_action";
    public static final String CONTRACT_TRANSACTION_DEPLOY = "ch.uzh.ifi.csg.smart_contract.contract_deploy";
    public static final String CONTRACT_TRANSACTION_UPDATE = "ch.uzh.ifi.csg.smart_contract.contract_update";
    public static final String CONTRACT_TRANSACTION_ERROR = "ch.uzh.ifi.csg.smart_contract.contract_error";
    public static final String CONTRACT_TRANSACTION_ADDRESS = "ch.uzh.ifi.csg.smart_contract.contract_address";

    private static final LocalBroadcastManager broadcastManager;

    static {
        broadcastManager = LocalBroadcastManager.getInstance(AppContext.getContext());
    }

    private static void notifyContractUpdated(String contractAddress)
    {
        Intent intent = new Intent();
        intent.setAction(ACTION_HANDLE_TRANSACTION);
        intent.putExtra(CONTRACT_TRANSACTION_ADDRESS, contractAddress);
        intent.putExtra(CONTRACT_TRANSACTION_TYPE, CONTRACT_TRANSACTION_UPDATE);

        broadcastManager.sendBroadcast(intent);
    }

    private static void notifyContractCreated(String contractAddress)
    {
        Intent intent = new Intent();
        intent.setAction(ACTION_HANDLE_TRANSACTION);
        intent.putExtra(CONTRACT_TRANSACTION_ADDRESS, contractAddress);
        intent.putExtra(CONTRACT_TRANSACTION_TYPE, CONTRACT_TRANSACTION_DEPLOY);

        broadcastManager.sendBroadcast(intent);
    }

    private static void notifyTransactionError(String contractAddress, Throwable exception)
    {
        Intent intent = new Intent();
        intent.setAction(ACTION_HANDLE_TRANSACTION);
        intent.putExtra(CONTRACT_TRANSACTION_ADDRESS, contractAddress);
        intent.putExtra(CONTRACT_TRANSACTION_ERROR, exception);

        broadcastManager.sendBroadcast(intent);
    }

    public static <T> void toTransaction(SimplePromise<T> promise, final String contractAddress)
    {
        promise.always(new AlwaysCallback<T>()
        {
            @Override
            public void onAlways(Promise.State state, T resolved, Throwable rejected) {
                if(rejected != null)
                {
                    notifyTransactionError(contractAddress, rejected);
                }else{
                    if(contractAddress != null)
                    {
                        notifyContractUpdated(contractAddress);
                    }else{
                        notifyContractCreated(((IPurchaseContract)resolved).getContractAddress());
                    }
                }
            }
        });
    }
}
