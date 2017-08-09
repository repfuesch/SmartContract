package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction;
import android.content.Intent;

import org.jdeferred.Promise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.service.connection.EthConnectionService;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.message.MessageService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.broadcast.BroadCastService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContext;

/**
 * Implementation of the {@link TransactionHandler} interface.
 */
public class TransactionHandlerImpl implements TransactionHandler
{
    private final ApplicationContext applicationContext;
    private List<ContractInfo> contractList;

    public TransactionHandlerImpl(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
        this.contractList = new ArrayList<>();
    }

    private void notifyContractUpdated(String contractAddress)
    {
        Intent intent = new Intent();
        intent.setAction(ACTION_TRANSACTION_UPDATED);
        intent.putExtra(CONTRACT_ADDRESS, contractAddress);

        applicationContext.getBroadCastService().sendBroadcast(intent);
    }

    private void notifyContractCreated(ITradeContract contract)
    {
        Intent intent = new Intent();
        intent.setAction(ACTION_TRANSACTION_CREATED);
        intent.putExtra(CONTRACT_ADDRESS, contract.getContractAddress());
        intent.putExtra(CONTRACT_TYPE, contract.getContractType());

        applicationContext.getBroadCastService().sendBroadcast(intent);
    }

    public <T> void toTransaction(SimplePromise<T> transactionPromise, final String contractAddress)
    {
        transactionPromise.always(new AlwaysCallback<T>()
        {
            @Override
            public void onAlways(Promise.State state, T resolved, Throwable rejected) {
                if(rejected != null)
                {
                    applicationContext.getMessageService().showErrorMessage("Transaction failed.");
                }else{
                    notifyContractUpdated(contractAddress);
                }
            }
        });
    }

    public <T extends ITradeContract> void toDeployTransaction(SimplePromise<T> deployPromise, final ContractInfo contractInfo, final String account, final ContractService contractService)
    {
        contractList.add(contractInfo);

        deployPromise.always(new AlwaysCallback<T>()
        {
            @Override
            public void onAlways(Promise.State state, ITradeContract resolved, Throwable rejected) {
                if(rejected != null)
                {
                    if(applicationContext.getServiceProvider().getConnectionService().hasConnection())
                    {
                        applicationContext.getMessageService().showErrorMessage("Creation of contract failed.");
                    }else{
                        //We don't know if contract creation was successful because the connection to the host is interrupted
                        //We save the contract anyway such that it can be recovered later
                        contractService.saveContract(contractInfo, account);
                    }
                    contractList.remove(contractInfo);
                }else
                {
                    contractService.saveContract(contractInfo, account);
                    contractList.remove(contractInfo);
                    notifyContractCreated(resolved);
                }
            }
        });
    }

    @Override
    public boolean hasOpenTransactions() {
        return !contractList.isEmpty();
    }
}
