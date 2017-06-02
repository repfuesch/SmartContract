package ch.uzh.ifi.csg.contract.contract;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Array;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticArray;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes16;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.MessageDecodingException;
import org.web3j.protocol.exceptions.TransactionTimeoutException;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;

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
import java.util.concurrent.Future;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.common.HexUtil;
import ch.uzh.ifi.csg.contract.event.IContractObserver;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import rx.Subscription;
import rx.functions.Action1;

public class PurchaseContract extends Contract implements IPurchaseContract {

    private List<IContractObserver> observers;
    private List<Subscription> subscriptions;
    private UserProfile userProfile;
    private Map<String, String> images;

    private PurchaseContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit)
    {
        super(contractAddress, web3j, transactionManager, gasPrice, gasLimit);

        observers = new ArrayList<>();
        subscriptions = new ArrayList<>();
        userProfile = new UserProfile();
        images = new HashMap<>();
    }

    public static SimplePromise<IPurchaseContract> deployContract(
            final Web3j web3j,
            final TransactionManager transactionManager,
            final BigInteger gasPrice,
            final BigInteger gasLimit,
            final String binary,
            final BigInteger value,
            final Type... args) {
        return Async.toPromise(new Callable<IPurchaseContract>() {
            @Override
            public IPurchaseContract call() throws Exception {
                String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.asList(args));
                PurchaseContract purchase = deploy(PurchaseContract.class, web3j, transactionManager, gasPrice, gasLimit, binary, encodedConstructor, value);
                return purchase;
            }
        });
    }

    public static SimplePromise<IPurchaseContract> loadContract(
            final String contractAddress,
            final Web3j web3j,
            final TransactionManager transactionManager,
            final BigInteger gasPrice,
            final BigInteger gasLimit)
    {
        return Async.toPromise(
                new Callable<IPurchaseContract>() {
                    @Override
                    public IPurchaseContract call() throws Exception {
                        Constructor<PurchaseContract> constructor = PurchaseContract.class.getDeclaredConstructor(
                                String.class, Web3j.class, TransactionManager.class, BigInteger.class, BigInteger.class);
                        constructor.setAccessible(true);

                        PurchaseContract contract = constructor.newInstance(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
                        return contract;
                    }
                });
    }

    protected TransactionReceipt executeTransaction(Function function, BigInteger value) throws InterruptedException,
            ExecutionException, TransactionTimeoutException {
        return executeTransaction(FunctionEncoder.encode(function), value);
    }


    protected List<IContractObserver> getObservers() {
        return observers;
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
    public SimplePromise<List<String>> getImageSignatures() {
        return Async.toPromise(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                Function function = new Function("getImageSignatures",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Bytes32>>() {
                        }));

                List<String> signatures = new ArrayList<String>();
                DynamicArray<Bytes32> result = executeCallSingleValueReturn(function);
                for(Bytes32 bytes : result.getValue())
                {
                    signatures.add(new String(HexUtil.byteArrayToHexString(bytes.getValue())));
                }

                return signatures;
            }
        });
    }

    public SimplePromise<String> setImageSignatures(List<String> imageSignatures)
    {
        final List<Bytes32> sigList = new ArrayList<>();
        for(String imgSig : imageSignatures)
        {
            byte[] bytes = HexUtil.hexStringToByteArray(imgSig);
            sigList.add(new Bytes32(bytes));
        }

        return Async.toPromise(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Function function = new Function("addImageSignatures",
                        Arrays.<Type>asList(new DynamicArray(sigList)),
                        Collections.<TypeReference<?>>emptyList());

                TransactionReceipt result = executeTransaction(function);
                return result.getTransactionHash();
            }
        });
    }

    public SimplePromise<String> seller() {
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

    /**
     * Todo: Return transaction wrapper object instead of only hash of transaction
     */
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

    public SimplePromise<BigInteger> value() {
        return Async.toPromise(
                new Callable<BigInteger>() {
                    @Override
                    public BigInteger call() throws Exception {
                        Function function = new Function("value",
                                Arrays.<Type>asList(),
                                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                                }));
                        Uint256 result = executeCallSingleValueReturn(function);

                        return result.getValue();
                    }
                });
    }

    public SimplePromise<String> buyer() {
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

    public SimplePromise<String> title() {
        return Async.toPromise(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Function function = new Function("title",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                        }));
                Utf8String result = executeCallSingleValueReturn(function);
                return result.toString();
            }
        });
    }


    public SimplePromise<String> description() {
        return Async.toPromise(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Function function = new Function("description",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {
                        }));
                Utf8String result = executeCallSingleValueReturn(function);
                return result.toString();
            }
        });
    }

    public SimplePromise<String> confirmReceived() {
        return Async.toPromise(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Function function = new Function("confirmReceived", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
                TransactionReceipt result = executeTransaction(function);
                return result.getTransactionHash();
            }
        });
    }

    public SimplePromise<ContractState> state() {
        return Async.toPromise(
                new Callable<ContractState>() {
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

    public SimplePromise<String> confirmPurchase() {
        return Async.toPromise(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        Function function = new Function("confirmPurchase", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
                        try{
                            TransactionReceipt result = executeTransaction(function, value().get().multiply(BigInteger.valueOf(2)));
                            return result.getTransactionHash();
                        }catch(MessageDecodingException ex)
                        {
                            ex.printStackTrace();
                            return null;
                        }
                    }
                });
    }

    @Override
    public SimplePromise<Boolean> verifyIdentity() {
        return Async.toPromise(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Function function = new Function("verifyIdentity",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
                        }));
                Bool result = executeCallSingleValueReturn(function);
                return result.getValue();
            }
        });
    }

    protected void registerContractEvents()
    {
        Event event = new Event("purchaseConfirmed", new ArrayList<TypeReference<?>>(), new ArrayList<TypeReference<?>>());
        registerEvent(event);
        event = new Event("aborted", new ArrayList<TypeReference<?>>(), new ArrayList<TypeReference<?>>());
        registerEvent(event);
        event = new Event("itemReceived", new ArrayList<TypeReference<?>>(), new ArrayList<TypeReference<?>>());
        registerEvent(event);
    }

    protected void unregisterContractEvents()
    {
        for(Subscription subscription : subscriptions)
        {
            subscription.unsubscribe();
        }

        subscriptions.clear();
    }

    private void registerEvent(final Event event)
    {
        String encodedEventSignature = EventEncoder.encode(event);
        EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST, getContractAddress()).addSingleTopic(encodedEventSignature);

        Subscription subscription = web3j.ethLogObservable(filter).subscribe(
                new Action1<Log>() {
                    @Override
                    public void call(Log log) {
                        notifyObservers(event.getName(), null);
                    }
                });

        subscriptions.add(subscription);
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
}