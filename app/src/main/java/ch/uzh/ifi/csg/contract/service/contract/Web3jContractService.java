package ch.uzh.ifi.csg.contract.service.contract;

import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.contract.PurchaseContract;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;

/**
 * Web3j implementation of the ContractService.
 */

public class Web3jContractService implements ContractService
{
    private final Web3j web3;
    private final TransactionManager transactionManager;
    private final BigInteger gasPrice;
    private final BigInteger gasLimit;
    private final ContractManager contractManager;

    /**
     *
     * @param web3: the rpc client
     * @param transactionManager: the transaction manager used by the deployed contracts to perform transactions
     * @param contractManager: ContractManager implementation used to persist created contracts for an account
     * @param gasPrice
     * @param gasLimit
     */
    public Web3jContractService(Web3j web3, TransactionManager transactionManager, ContractManager contractManager, BigInteger gasPrice, BigInteger gasLimit)
    {
        this.web3 = web3;
        this.transactionManager = transactionManager;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.contractManager = contractManager;
    }

    /**
     * Deploys a purchase contract on the blockchain using the PurchaseContract.deployContract
     * factory method.
     *
     * @param value
     * @param title
     * @param description
     * @return  a promise representing the result of the call.
     */
    @Override
    public SimplePromise<IPurchaseContract> deployContract(BigInteger value, String title, String description, boolean verifyIdentity)
    {
        return PurchaseContract.deployContract(
                web3,
                transactionManager,
                gasPrice,
                gasLimit,
                "0x6060604052604051610b15380380610b15833981016040528080518201919060200180518201919060200180519060200190919050505b600234811561000057046002819055503460025460020214151561005957610000565b33600360006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508260009080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106100e657805160ff1916838001178555610114565b82800160010185558215610114579182015b828111156101135782518255916020019190600101906100f8565b5b50905061013991905b8082111561013557600081600090555060010161011d565b5090565b505080600460146101000a81548160ff0219169083151502179055508160019080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106101a157805160ff19168380011785556101cf565b828001600101855582156101cf579182015b828111156101ce5782518255916020019190600101906101b3565b5b5090506101f491905b808211156101f05760008160009055506001016101d8565b5090565b50506000600460156101000a81548160ff0219169083600281116100005702179055505b5050505b6108ea8061022b6000396000f300606060405236156100a2576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806308551a53146100a757806335a063b4146100f65780633fa4f245146101055780634a79d50c146101285780637150d8ae146101be5780637284e4161461020d57806373fac6f0146102a3578063c19d93fb146102b2578063d6960697146102e0578063e8731c32146102ea575b610000565b34610000576100b4610311565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b3461000057610103610337565b005b3461000057610112610494565b6040518082815260200191505060405180910390f35b346100005761013561049a565b6040518080602001828103825283818151815260200191508051906020019080838360008314610184575b80518252602083111561018457602082019150602081019050602083039250610160565b505050905090810190601f1680156101b05780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34610000576101cb610538565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b346100005761021a61055e565b6040518080602001828103825283818151815260200191508051906020019080838360008314610269575b80518252602083111561026957602082019150602081019050602083039250610245565b505050905090810190601f1680156102955780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34610000576102b06105fc565b005b34610000576102bf6107bd565b6040518082600281116100005760ff16815260200191505060405180910390f35b6102e86107d0565b005b34610000576102f76108ab565b604051808215151515815260200191505060405180910390f35b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561039357610000565b6000806002811161000057600460159054906101000a900460ff1660028111610000571415156103c257610000565b7f80b62b7017bb13cf105e22749ee2a06a417ffba8c7f57b665057e0f3c2e925d960405180905060405180910390a16002600460156101000a81548160ff021916908360028111610000570217905550600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc3073ffffffffffffffffffffffffffffffffffffffff16319081150290604051809050600060405180830381858888f19350505050151561048e57610000565b5b5b505b565b60025481565b60008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156105305780601f1061050557610100808354040283529160200191610530565b820191906000526020600020905b81548152906001019060200180831161051357829003601f168201915b505050505081565b600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156105f45780601f106105c9576101008083540402835291602001916105f4565b820191906000526020600020905b8154815290600101906020018083116105d757829003601f168201915b505050505081565b600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561065857610000565b6001806002811161000057600460159054906101000a900460ff16600281116100005714151561068757610000565b7f64ea507aa320f07ae13c28b5e9bf6b4833ab544315f5f2aa67308e21c252d47d60405180905060405180910390a16002600460156101000a81548160ff021916908360028111610000570217905550600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc6002549081150290604051809050600060405180830381858888f1935050505015806107ad5750600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc3073ffffffffffffffffffffffffffffffffffffffff16319081150290604051809050600060405180830381858888f19350505050155b156107b757610000565b5b5b505b565b600460159054906101000a900460ff1681565b6000806002811161000057600460159054906101000a900460ff1660028111610000571415156107ff57610000565b600254600202341480151561081357610000565b7f764326667cab2f2f13cad5f7b7665c704653bd1acc250dcb7b422bce726896b460405180905060405180910390a133600460006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506001600460156101000a81548160ff0219169083600281116100005702179055505b5b505b50565b600460149054906101000a900460ff16815600a165627a7a72305820c97898906d5a18f417eb0a80e4f1bc6e49c6862427fc8a7089e3bae831da49db0029",
                value,
                new Utf8String(title),
                new Utf8String(description),
                new Bool(verifyIdentity));
    }

