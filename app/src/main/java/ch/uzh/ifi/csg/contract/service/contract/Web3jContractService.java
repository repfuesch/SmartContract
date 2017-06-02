package ch.uzh.ifi.csg.contract.service.contract;

import org.web3j.abi.datatypes.Array;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes16;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.common.HexUtil;
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
    public SimplePromise<IPurchaseContract> deployContract(final BigInteger value, final String title, final String description, final List<String> imageSignatures, final boolean verifyIdentity)
    {
        return Async.toPromise(new Callable<IPurchaseContract>() {
            @Override
            public IPurchaseContract call() throws Exception {
                List<Type> typeList = new ArrayList<>();
                typeList.add(new Utf8String(title));
                typeList.add(new Utf8String(description));
                typeList.add(new Bool(verifyIdentity));

                final List<Bytes32> sigList = new ArrayList<>();
                for(String imgSig : imageSignatures)
                {
                    byte[] bytes = HexUtil.hexStringToByteArray(imgSig);
                    sigList.add(new Bytes32(bytes));
                }

                if(sigList.size() > 0)
                {
                    typeList.add(new DynamicArray(sigList));
                }else{
                    typeList.add(DynamicArray.empty(Bytes32.TYPE_NAME));
                }

                IPurchaseContract contract = PurchaseContract.deployContract(
                        web3,
                        transactionManager,
                        gasPrice,
                        gasLimit,
                        "0x606060405260405162000d8f38038062000d8f833981016040528080518201919060200180518201919060200180519060200190919080518201919050505b838383835b33600360006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508360009080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10620000d257805160ff191683800117855562000103565b8280016001018555821562000103579182015b8281111562000102578251825591602001919060010190620000e5565b5b5090506200012b91905b80821115620001275760008160009055506001016200010d565b5090565b505081600460146101000a81548160ff0219169083151502179055508260019080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106200019557805160ff1916838001178555620001c6565b82800160010185558215620001c6579182015b82811115620001c5578251825591602001919060010190620001a8565b5b509050620001ee91905b80821115620001ea576000816000905550600101620001d0565b5090565b50506000600660006101000a81548160ff0219169083600281116200000057021790555062000231816200026764010000000002620009a7176401000000009004565b5b505050506002348115620000005704600281905550346002546002021415156200025c5762000000565b5b505050506200030d565b6000600090505b8151811015620003085760058054806001018281815481835581811511620002c557818360005260206000209182019101620002c491905b80821115620002c0576000816000905550600101620002a6565b5090565b5b505050916000526020600020900160005b84848151811015620000005790602001906020020151909190915090600019169055505b80806001019150506200026e565b5b5050565b610a72806200031d6000396000f300606060405236156100ad576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806308551a53146100b257806335a063b4146101015780633fa4f245146101105780634a79d50c146101335780637150d8ae146101c95780637284e4161461021857806373fac6f0146102ae5780639a078f2c146102bd578063c19d93fb1461032f578063d69606971461035d578063e8731c3214610367575b610000565b34610000576100bf61038e565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b346100005761010e6103b4565b005b346100005761011d610511565b6040518082815260200191505060405180910390f35b3461000057610140610517565b604051808060200182810382528381815181526020019150805190602001908083836000831461018f575b80518252602083111561018f5760208201915060208101905060208303925061016b565b505050905090810190601f1680156101bb5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34610000576101d66105b5565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34610000576102256105db565b6040518080602001828103825283818151815260200191508051906020019080838360008314610274575b80518252602083111561027457602082019150602081019050602083039250610250565b505050905090810190601f1680156102a05780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34610000576102bb610679565b005b34610000576102ca61083a565b604051808060200182810382528381815181526020019150805190602001906020028083836000831461031c575b80518252602083111561031c576020820191506020810190506020830392506102f8565b5050509050019250505060405180910390f35b346100005761033c6108a6565b6040518082600281116100005760ff16815260200191505060405180910390f35b6103656108b9565b005b3461000057610374610994565b604051808215151515815260200191505060405180910390f35b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561041057610000565b6000806002811161000057600660009054906101000a900460ff16600281116100005714151561043f57610000565b7f80b62b7017bb13cf105e22749ee2a06a417ffba8c7f57b665057e0f3c2e925d960405180905060405180910390a16002600660006101000a81548160ff021916908360028111610000570217905550600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc3073ffffffffffffffffffffffffffffffffffffffff16319081150290604051809050600060405180830381858888f19350505050151561050b57610000565b5b5b505b565b60025481565b60008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156105ad5780601f10610582576101008083540402835291602001916105ad565b820191906000526020600020905b81548152906001019060200180831161059057829003601f168201915b505050505081565b600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156106715780601f1061064657610100808354040283529160200191610671565b820191906000526020600020905b81548152906001019060200180831161065457829003601f168201915b505050505081565b600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415156106d557610000565b6001806002811161000057600660009054906101000a900460ff16600281116100005714151561070457610000565b7f64ea507aa320f07ae13c28b5e9bf6b4833ab544315f5f2aa67308e21c252d47d60405180905060405180910390a16002600660006101000a81548160ff021916908360028111610000570217905550600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc6002549081150290604051809050600060405180830381858888f19350505050158061082a5750600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc3073ffffffffffffffffffffffffffffffffffffffff16319081150290604051809050600060405180830381858888f19350505050155b1561083457610000565b5b5b505b565b6020604051908101604052806000815250600580548060200260200160405190810160405280929190818152602001828054801561089b57602002820191906000526020600020905b81546000191681526020019060010190808311610883575b505050505090505b90565b600660009054906101000a900460ff1681565b6000806002811161000057600660009054906101000a900460ff1660028111610000571415156108e857610000565b60025460020234148015156108fc57610000565b7f764326667cab2f2f13cad5f7b7665c704653bd1acc250dcb7b422bce726896b460405180905060405180910390a133600460006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506001600660006101000a81548160ff0219169083600281116100005702179055505b5b505b50565b600460149054906101000a900460ff1681565b6000600090505b8151811015610a415760058054806001018281815481835581811511610a00578183600052602060002091820191016109ff91905b808211156109fb5760008160009055506001016109e3565b5090565b5b505050916000526020600020900160005b848481518110156100005790602001906020020151909190915090600019169055505b80806001019150506109ae565b5b50505600a165627a7a7230582085dfb82ef82b69e7a1b7937fa16f15e7867625a9ebad44be028aff8127d8d3b30029",
                        value,
                        typeList.toArray(new Type[typeList.size()])).get();

               // contract.setImageSignatures(imageSignatures).get();

                List<String> imagesSigs = contract.getImageSignatures().get();
                return contract;
            }
        })
;
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

                    if(contractInfo.getImages() != null)
                    {
                        for(String key : contractInfo.getImages().keySet())
                            contract.addImage(key, contractInfo.getImages().get(key));
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
        contractManager.saveContract(new ContractInfo(contract.state().get(), contract.getContractAddress(), contract.getUserProfile(), contract.getImages()), account);
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
