package ch.uzh.ifi.csg.contract.service.contract;

import org.web3j.abi.datatypes.Utf8String;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.parity.Parity;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.contract.PurchaseContract;

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
    public SimplePromise<IPurchaseContract> deployContract(BigInteger value, String title, String description)
    {
        return PurchaseContract.deployContract(
                web3,
                transactionManager,
                gasPrice,
                gasLimit,
                "0x6060604052604051610803380380610803833981016040528051602082015190820191015b60023404600281815502341461003957610000565b60038054600160a060020a03191633600160a060020a031617905581516000805481805290917f290decd9548b62a8d60345a988386fc84ba6bc95484008f6362f93160ef3e563602060026101006001861615026000190190941693909304601f9081018490048201938701908390106100be57805160ff19168380011785556100eb565b828001600101855582156100eb579182015b828111156100eb5782518255916020019190600101906100d0565b5b5061010c9291505b8082111561010857600081556001016100f4565b5090565b50508060019080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061015a57805160ff1916838001178555610187565b82800160010185558215610187579182015b8281111561018757825182559160200191906001019061016c565b5b506101a89291505b8082111561010857600081556001016100f4565b5090565b50506004805460a060020a60ff02191690555b50505b610636806101cd6000396000f3006060604052361561007d5763ffffffff60e060020a60003504166308551a53811461008257806335a063b4146100ab5780633fa4f245146100ba5780634a79d50c146100d95780637150d8ae146101665780637284e4161461018f57806373fac6f01461021c578063c19d93fb1461022b578063d696069714610259575b610000565b346100005761008f610263565b60408051600160a060020a039092168252519081900360200190f35b34610000576100b8610272565b005b34610000576100c7610331565b60408051918252519081900360200190f35b34610000576100e6610337565b60408051602080825283518183015283519192839290830191850190808383821561012c575b80518252602083111561012c57601f19909201916020918201910161010c565b505050905090810190601f1680156101585780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b346100005761008f6103c5565b60408051600160a060020a039092168252519081900360200190f35b34610000576100e66103d4565b60408051602080825283518183015283519192839290830191850190808383821561012c575b80518252602083111561012c57601f19909201916020918201910161010c565b505050905090810190601f1680156101585780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34610000576100b8610461565b005b346100005761023861055c565b6040518082600281116100005760ff16815260200191505060405180910390f35b6100b861056c565b005b600354600160a060020a031681565b60035433600160a060020a0390811691161461028d57610000565b60008060045460ff60a060020a909104166002811161000057146102b057610000565b6040517f80b62b7017bb13cf105e22749ee2a06a417ffba8c7f57b665057e0f3c2e925d990600090a1600480546002919060a060020a60ff02191660a060020a830217905550600354604051600160a060020a039182169130163180156108fc02916000818181858888f19350505050151561032b57610000565b5b5b505b565b60025481565b6000805460408051602060026001851615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156103bd5780601f10610392576101008083540402835291602001916103bd565b820191906000526020600020905b8154815290600101906020018083116103a057829003601f168201915b505050505081565b600454600160a060020a031681565b60018054604080516020600284861615610100026000190190941693909304601f810184900484028201840190925281815292918301828280156103bd5780601f10610392576101008083540402835291602001916103bd565b820191906000526020600020905b8154815290600101906020018083116103a057829003601f168201915b505050505081565b60045433600160a060020a0390811691161461047c57610000565b60018060045460ff60a060020a9091041660028111610000571461049f57610000565b6040517f64ea507aa320f07ae13c28b5e9bf6b4833ab544315f5f2aa67308e21c252d47d90600090a16004805460a060020a60ff021916740200000000000000000000000000000000000000001790819055600254604051600160a060020a039092169181156108fc0291906000818181858888f19350505050158061054c5750600354604051600160a060020a039182169130163180156108fc02916000818181858888f19350505050155b1561032b57610000565b5b5b505b565b60045460a060020a900460ff1681565b60008060045460ff60a060020a9091041660028111610000571461058f57610000565b60028054023414806105a057610000565b6040517f764326667cab2f2f13cad5f7b7665c704653bd1acc250dcb7b422bce726896b490600090a160048054600160a060020a03331673ffffffffffffffffffffffffffffffffffffffff199091161760a060020a60ff02191660a060020a1790555b5b505b505600a165627a7a723058200fd6977231dc69b791504ea48dd71c40013e86c1166e1fbad9124f31cc7501af0029",
                value,
                new Utf8String(title),
                new Utf8String(description));
    }

    /**
     * Loads a contract from the blockchain specified by the provided address.
     *
     * @param contractAddress
     * @return a promise representing the result of the call.
     */
    @Override
    public SimplePromise<IPurchaseContract> loadContract(String contractAddress)
    {
        return PurchaseContract.loadContract(contractAddress, web3, transactionManager, gasPrice, gasLimit);
    }

    /**
     * Persists a contract for an account with the provided ContractManager.
     *
     * @param contract
     * @param account
     */
    @Override
    public void saveContract(IPurchaseContract contract, String account) {
        contractManager.saveContract(new ContractInfo(contract.state().get(), contract.getContractAddress()), account);
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
    public SimplePromise<List<IPurchaseContract>> loadContracts(String account) {

        final List<ContractInfo> contractInfos = contractManager.loadContracts(account);

        return Async.toPromise(new Callable<List<IPurchaseContract>>() {
            @Override
            public List<IPurchaseContract> call() throws Exception {
                List<IPurchaseContract> contractList = new ArrayList<IPurchaseContract>(contractInfos.size());
                for(ContractInfo info : contractInfos)
                {
                    contractList.add(loadContract(info.getContractAddress()).get());
                }

                return contractList;
            }
        });
    }
}