    /**
     * Loads a contract from the blockchain specified by the provided address.
     *
     * @param contractAddress
     * @return a promise representing the result of the call.
     */
    @Override
    public SimplePromise<IPurchaseContract> loadContract(final String contractAddress, final String account)
    {
        return Async.toPromise(new Callable<IPurchaseContract>() {
            @Override
            public IPurchaseContract call() throws Exception {

                IPurchaseContract contract = PurchaseContract.loadContract(contractAddress, web3, transactionManager, gasPrice, gasLimit).get();
                ContractInfo contractInfo = contractManager.getContract(contractAddress, account);
                if(contractInfo != null)
                {
                    if(contractInfo.getUserProfile() != null)
                    {
                        contract.setUserProfile(contractInfo.getUserProfile());
                    }

                }else{
                    contractManager.saveContract(new ContractInfo(contract.state().get(), contractAddress), account);
                }

                return contract;
            }
        });
    }

    /**
     * Persists a contract for an account with the provided ContractManager.
     *
     * @param contract
     * @param account
     */
    @Override
    public void saveContract(IPurchaseContract contract, String account) {
        contractManager.saveContract(new ContractInfo(contract.state().get(), contract.getContractAddress(), contract.getUserProfile()), account);
    }

    /**
     * Removes a contract from a persisted storage. This method does not delete a contract from the
     * blockchain.
     *
     * @param contract
     * @param account
     */
    @Override
    public void removeContract(IPurchaseContract contract, String account) {
        contractManager.deleteContract(new ContractInfo(contract.state().get(), contract.getContractAddress()), account);
    }

    /**
     * Loads all persisted contracts for the specified account.
     *
     * @param account
     * @return  a promise representing the result of the call.
     */
    @Override
    public SimplePromise<List<IPurchaseContract>> loadContracts(final String account) {

        final List<ContractInfo> contractInfos = contractManager.getContracts(account);

        return Async.toPromise(new Callable<List<IPurchaseContract>>() {
            @Override
            public List<IPurchaseContract> call() throws Exception {
                List<IPurchaseContract> contractList = new ArrayList<IPurchaseContract>(contractInfos.size());
                for(ContractInfo info : contractInfos)
                {
                    contractList.add(loadContract(info.getContractAddress(), account).get());
                }

                return contractList;
            }
        });
    }

    @Override
    public SimplePromise<Boolean> isContract(final String address) {
        return Async.toPromise(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                EthGetCode response = web3.ethGetCode(address, DefaultBlockParameterName.LATEST).send();
                if(response.hasError())
                    return false;

                return response.getCode().length() > 0;
            }
        });
    }
}
