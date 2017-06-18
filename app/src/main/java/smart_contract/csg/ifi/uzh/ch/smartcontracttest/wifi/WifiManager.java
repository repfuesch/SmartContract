package smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi;

import android.support.v7.app.AppCompatActivity;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.MessageHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiBuyerCallback;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.wifi.peer.WifiSellerCallback;

/**
 * Created by flo on 16.06.17.
 */

public interface WifiManager
{
    void attach(AppCompatActivity activity, MessageHandler messageHandler);
    void detach();

    void requestBuyerConnection(WifiSellerCallback callback, boolean useIdentification);
    void requestContractData(WifiBuyerCallback callback);
}
