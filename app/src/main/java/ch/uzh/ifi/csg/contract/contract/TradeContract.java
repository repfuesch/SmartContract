package ch.uzh.ifi.csg.contract.contract;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionTimeoutException;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import ch.uzh.ifi.csg.contract.service.serialization.SerializationService;
import ch.uzh.ifi.csg.contract.util.BinaryUtil;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import rx.Subscription;
import rx.functions.Action1;

/**
 * This class implements the ITradeContract interface and is derived rom the Contract base class
 * of Web3j.
 */

public abstract class TradeContract extends Contract implements ITradeContract
{
    private List<IContractObserver> observers;
    private List<Subscription> subscriptions;
    private UserProfile userProfile;
    private Map<String, String> images;
    private ContractInfo contractInfo;
    private BigInteger deposit;
    private BigInteger price;

    protected TradeContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit)
    {
        super(contractAddress, web3j, transactionManager, gasPrice, gasLimit);

        observers = new ArrayList<>();
        subscriptions = new ArrayList<>();
        userProfile = new UserProfile();
        images = new HashMap<>();
    }

    /**
     * Setts the ContractInfo object for this contract.
     * This method is always called after the constructor.
     *
     * @param contractInfo
     */
    public void initContract(ContractInfo contractInfo)
    {
        this.contractInfo = contractInfo;
    }

    public static <T extends TradeContract>  SimplePromise<ITradeContract> deployContractAsync(
            final Class<T> clazz,
            final Web3j web3j,
            final TransactionManager transactionManager,
            final BigInteger gasPrice,
            final BigInteger gasLimit,
            final ContractInfo contractInfo,
            final String binary,
            final BigInteger value,
            final Type... args)
    {
        return Async.toPromise(new Callable<ITradeContract>() {
            @Override
            public ITradeContract call() throws Exception {
                return deployContract(clazz, web3j, transactionManager, gasPrice, gasLimit, contractInfo, binary, value, args);
            }
        });
    }

    /**
     *  Creates a smart contract with the provided binary code, arguments and value on the blockchain.
     *  If the creation succeeds, a Java wrapper class instance is created that implements all
     *  interaction logic to access smart contract fields and execute transactions.
     *
     * @param clazz The java wrapper class for this contract. Must be derived from TradeContract.
     * @param web3j The Web3j client object
     * @param transactionManager The TransactionHandler used for signing and executing transactions
     *                           on the contract on the blockchain.
     * @param gasPrice  The gas price used by the TransactionHandler when executing transactions
     * @param gasLimit The gas limit used by the TransactionHandler when executing transactions
     * @param contractInfo Object that contains all information that is stored locally
     * @param binary The binary contract code of the smart contract deployed on the blockchain
     * @param value The value in wei that is sent in the transaction that creates this contract
     * @param args Web3j Type array that contains all arguments of the smart contract constructor
     * @param <T> The concrete java wrapper class
     * @return The Java wrapper class that
     * @throws Exception
     */
    public static <T extends TradeContract>  ITradeContract deployContract(
            final Class<T> clazz,
            final Web3j web3j,
            final TransactionManager transactionManager,
            final BigInteger gasPrice,
            final BigInteger gasLimit,
            final ContractInfo contractInfo,
            final String binary,
            final BigInteger value,
            final Type... args) throws Exception
    {
            String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.asList(args));
            TradeContract contract = deploy(clazz, web3j, transactionManager, gasPrice, gasLimit, binary, encodedConstructor, value);
            contract.initContract(contractInfo);
            return contract;
    }

    /**
     * Creates and initializes a Java wrapper class derived from TradeContract that contains
     * the interaction logic for the smart contract on the provided contract address.
     *
     * @param clazz The java wrapper class for this contract. Must be derived from TradeContract.
     * @param contractAddress The Ethereum address that contains the smart contract code
     * @param web3j The Web3j client object
     * @param transactionManager The TransactionHandler used for signing and executing transactions
     *                           on the contract on the blockchain.
     * @param gasPrice  The gas price used by the TransactionHandler when executing transactions
     * @param gasLimit The gas limit used by the TransactionHandler when executing transactions
     * @param contractInfo Object that contains all information of a contract that is stored locally
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T extends TradeContract> ITradeContract loadContract(
            final Class<T> clazz,
            final String contractAddress,
            final Web3j web3j,
            final TransactionManager transactionManager,
            final BigInteger gasPrice,
            final BigInteger gasLimit,
            final ContractInfo contractInfo) throws Exception
    {
            Constructor<T> constructor = clazz.getDeclaredConstructor(
                    String.class, Web3j.class, TransactionManager.class, BigInteger.class, BigInteger.class);
            constructor.setAccessible(true);

            TradeContract contract = constructor.newInstance(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
            contract.initContract(contractInfo);
            return contract;
    }

    public static <T extends TradeContract> SimplePromise<ITradeContract> loadContractAsync(
            final Class<T> clazz,
            final String contractAddress,
            final Web3j web3j,
            final TransactionManager transactionManager,
            final BigInteger gasPrice,
            final BigInteger gasLimit,
            final ContractInfo contractInfo)
    {
        return Async.toPromise(
                new Callable<ITradeContract>() {
                    @Override
                    public ITradeContract call() throws Exception {
                    return loadContract(clazz, contractAddress, web3j, transactionManager, gasPrice, gasLimit, contractInfo);
                    }
                });
    }

    protected List<IContractObserver> getObservers() {
        return observers;
    }

    /**
     * Registers the aborted event of the contract
     */
    protected void registerContractEvents()
    {
        Event event = new Event("aborted", new ArrayList<TypeReference<?>>(), new ArrayList<TypeReference<?>>());
        registerEvent(event);
    }

    /**
     * Unregisters all events that are registered for this contract
     */
    protected void unregisterContractEvents()
    {
        for(final Subscription subscription : subscriptions)
        {
            Async.run(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    subscription.unsubscribe();
                    return null;
                }
            });
        }

        subscriptions.clear();
    }

    /**
     * Registers the provided event.
     *
     * @param event the Event to register
     */
    protected void registerEvent(final Event event)
    {
        String encodedEventSignature = EventEncoder.encode(event);
        final EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST, getContractAddress()).addSingleTopic(encodedEventSignature);

        Async.toPromise(new Callable<Subscription>() {

            @Override
            public Subscription call() throws Exception {
                return web3j.ethLogObservable(filter).subscribe(
                        new Action1<Log>() {
                            @Override
                            public void call(Log log) {
                                notifyObservers(event.getName(), null);
                            }
                        });
            }
        }).done(new DoneCallback<Subscription>() {
            @Override
            public void onDone(Subscription result) {
                subscriptions.add(result);
            }
        });
    }

    private void notifyObservers(String event, Object value) {
        for (IContractObserver observer : getObservers())
        {
            observer.contractStateChanged(event, value);
        }
    }

    @Override
    public void addObserver(IContractObserver observer)
    {
        if(getObservers().size() == 0)
            registerContractEvents();

        getObservers().add(observer);
    }

    @Override
    public void removeObserver(IContractObserver observer) {

        getObservers().remove(observer);
        if(getObservers().size() == 0)
            unregisterContractEvents();
    }

    @Override
    public UserProfile getUserProfile() {
        return userProfile;
    }

    @Override
    public void setUserProfile(UserProfile profile) {
        userProfile = profile;
    }

    @Override
    public void addImage(String signature, String filename)
    {
        images.put(signature, filename);
    }

    @Override
    public Map<String, String> getImages() {
        return images;
    }

    @Override
    public boolean isLightContract()
    {
        return contractInfo.isLightContract();
    }

    @Override
    public String toJson()
    {
        SerializationService serializationService = new GsonSerializationService();
        return serializationService.serialize(contractInfo);
    }

    @Override
    public SimplePromise<String> abort()
    {
        return Async.toPromise(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Function function = new Function("abort", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
                TransactionReceipt result = executeTransaction(function);
                return result.getTransactionHash();
            }
        });
    }

    @Override
    public SimplePromise<BigInteger> getPrice()
    {
        return Async.toPromise(new Callable<BigInteger>() {
            @Override
            public BigInteger call() throws Exception {

                if(price != null)
                    return price;

                Function function = new Function("price",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                        }));
                Uint256 result = executeCallSingleValueReturn(function);
                price = result.getValue();
                return price;
            }
        });
    }

    @Override
    public SimplePromise<String> getSeller()
    {
        return Async.toPromise(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Function function = new Function("seller",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                        }));
                Address result = executeCallSingleValueReturn(function);
                return result.toString();
            }
        });
    }

    @Override
    public SimplePromise<String> getBuyer() {

        return Async.toPromise(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Function function = new Function("buyer",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {
                        }));
                Address result = executeCallSingleValueReturn(function);
                return result.toString();
            }
        });
    }

    @Override
    public SimplePromise<String> getTitle() {

        return Async.toPromise(new Callable<String>() {
            @Override
            public String call() throws Exception {

                if(contractInfo.isLightContract())
                    return contractInfo.getTitle();

                Function function = new Function("title",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                        }));
                Utf8String result = executeCallSingleValueReturn(function);
                return result.toString();
            }
        });
    }

    @Override
    public SimplePromise<String> getDescription() {

        return Async.toPromise(new Callable<String>() {
            @Override
            public String call() throws Exception {

                if(contractInfo.isLightContract())
                    return contractInfo.getDescription();

                Function function = new Function("description",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                        }));
                Utf8String result = executeCallSingleValueReturn(function);
                return result.toString();
            }
        });
    }

    @Override
    public SimplePromise<ContractState> getState() {

        return Async.toPromise(new Callable<ContractState>() {
            @Override
            public ContractState call() throws Exception {
                Function function = new Function("state",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {
                        }));
                Uint8 value = executeCallSingleValueReturn(function);
                BigInteger bigValue = value.getValue();
                return ContractState.valueOf(bigValue.intValue());
            }
        });
    }

    @Override
    public SimplePromise<BigInteger> getDeposit() {

        return Async.toPromise(new Callable<BigInteger>() {
            @Override
            public BigInteger call() throws Exception {

                if(deposit != null)
                    return deposit;

                Function function = new Function("deposit",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                        }));
                Uint256 value = executeCallSingleValueReturn(function);
                BigInteger bigValue = value.getValue();
                deposit = bigValue;
                return bigValue;
            }
        });
    }

    @Override
    public SimplePromise<Boolean> getVerifyIdentity() {

        return Async.toPromise(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                if(contractInfo.isLightContract())
                    return contractInfo.isVerifyIdentity();

                Function function = new Function("verifyIdentity",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                        }));
                Bool result = executeCallSingleValueReturn(function);
                return result.getValue();
            }
        });
    }

    @Override
    public SimplePromise<List<String>> getImageSignatures()
    {
        return Async.toPromise(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {

                if(contractInfo.isLightContract())
                    return new ArrayList<>(contractInfo.getImages().keySet());

                Function function = new Function("getImageSignatures",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Bytes32>>() {
                        }));

                List<String> signatures = new ArrayList<String>();
                DynamicArray<Bytes32> result = executeCallSingleValueReturn(function);
                for(Bytes32 bytes : result.getValue())
                {
                    signatures.add(new String(BinaryUtil.byteArrayToHexString(bytes.getValue())));
                }

                return signatures;
            }
        });
    }

    @Override
    public SimplePromise<String> getContentHash()
    {
        return Async.toPromise(new Callable<String>() {
            @Override
            public String call() throws Exception {

                if(contractInfo.isLightContract())
                    return contractInfo.getContentHash();

                Function function = new Function("getImageSignatures",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Bytes32>>() {
                        }));

                return null;
            }
        });
    }

    @Override
    public SimplePromise<Boolean> verifyContent()
    {
        return Async.toPromise(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception
            {
                if(!contractInfo.isLightContract())
                    return true;

                String remoteContent = getContentHash().get();
                String localContent = contractInfo.getContentHash();
                return remoteContent.equals(localContent);
            }
        });

    }
}
