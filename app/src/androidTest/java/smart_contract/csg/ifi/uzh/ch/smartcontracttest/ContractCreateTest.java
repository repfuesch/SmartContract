package smart_contract.csg.ifi.uzh.ch.smartcontracttest;

import android.content.Intent;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.contract.IRentContract;
import ch.uzh.ifi.csg.contract.contract.TimeUnit;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import ch.uzh.ifi.csg.contract.util.Web3Util;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ServiceProvider;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create.ContractCreateActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create.ContractDeployFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create.PurchaseContractDeployFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create.RentContractDeployFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.ContractOverviewActivity;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Instrumented Unit tests for the {@link ContractCreateActivity}
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ContractCreateTest extends InstrumentedTestBase
{
    @Rule
    public ActivityTestRule<ContractCreateActivity> rule  = new  ActivityTestRule<>(ContractCreateActivity.class, true, false);

    @Before
    public void setup() throws Exception {
        super.setup();
    }

    /**
     * Checks that the {@link PurchaseContractDeployFragment} is loaded when the Activity is started with
     * with a {@link ContractType#Purchase} intent
     */
    @Test
    public void onCreate_WhenCreatedWithPurchaseContractIntent_ThenPurchaseContractDeployFragmentLoaded()
    {
        //arrange
        Intent startIntent = new Intent();
        startIntent.putExtra(ContractCreateActivity.CONTRACT_TYPE_EXTRA, ContractType.Purchase);

        //act
        rule.launchActivity(startIntent);

        //assert
        ContractCreateActivity activity = rule.getActivity();
        assertThat(activity.getFragmentManager().findFragmentByTag(ContractDeployFragment.TAG), instanceOf(PurchaseContractDeployFragment.class));
    }

    /**
     * Checks that the {@link RentContractDeployFragment} is loaded when the Activity is started with
     * with a {@link ContractType#Rent} intent
     */
    @Test
    public void onCreate_WhenCreatedWithRentContractIntent_ThenRentContractDeployFragmentLoaded()
    {
        //arrange
        Intent startIntent = new Intent();
        startIntent.putExtra(ContractCreateActivity.CONTRACT_TYPE_EXTRA, ContractType.Rent);

        //act
        rule.launchActivity(startIntent);

        //assert
        ContractCreateActivity activity = rule.getActivity();
        assertThat(activity.getFragmentManager().findFragmentByTag(ContractDeployFragment.TAG), instanceOf(RentContractDeployFragment.class));
    }

    /**
     * Checks that the deploy button is enabled after all mandatory fields are set in the
     * {@link PurchaseContractDeployFragment}
     */
    @Test
    public void deployPurchaseContract_WhenAllMandatoryFieldsSet_ThenDeployButtonEnabled()
    {
        //arrange
        Intent startIntent = new Intent();
        startIntent.putExtra(ContractCreateActivity.CONTRACT_TYPE_EXTRA, ContractType.Purchase);

        //act
        rule.launchActivity(startIntent);
        onView(withId(R.id.contract_title)).perform(typeText("title"));
        onView(withId(R.id.contract_description)).perform(typeText("description"));

        //verify
        onView(withId(R.id.action_deploy_contract)).check(matches(not(isEnabled())));
        onView(withId(R.id.contract_price)).perform(typeText("100"));
        onView(withId(R.id.action_deploy_contract)).check(matches(isEnabled()));
    }

    /**
     * Checks that the deploy button is enabled after all mandatory fields are set in the
     * {@link RentContractDeployFragment}
     */
    @Test
    public void deployRentContract_WhenAllMandatoryFieldsSet_ThenDeployButtonEnabled()
    {
        //arrange
        Intent startIntent = new Intent();
        startIntent.putExtra(ContractCreateActivity.CONTRACT_TYPE_EXTRA, ContractType.Rent);

        //act + assert
        rule.launchActivity(startIntent);
        onView(withId(R.id.contract_description)).perform(typeText("description"));
        onView(withId(R.id.contract_title)).perform(typeText("title"));
        onView(withId(R.id.contract_price)).perform(typeText("1"));
        onView(withId(R.id.action_deploy_contract)).check(matches(not(isEnabled())));
        onView(withId(R.id.contract_deposit)).perform(typeText("10"));
        onView(withId(R.id.action_deploy_contract)).check(matches(isEnabled()));
    }

    /**
     * Checks that an error is displayed when the user does not have neough money to deploy a
     * purchase contract
     */
    @Test
    public void deployPurchaseContract_WhenPriceHigherThanBalance_ThenErrorIsDisplayed()
    {
        //arrange
        Intent startIntent = new Intent();
        startIntent.putExtra(ContractCreateActivity.CONTRACT_TYPE_EXTRA, ContractType.Purchase);

        //act
        rule.launchActivity(startIntent);
        onView(withId(R.id.contract_title)).perform(typeText("title"));
        onView(withId(R.id.contract_description)).perform(typeText("description"));
        onView(withId(R.id.contract_price)).perform(typeText("100000000000000000")).perform(closeSoftKeyboard());;
        onView(withId(R.id.action_deploy_contract)).perform(click());

        //assert
        verify(context.getMessageService()).showErrorMessage("You don't have enough money to do that!");
        verifyNoMoreInteractions(context.getServiceProvider().getContractService());
    }

    /**
     * Checks that the {@link ContractService#deployPurchaseContract} is invoked wit the correct
     * arguments after the user filled out the fields and hits the 'deploy' button.
     */
    @Test
    public void deployPurchaseContract_WhenContractDeployed_ThenContractDeployed()
    {
        //arrange
        Intent startIntent = new Intent();
        startIntent.putExtra(ContractCreateActivity.CONTRACT_TYPE_EXTRA, ContractType.Purchase);

        final IPurchaseContract contractMock = mock(IPurchaseContract.class);
        SimplePromise<IPurchaseContract> promise = promise(contractMock);

        //setup service call
        when(context.getServiceProvider().getContractService().deployPurchaseContract(any(BigInteger.class), any(String.class), any(String.class), any(HashMap.class), any(boolean.class), any(boolean.class)))
                .thenReturn(promise);

        //setup a monitor to prevent that the overview activity is loaded
        getInstrumentation().addMonitor(ContractOverviewActivity.class.getName(), null, true);

        //act
        rule.launchActivity(startIntent);

        //fill out fields
        onView(withId(R.id.contract_title)).perform(typeText("title"));
        onView(withId(R.id.contract_description)).perform(typeText("description"));
        onView(withId(R.id.contract_price)).perform(typeText("10")).perform(closeSoftKeyboard());;
        onView(withId(R.id.action_deploy_contract)).perform(click());

        //assert

        //calculate the correct price in wei
        BigDecimal priceEther = context.getServiceProvider().getExchangeService().convertToEther(new BigDecimal("10"), Currency.USD).get();
        BigInteger priceWei = Web3Util.toWei(priceEther).multiply(BigInteger.valueOf(2));

        ArgumentCaptor<BigInteger> priceCaptor = ArgumentCaptor.forClass(BigInteger.class);
        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);

        //verify call the service
        verify(context.getServiceProvider().getContractService()).deployPurchaseContract(priceCaptor.capture(), titleCaptor.capture(), descriptionCaptor.capture(), any(HashMap.class), any(boolean.class), any(boolean.class));
        assertThat(priceCaptor.getValue(), is(priceWei));
        assertThat(titleCaptor.getValue(), is("title"));
        assertThat(descriptionCaptor.getValue(), is("description"));
    }

    /**
     * Checks that the {@link ContractService#deployRentContract} is invoked wit the correct
     * arguments after the user filled out the fields and hits the 'deploy' button.
     */
    @Test
    public void deployRentContract_WhenContractDeployed_ThenContractSavedAndPromiseHandedToTransactionManager()
    {
        //arrange
        Intent startIntent = new Intent();
        startIntent.putExtra(ContractCreateActivity.CONTRACT_TYPE_EXTRA, ContractType.Rent);

        final IRentContract contractMock = mock(IRentContract.class);
        SimplePromise<IRentContract> promise = promise(contractMock);

        //setup service call
        when(context.getServiceProvider().getContractService().deployRentContract(any(BigInteger.class), any(BigInteger.class), any(TimeUnit.class), any(String.class), any(String.class), any(HashMap.class), any(boolean.class), any(boolean.class)))
                .thenReturn(promise);

        //setup a monitor to prevent that the overview activity is loaded
        getInstrumentation().addMonitor(ContractOverviewActivity.class.getName(), null, true);

        //act
        rule.launchActivity(startIntent);

        //fill out fields
        onView(withId(R.id.contract_title)).perform(typeText("title")).perform(closeSoftKeyboard());
        onView(withId(R.id.contract_description)).perform(typeText("description")).perform(closeSoftKeyboard());
        onView(withId(R.id.contract_deposit)).perform(typeText("10")).perform(closeSoftKeyboard());
        onView(withId(R.id.contract_price)).perform(typeText("1")).perform(closeSoftKeyboard());
        onView(withId(R.id.action_deploy_contract)).perform(click());

        //assert

        //calculate the correct price in wei
        BigDecimal priceEther = context.getServiceProvider().getExchangeService().convertToEther(new BigDecimal("1"), Currency.USD).get();
        BigInteger priceWei = Web3Util.toWei(priceEther).divide(BigInteger.valueOf(3600));
        BigDecimal depositEther = context.getServiceProvider().getExchangeService().convertToEther(new BigDecimal("10"), Currency.USD).get();
        BigInteger depositWei = Web3Util.toWei(depositEther);

        //verify call
        ArgumentCaptor<BigInteger> priceCaptor = ArgumentCaptor.forClass(BigInteger.class);
        ArgumentCaptor<BigInteger> depositCaptor = ArgumentCaptor.forClass(BigInteger.class);
        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> descriptionCaptor = ArgumentCaptor.forClass(String.class);

        verify(context.getServiceProvider().getContractService()).deployRentContract(priceCaptor.capture(), depositCaptor.capture(), any(TimeUnit.class), titleCaptor.capture(), descriptionCaptor.capture(), any(HashMap.class), any(boolean.class), any(boolean.class));
        assertThat(priceCaptor.getValue(), is(priceWei));
        assertThat(depositCaptor.getValue(), is(depositWei));
        assertThat(titleCaptor.getValue(), is("title"));
        assertThat(descriptionCaptor.getValue(), is("description"));
    }
}
