package ch.uzh.ifi.csg.contract.service.connection;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.web3j.protocol.Web3j;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.broadcast.BroadCastService;

/**
 * Created by flo on 10.04.17.
 */

public class Web3ConnectionService implements EthConnectionService {

    private Web3j web3;
    private ScheduledExecutorService executorService;
    private BroadCastService broadCastService;
    private int pollingInterval;
    private ScheduledFuture future;
    private boolean connectionUp;
    private boolean connectionWasUp;

    public Web3ConnectionService(Web3j web3, ScheduledExecutorService executorService, BroadCastService broadCastService, int pollingInterval)
    {
        this.web3 = web3;
        this.executorService = executorService;
        this.broadCastService = broadCastService;
        this.pollingInterval = pollingInterval;
        connectionUp = true;
    }

    @Override
    public void startPolling() {

        future = executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try{
                    web3.ethGasPrice().send();
                    if(!connectionUp)
                    {
                        connectionUp = true;
                        broadcastConnectionChanged();
                    }
                }catch(IOException ex)
                {
                    if(connectionUp)
                    {
                        connectionUp = false;
                        broadcastConnectionChanged();
                    }
                }
            }
        }, 0, pollingInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stopPolling()
    {
        if(future != null)
            future.cancel(true);
    }

    @Override
    public boolean hasConnection() {
        return connectionUp;
    }

    private void broadcastConnectionChanged()
    {
        Intent intent = new Intent();
        if(connectionUp)
        {
            intent.setAction(EthConnectionService.ACTION_HANDLE_CONNECTION_UP);
        }else{
            intent.setAction(EthConnectionService.ACTION_HANDLE_CONNECTION_DOWN);
        }
        broadCastService.sendBroadcast(intent);
    }

}
