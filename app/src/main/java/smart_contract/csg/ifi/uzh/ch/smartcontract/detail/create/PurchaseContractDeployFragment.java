package smart_contract.csg.ifi.uzh.ch.smartcontract.detail.create;

import java.math.BigInteger;
import java.util.Map;

import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import smart_contract.csg.ifi.uzh.ch.smartcontract.R;

/**
 * Fragment to deploy an {@link IPurchaseContract}
 */
public class PurchaseContractDeployFragment extends ContractDeployFragment
{
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_contract_create_purchase;
    }

    /**
     * see {@link ContractDeployFragment#deployContract}
     * see {@link ContractService#deployPurchaseContract}
     */
    @Override
    protected void deployContract(BigInteger priceWei, String title, String description, final boolean needsVerification, final Map<String, String> imageSignatures)
    {
        //make sure that the price is dividable by 2
        if(!(priceWei.mod(BigInteger.valueOf(2))).equals(BigInteger.ZERO))
        {
            priceWei = priceWei.add(BigInteger.ONE);
        }

        //ensure balance of account
        BigInteger priceWithDeposit = priceWei.multiply(BigInteger.valueOf(2));
        if(!ensureBalance(priceWithDeposit))
            return;

        //deploy the contract using the ContractService
        appContext.getServiceProvider().getContractService().deployPurchaseContract(
                priceWithDeposit,
                title,
                description,
                imageSignatures,
                needsVerification,
                !deployFull).get();
    }
}
