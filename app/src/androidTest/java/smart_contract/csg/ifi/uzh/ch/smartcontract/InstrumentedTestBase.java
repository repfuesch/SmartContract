package smart_contract.csg.ifi.uzh.ch.smartcontract;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import ch.uzh.ifi.csg.contract.util.Web3Util;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.contract.IRentContract;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import ezvcard.VCard;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Address;
import ezvcard.property.StructuredName;
import smart_contract.csg.ifi.uzh.ch.smartcontract.mocks.TestAppContext;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base class for instrumented unit-tests. Sets up test contracts and a test profile
 */
public abstract class InstrumentedTestBase {

    protected static IPurchaseContract purchaseContract;
    protected static IRentContract rentContract;
    protected static List<ITradeContract> contractList;
    protected static UserProfile testProfile;

    protected final static String selectedAccount = "account";
    protected final static BigDecimal exchangeRate = new BigDecimal("200");

    protected TestAppContext context;

    static { initTestContracts(); initTestProfileCard(); }

    /**
     * Sets up a fake {@link UserProfile}
     */
    private static void initTestProfileCard()
    {
        VCard testProfileCard = new VCard();

        StructuredName name = new StructuredName();
        name.setFamily("Muster");
        name.setGiven("Hans");

        Address address = new Address();
        address.setStreetAddress("Musterstrasse 12");
        address.setLocality("Musterhausen");
        address.setPostalCode("3333");
        address.setCountry("Switzerland");
        address.setRegion("Lucerne");

        testProfileCard.addAddress(address);
        testProfileCard.setStructuredName(name);
        testProfileCard.addEmail("max.muster@example.com");
        testProfileCard.addTelephoneNumber("+41795821457", TelephoneType.HOME);

        testProfile = new UserProfile();
        testProfile.setVCard(testProfileCard);
    }

    /**
     * Fakes the list of stored {@link ITradeContract} instances
     */
    private static void initTestContracts()
    {
        try{
            //fake purchase contract
            purchaseContract = mock(IPurchaseContract.class);
            when(purchaseContract.getContractType()).thenReturn(ContractType.Purchase);
            when(purchaseContract.getContractAddress()).thenReturn("address1");
            when(purchaseContract.getTitle()).thenReturn(promise("purchase"));
            when(purchaseContract.getState()).thenReturn(promise(ContractState.Created));
            when(purchaseContract.getPrice()).thenReturn(promise(Web3Util.toWei(new BigDecimal("1"))));
            when(purchaseContract.getBuyer()).thenReturn(promise("buyer"));
            when(purchaseContract.getSeller()).thenReturn(promise(selectedAccount));
            when(purchaseContract.getDescription()).thenReturn(promise("description"));
            when(purchaseContract.getVerifyIdentity()).thenReturn(promise(false));
            when(purchaseContract.verifyContent()).thenReturn(promise(true));
            when(purchaseContract.getImageSignatures()).thenReturn(promise((List<String>)new ArrayList<String>()));
            when(purchaseContract.toJson()).thenReturn(new GsonSerializationService().serialize(new ContractInfo(ContractType.Purchase, "address1")));

            //fake rent contract
            rentContract = mock(IRentContract.class);
            when(rentContract.getContractType()).thenReturn(ContractType.Rent);
            when(rentContract.getContractAddress()).thenReturn("address2");
            when(rentContract.getTitle()).thenReturn(promise("rent"));
            when(rentContract.getState()).thenReturn(promise(ContractState.Locked));
            when(rentContract.getRentingFee()).thenReturn(promise(Web3Util.toWei(new BigDecimal("2"))));
            when(rentContract.getPrice()).thenReturn(promise(Web3Util.toWei(new BigDecimal("0.1"))));
            when(rentContract.getDeposit()).thenReturn(promise(Web3Util.toWei(new BigDecimal("10"))));
            when(rentContract.getBuyer()).thenReturn(promise("buyer"));
            when(rentContract.getDescription()).thenReturn(promise("description"));
            when(rentContract.getSeller()).thenReturn(promise(selectedAccount));
            when(rentContract.getVerifyIdentity()).thenReturn(promise(false));
            when(rentContract.verifyContent()).thenReturn(promise(true));
            when(rentContract.getImageSignatures()).thenReturn(promise((List<String>)new ArrayList<String>()));
            when(rentContract.toJson()).thenReturn(new GsonSerializationService().serialize(new ContractInfo(ContractType.Rent, "address2")));

        }catch(Exception e)
        {
        }

        contractList = new ArrayList<>();
        contractList.add(purchaseContract);
        contractList.add(rentContract);
    }

    /**
     * makes a {@link SimplePromise} that returns the specified value
     */
    public static <T> SimplePromise<T> promise(final T returnValue)
    {
        return Async.toPromise(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return returnValue;
            }
        });
    }

    /**
     * Sets up default service calls
     */
    public void setup() throws Exception
    {
        //set the ApplicationContext for the tests
        context = (TestAppContext) getInstrumentation().getTargetContext().getApplicationContext();
        context.initMocks();

        //set selected account
        when(context.getSettingProvider().getSelectedAccount()).thenReturn(selectedAccount);

        //make sure that connection is established to Ethereum client
        when(context.getServiceProvider().getConnectionService().hasConnection()).thenReturn(true);

        //set account balance for account
        when(context.getServiceProvider().getAccountService().getAccountBalance(selectedAccount)).thenReturn(Async.toPromise(new Callable<BigInteger>() {
            @Override
            public BigInteger call() throws Exception {
                return Web3Util.toWei(new BigDecimal("1000"));
            }
        }));

        //fake exchange service calls
        when(context.getServiceProvider().getExchangeService().convertToCurrency(any(BigDecimal.class), any(Currency.class)))
                .thenAnswer(new Answer<SimplePromise<BigDecimal>>() {
                    @Override
                    public SimplePromise<BigDecimal> answer(InvocationOnMock invocation) throws Throwable {
                        final BigDecimal value = (BigDecimal) invocation.getArguments()[0];
                        return Async.toPromise(new Callable<BigDecimal>() {
                            @Override
                            public BigDecimal call() throws Exception {
                                return value.multiply(exchangeRate);
                            }
                        });
                    }
                });

        when(context.getServiceProvider().getExchangeService().convertToEther(any(BigDecimal.class), any(Currency.class)))
                .thenAnswer(new Answer<SimplePromise<BigDecimal>>() {
                    @Override
                    public SimplePromise<BigDecimal> answer(InvocationOnMock invocation) throws Throwable {
                        final BigDecimal value = (BigDecimal) invocation.getArguments()[0];
                        return Async.toPromise(new Callable<BigDecimal>() {
                            @Override
                            public BigDecimal call() throws Exception {
                                return value.divide(exchangeRate, 18, BigDecimal.ROUND_HALF_EVEN);
                            }
                        });
                    }
                });

        when(context.getServiceProvider().getExchangeService().getExchangeRate(any(Currency.class)))
                .thenReturn(Async.toPromise(new Callable<BigDecimal>() {
                    @Override
                    public BigDecimal call() throws Exception {
                        return exchangeRate;
                    }
                }));


        when(context.getServiceProvider().getContractService().verifyContractCode(any(String.class), any(String.class)))
                .thenReturn(Async.toPromise(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return true;
                    }
                }));

        when(context.getPermissionProvider().hasPermission(any(String.class))).thenReturn(true);
    }
}
