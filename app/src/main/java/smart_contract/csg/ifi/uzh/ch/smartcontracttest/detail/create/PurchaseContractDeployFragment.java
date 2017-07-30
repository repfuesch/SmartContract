package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

/**
 * Created by flo on 05.06.17.
 */

public class PurchaseContractDeployFragment extends ContractDeployFragment
{
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_contract_create_purchase;
    }

    @Override
    protected SimplePromise<ITradeContract> deployContract(BigInteger priceWei, String title, String description, final boolean needsVerification, final Map<String, String> imageSignatures)
    {
        if(!(priceWei.mod(BigInteger.valueOf(2))).equals(BigInteger.ZERO))
        {
            priceWei = priceWei.add(BigInteger.ONE);
        }

        BigInteger priceWithDeposit = priceWei.multiply(BigInteger.valueOf(2));
        if(!ensureBalance(priceWithDeposit))
            return null;

        return  appContext.getServiceProvider().getContractService().deployPurchaseContract(
                priceWithDeposit,
                title,
                description,
                imageSignatures,
                needsVerification,
                !deployFull);
    }
}
