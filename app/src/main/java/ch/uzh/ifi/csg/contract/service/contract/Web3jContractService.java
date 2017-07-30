package ch.uzh.ifi.csg.contract.service.contract;

import org.spongycastle.util.encoders.Hex;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.util.BinaryUtil;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.contract.PurchaseContract;
import ch.uzh.ifi.csg.contract.contract.RentContract;
import ch.uzh.ifi.csg.contract.contract.TimeUnit;
import ch.uzh.ifi.csg.contract.contract.TradeContract;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.util.Web3Util;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionHandler;

/**
 * Web3j implementation of the ContractService.
 */

public class Web3jContractService implements ContractService
{
    private final Web3j web3;
    private final TransactionManager transactionManager;
    private TransactionHandler transactionHandler;
    private final BigInteger gasPrice;
    private final BigInteger gasLimit;
    private final ContractManager contractManager;
    private String accountAddress;

    /**
     *
     * @param web3: the rpc client
     * @param transactionManager: the transaction manager used by the deployed contracts to perform transactions
     * @param contractManager: ContractManager implementation used to persist created contracts for an account
     * @param gasPrice
     * @param gasLimit
     */
    public Web3jContractService(Web3j web3, TransactionManager transactionManager, ContractManager contractManager, TransactionHandler transactionHandler, BigInteger gasPrice, BigInteger gasLimit, String accountAddress)
    {
        this.web3 = web3;
        this.transactionManager = transactionManager;
        this.transactionHandler = transactionHandler;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.contractManager = contractManager;
        this.accountAddress = accountAddress;
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
    public SimplePromise<ITradeContract> deployPurchaseContract(final BigInteger value, final String title, final String description, final Map<String, String> imageSignatures, final boolean verifyIdentity, final boolean lightDeployment)
    {
        final String contractAddress = calculateContractAddress(accountAddress).get();
        final ContractInfo info = new ContractInfo(ContractType.Purchase, contractAddress, title, description, verifyIdentity, lightDeployment);
        info.setImages(imageSignatures);

        SimplePromise<ITradeContract> promise = Async.toPromise(new Callable<ITradeContract>()
        {
            @Override
            public ITradeContract call() throws Exception {

                if(lightDeployment)
                {
                    List<Type> typeList = new ArrayList<>();
                    byte[] bytes = BinaryUtil.hexStringToByteArray(info.getContentHash());
                    typeList.add(new Bytes32(bytes));
                    return deploy(PurchaseContract.class, PurchaseContract.BINARY_LIGHT, info, typeList, value);

                }else{
                    List<Type> typeList = buildTypeList(title, description, imageSignatures.keySet(), verifyIdentity);
                    ITradeContract contract = deploy(PurchaseContract.class, PurchaseContract.BINARY_FULL, info, typeList, value);
                    return contract;
                }
            }
        });

        transactionHandler.toDeployTransaction(promise, info, accountAddress, this);
        return promise;
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
    public SimplePromise<ITradeContract> deployRentContract(
            final BigInteger price,
            final BigInteger deposit,
            final TimeUnit timeUnit,
            final String title,
            final String description,
            final Map<String, String> imageSignatures,
            final boolean verifyIdentity,
            final boolean lightDeployment)
    {
        final String contractAddress = calculateContractAddress(accountAddress).get();
        final ContractInfo contractInfo = new ContractInfo(ContractType.Rent, contractAddress, title, description, verifyIdentity, lightDeployment);
        contractInfo.setImages(imageSignatures);

        SimplePromise<ITradeContract> promise = Async.toPromise(new Callable<ITradeContract>() {
            @Override
            public ITradeContract call() throws Exception {

                if(lightDeployment)
                {
                    List<Type> typeList = new ArrayList<>();
                    typeList.add(new Uint256(deposit));
                    typeList.add(new Uint256(price));
                    byte[] bytes = BinaryUtil.hexStringToByteArray(contractInfo.getContentHash());
                    typeList.add(new Bytes32(bytes));
                    return deploy(RentContract.class, RentContract.BINARY_LIGHT, contractInfo, typeList, BigInteger.ZERO);

                }else{
                    List<Type> typeList = buildTypeList(title, description, imageSignatures.keySet(), verifyIdentity);
                    typeList.add(new Uint256(deposit));
                    typeList.add(new Uint256(price));
                    return deploy(RentContract.class, RentContract.BINARY_FULL, contractInfo, typeList, BigInteger.ZERO);
                }
            }
        });

        transactionHandler.toDeployTransaction(promise, contractInfo, accountAddress, this);
        return promise;
    }

    private List<Type> buildTypeList(final String title, final String description, final Set<String> imageSignatures, final boolean verifyIdentity)
    {
        List<Type> typeList = new ArrayList<>();
        typeList.add(new Utf8String(title));
        typeList.add(new Utf8String(description));
        typeList.add(new Bool(verifyIdentity));

        final List<Bytes32> sigList = new ArrayList<>();
        for(String imgSig : imageSignatures)
        {
            byte[] bytes = BinaryUtil.hexStringToByteArray(imgSig);
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

    private ITradeContract deploy(Class<? extends TradeContract> clazz, String binary, ContractInfo contractInfo, List<Type> paramList, BigInteger value) throws Exception {
        return TradeContract.deployContract(
                clazz,
                web3,
                transactionManager,
                gasPrice,
                gasLimit,
                contractInfo,
                binary,
                value,
                paramList.toArray(new Type[paramList.size()]));
    }

    /**
     * Loads a contract from the blockchain specified by the provided accountAddress.
     *
     * @param contractAddress
     * @return a promise representing the result of the call.
     */
    @Override
    public SimplePromise<ITradeContract> loadContract(final ContractType contractType, final String contractAddress, final String account)
    {
        return Async.toPromise(new Callable<ITradeContract>() {
            @Override
            public ITradeContract call() throws Exception {
                ContractInfo contractInfo = contractManager.getContract(contractAddress, account);
                ITradeContract contract = load(contractInfo);
                if(contractInfo.getUserProfile() != null)
                {
                    contract.setUserProfile(contractInfo.getUserProfile());
                }

                if(contractInfo.getImages() != null)
                {
                    for(String key : contractInfo.getImages().keySet())
                        contract.addImage(key, contractInfo.getImages().get(key));
                }

                return contract;
            }
        });
    }

    private ITradeContract load(final ContractInfo contractInfo) throws Exception
    {
        ITradeContract contract;
        switch(contractInfo.getContractType())
        {
            case Purchase:
                contract = TradeContract.loadContract(PurchaseContract.class, contractInfo.getContractAddress(), web3, transactionManager, gasPrice, gasLimit, contractInfo);
                break;
            case Rent:
                contract = TradeContract.loadContract(RentContract.class, contractInfo.getContractAddress(), web3, transactionManager, gasPrice, gasLimit, contractInfo);
                break;
            default:
                throw new IllegalArgumentException("The contract type " + contractInfo.getContractType().toString() + "is not supported!");
        }

        return contract;
    }

    /**
     * Persists a contract for an account with the provided ContractManager.
     *
     * @param contract
     * @param account
     */
    @Override
    public void saveContract(ITradeContract contract, String account) {
        ContractInfo info = new ContractInfo(contract.getContractType(), contract.getContractAddress(), contract.getTitle().get(), contract.getDescription().get(), contract.getVerifyIdentity().get(), contract.isLightContract());
        info.setUserProfile(contract.getUserProfile());
        info.setImages(contract.getImages());

        contractManager.saveContract(
                info,
                account);
    }

    /**
     * Persists a contract for an account with the provided ContractManager.
     *
     * @param contractInfo
     * @param account
     */
    @Override
    public void saveContract(ContractInfo contractInfo,  String account) {
        contractManager.saveContract(
                contractInfo,
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
                    contractList.add(load(info));
                }
                return contractList;
            }
        });
    }

    @Override
    public SimplePromise<Boolean> verifyContractCode(final String address, final String binary) {
        return Async.toPromise(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                EthGetCode response = web3.ethGetCode(address, DefaultBlockParameterName.LATEST).send();
                if(response.hasError())
                    return false;

                return response.getCode().equals(binary);
            }
        });
    }

    private SimplePromise<String> calculateContractAddress(final String senderAddress)
    {
        return Async.toPromise(new Callable<String>() {
            @Override
            public String call() throws Exception {
                BigInteger nonce = getNonce(senderAddress).get();
                RlpString rlpNonce = RlpString.create(nonce);
                RlpString rlpAddress = RlpString.create(Web3Util.normalizeAddress(senderAddress));
                byte[] encodedList = RlpEncoder.encode(new RlpList(rlpAddress, rlpNonce));
                byte[] addressHash = Hash.sha3(encodedList);
                String addressString = Hex.toHexString(addressHash);
                return Numeric.prependHexPrefix(addressString.substring(24));
            }
        });
    }

    private SimplePromise<BigInteger> getNonce(final String address)
    {
        return Async.toPromise(new Callable<BigInteger>() {
            @Override
            public BigInteger call() throws Exception {
                EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(
                        address, DefaultBlockParameterName.PENDING).send();

                return ethGetTransactionCount.getTransactionCount();
            }
        });
    }
}
