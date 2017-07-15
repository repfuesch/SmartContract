package smart_contract.csg.ifi.uzh.ch.smartcontracttest;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
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
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContext;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by flo on 14.07.17.
 */

public abstract class InstrumentedTestBase {

    protected static IPurchaseContract purchaseContract;
    protected static IRentContract rentContract;
    protected static List<ITradeContract> contractList;
    protected static VCard testProfile;

    protected final static String selectedAccount = "account";
    protected final static BigDecimal exchangeRate = new BigDecimal("200");

    protected ApplicationContext context;

    static { initTestContracts(); initTestProfileCard(); }

    private static void initTestProfileCard()
    {
        testProfile = new VCard();

        StructuredName name = new StructuredName();
        name.setFamily("Muster");
        name.setGiven("Hans");

        Address address = new Address();
        address.setStreetAddress("Musterstrasse 12");
        address.setLocality("Musterhausen");
        address.setPostalCode("3333");
        address.setCountry("Switzerland");
        address.setRegion("Lucerne");

        testProfile.addAddress(address);
        testProfile.setStructuredName(name);
        testProfile.addEmail("max.muster@example.com");
        testProfile.addTelephoneNumber("+41795821457", TelephoneType.HOME);
    }

    private static void initTestContracts()
    {
        try{
            //setup contract list
            purchaseContract = mock(IPurchaseContract.class);
            when(purchaseContract.getContractType()).thenReturn(ContractType.Purchase);
            when(purchaseContract.getContractAddress()).thenReturn("address1");
            when(purchaseContract.getTitle()).thenReturn("purchase");
            when(purchaseContract.getState()).thenReturn(ContractState.Created);
            when(purchaseContract.getPrice()).thenReturn(Web3Util.toWei(new BigDecimal("1")));
            when(purchaseContract.getBuyer()).thenReturn("buyer");
            when(purchaseContract.getSeller()).thenReturn(selectedAccount);
            when(purchaseContract.getDescription()).thenReturn("description");

            rentContract = mock(IRentContract.class);
            when(rentContract.getContractType()).thenReturn(ContractType.Rent);
            when(rentContract.getContractAddress()).thenReturn("address2");
            when(rentContract.getTitle()).thenReturn("rent");
            when(rentContract.getState()).thenReturn(ContractState.Locked);
            when(rentContract.getRentingFee()).thenReturn(Web3Util.toWei(new BigDecimal("2")));
            when(rentContract.getPrice()).thenReturn(Web3Util.toWei(new BigDecimal("0.1")));
            when(rentContract.getDeposit()).thenReturn(Web3Util.toWei(new BigDecimal("10")));
            when(rentContract.getBuyer()).thenReturn("buyer");
            when(rentContract.getDescription()).thenReturn("description");
            when(rentContract.getSeller()).thenReturn(selectedAccount);
        }catch(Exception e)
        {
        }

        contractList = new ArrayList<>();
        contractList.add(purchaseContract);
        contractList.add(rentContract);
    }

    public void setup() throws Exception
    {
        //setup service calls
        context = (ApplicationContext) getInstrumentation().getTargetContext().getApplicationContext();
        when(context.getSettingProvider().getSelectedAccount()).thenReturn(selectedAccount);
        when(context.getServiceProvider().getConnectionService().hasConnection()).thenReturn(true);
        when(context.getServiceProvider().getAccountService().getAccountBalance(selectedAccount)).thenReturn(Async.toPromise(new Callable<BigInteger>() {
            @Override
            public BigInteger call() throws Exception {
                return Web3Util.toWei(new BigDecimal("1000"));
            }
        }));

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

        when(context.getServiceProvider().getContractService().isContract(any(String.class)))
                .thenReturn(Async.toPromise(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return true;
                    }
                }));
    }
}
