package ch.uzh.ifi.csg.contract.service.contract;

import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.util.HexUtil;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.contract.PurchaseContract;
import ch.uzh.ifi.csg.contract.contract.RentContract;
import ch.uzh.ifi.csg.contract.contract.TimeUnit;
import ch.uzh.ifi.csg.contract.contract.TradeContract;
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
     * Deploys a purchase contract on the blockchain using the TradeContract.deployPurchaseContract
     * factory method.
     *
     * @param value
     * @param title
     * @param description
     * @return  a promise representing the result of the call.
     */
    @Override
    public SimplePromise<ITradeContract> deployPurchaseContract(final BigInteger value, final String title, final String description, final List<String> imageSignatures, final boolean verifyIdentity)
    {
        List<Type> typeList = buildTypeList(title, description, imageSignatures, verifyIdentity);
        return deploy(PurchaseContract.class, PurchaseContract.BINARY, typeList, value);
    }

    /**
     * Deploys a rent contract on the blockchain using the TradeContract.deployPurchaseContract
     * factory method.
     *
     * @param title
     * @param description
     * @return  a promise representing the result of the call.
     */
    @Override
    public SimplePromise<ITradeContract> deployRentContract(final BigInteger price, final BigInteger deposit, final TimeUnit timeUnit, final String title, final String description, final List<String> imageSignatures, final boolean verifyIdentity) {

        List<Type> typeList = buildTypeList(title, description, imageSignatures, verifyIdentity);
        typeList.add(new Uint256(deposit));
        typeList.add(new Uint256(price));
        typeList.add(new Uint8(BigInteger.valueOf(timeUnit.ordinal())));

        return deploy(RentContract.class, RentContract.BINARY, typeList, BigInteger.ZERO);
    }

    private List<Type> buildTypeList(final String title, final String description, final List<String> imageSignatures, final boolean verifyIdentity)
    {
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

        return typeList;
    }

    private SimplePromise<ITradeContract> deploy(Class<? extends TradeContract> clazz, String binary, List<Type> paramList, BigInteger value)
    {
        return TradeContract.deployContractAsync(
                clazz,
                web3,
                transactionManager,
                gasPrice,
                gasLimit,
                binary,
                value,
                paramList.toArray(new Type[paramList.size()]));
    }

    /**
     * Loads a contract from the blockchain specified by the provided address.
     *
     * @param contractAddress
     * @return a promise representing the result of the call.
     */
    @Override
    public SimplePromise<ITradeContract> loadContract(final ContractType contractType, final String contractAddress, final String account)
    {
        SimplePromise<ITradeContract> contractPromise = loadAsync(contractType, contractAddress);

        contractPromise.then(new DoneCallback<ITradeContract>() {
            @Override
            public void onDone(ITradeContract contract) {
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
                    contractManager.saveContract(new ContractInfo(contractType, contractAddress), account);
                }
            }
        });

        return contractPromise;
    }

    private ITradeContract load(final ContractType contractType, final String contractAddress) throws Exception
    {
        ITradeContract contract;
        switch(contractType)
        {
            case Purchase:
                contract = TradeContract.loadContract(PurchaseContract.class, contractAddress, web3, transactionManager, gasPrice, gasLimit);
                break;
            case Rent:
                contract = TradeContract.loadContract(RentContract.class, contractAddress, web3, transactionManager, gasPrice, gasLimit);
                break;
            default:
                throw new IllegalArgumentException("The contract type " + contractType.toString() + "is not supported!");
        }

        return contract;
    }

    private SimplePromise<ITradeContract> loadAsync(final ContractType contractType, final String contractAddress)
    {
        SimplePromise<ITradeContract> contractPromise;
        switch(contractType)
        {
            case Purchase:
                contractPromise = TradeContract.loadContractAsync(PurchaseContract.class, contractAddress, web3, transactionManager, gasPrice, gasLimit);
                break;
            case Rent:
                contractPromise = TradeContract.loadContractAsync(RentContract.class, contractAddress, web3, transactionManager, gasPrice, gasLimit);
                break;
            default:
                throw new IllegalArgumentException("The contract type " + contractType.toString() + "is not supported!");
        }

        return contractPromise;
    }

    /**
     * Persists a contract for an account with the provided ContractManager.
     *
     * @param contract
     * @param account
     */
    @Override
    public void saveContract(ITradeContract contract, String account) {
        saveContract(contract.getContractAddress(), contract.getContractType(), account);
    }

    /**
     * Persists a contract for an account with the provided ContractManager.
     *
     * @param contractAddress
     * @param contractType
     * @param account
     */
    @Override
    public void saveContract(String contractAddress, ContractType contractType,  String account) {
        contractManager.saveContract(
                new ContractInfo(contractType,  contractAddress),
                account);
    }

    /**
     * Removes a contract from a persistent storage. This method does not delete a contract from the
     * blockchain.
     *
     * @param contract
     * @param account
     */
    @Override
    public void removeContract(ITradeContract contract, String account) {
        removeContract(contract.getContractAddress(), account);
    }


    /**
     * Removes a contract from a persistent storage. This method does not delete a contract from the
     * blockchain.
     *
     * @param contractAddress
     * @param account
     */
    @Override
    public void removeContract(String contractAddress, String account) {
        contractManager.deleteContract(contractAddress, account);
    }

    /**
     * Loads all persisted contracts for the specified account.
     *
     * @param account
     * @return  a promise representing the result of the call.
     */
    @Override
    public SimplePromise<List<ITradeContract>> loadContracts(final String account) {

        final List<ContractInfo> contractInfos = contractManager.getContracts(account);

        return Async.toPromise(new Callable<List<ITradeContract>>() {
            @Override
            public List<ITradeContract> call() throws Exception {
                final List<ITradeContract> contractList = new ArrayList(contractInfos.size());
                for(ContractInfo info : contractInfos)
                {
                    contractList.add(load(info.getContractType(), info.getContractAddress()));
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
