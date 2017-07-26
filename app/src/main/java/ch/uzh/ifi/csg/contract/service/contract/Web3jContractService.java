package ch.uzh.ifi.csg.contract.service.contract;

import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
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
import ch.uzh.ifi.csg.contract.util.BinaryUtil;
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
    public SimplePromise<ITradeContract> deployPurchaseContract(final BigInteger value, final String title, final String description, final List<String> imageSignatures, final boolean verifyIdentity, boolean lightDeployment)
    {
        if(lightDeployment)
        {
            ContractInfo info = new ContractInfo(ContractType.Purchase, "", title, description, verifyIdentity, true);
            List<Type> typeList = new ArrayList<>();
            byte[] bytes = BinaryUtil.hexStringToByteArray(info.getContentHash());
            typeList.add(new Bytes32(bytes));
            info.setLightContract(true);
            return deploy(PurchaseContract.class, PurchaseContract.BINARY_LIGHT, info, typeList, value);

        }else{
            ContractInfo info = new ContractInfo(ContractType.Purchase, "", title, description, verifyIdentity, false);
            List<Type> typeList = buildTypeList(title, description, imageSignatures, verifyIdentity);
            return deploy(PurchaseContract.class, PurchaseContract.BINARY_FULL, info, typeList, value);
        }
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
            final List<String> imageSignatures,
            final boolean verifyIdentity,
            boolean lightDeployment)
    {


        if(lightDeployment)
        {
            ContractInfo contractInfo = new ContractInfo(ContractType.Rent, "", title, description, verifyIdentity, true);
            contractInfo.setLightContract(true);
            List<Type> typeList = new ArrayList<>();
            typeList.add(new Uint256(deposit));
            typeList.add(new Uint256(price));
            byte[] bytes = BinaryUtil.hexStringToByteArray(contractInfo.getContentHash());
            typeList.add(new Bytes32(bytes));
            return deploy(RentContract.class, RentContract.BINARY_LIGHT, contractInfo, typeList, BigInteger.ZERO);

        }else{
            ContractInfo contractInfo = new ContractInfo(ContractType.Rent, "", title, description, verifyIdentity, false);
            List<Type> typeList = buildTypeList(title, description, imageSignatures, verifyIdentity);
            typeList.add(new Uint256(deposit));
            typeList.add(new Uint256(price));
            return deploy(RentContract.class, RentContract.BINARY_FULL, contractInfo, typeList, BigInteger.ZERO);
        }
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

    private SimplePromise<ITradeContract> deploy(Class<? extends TradeContract> clazz, String binary, ContractInfo contractInfo, List<Type> paramList, BigInteger value)
    {
        return TradeContract.deployContractAsync(
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
     * Loads a contract from the blockchain specified by the provided address.
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
}
