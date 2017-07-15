package smart_contract.csg.ifi.uzh.ch.smartcontracttest;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.contract.IRentContract;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.contract.TimeUnit;
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by flo on 14.07.17.
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

        onView(withId(R.id.action_deploy_contract)).check(matches(not(isEnabled())));
        onView(withId(R.id.contract_price)).perform(typeText("100"));
        onView(withId(R.id.action_deploy_contract)).check(matches(isEnabled()));
    }

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
    }

    @Test
    public void deployPurchaseContract_WhenContractDeployed_ThenContractSavedAndHandedToTransactionManager()
    {
        //arrange
        Intent startIntent = new Intent();
        startIntent.putExtra(ContractCreateActivity.CONTRACT_TYPE_EXTRA, ContractType.Purchase);

        final IPurchaseContract contractMock = mock(IPurchaseContract.class);
        SimplePromise<ITradeContract> deployPromise = Async.toPromise(new Callable<ITradeContract>() {
            @Override
            public IPurchaseContract call() throws Exception {
                return contractMock;
            }
        });

        when(context.getServiceProvider().getContractService().deployPurchaseContract(any(BigInteger.class), any(String.class), any(String.class), any(ArrayList.class), any(boolean.class)))
                .thenReturn(deployPromise);

        getInstrumentation().addMonitor(ContractOverviewActivity.class.getName(), null, true);

        //act
        rule.launchActivity(startIntent);
        onView(withId(R.id.contract_title)).perform(typeText("title"));
        onView(withId(R.id.contract_description)).perform(typeText("description"));
        onView(withId(R.id.contract_price)).perform(typeText("10")).perform(closeSoftKeyboard());;
        onView(withId(R.id.action_deploy_contract)).perform(click());

        //assert
        verify(context.getTransactionManager()).toTransaction(deployPromise);
        verify(context.getServiceProvider().getContractService()).saveContract(contractMock, selectedAccount);
    }

    @Test
    public void deployRentContract_WhenContractDeployed_ThenContractSavedAndPromiseHandedToTransactionManager()
    {
        //arrange
        Intent startIntent = new Intent();
        startIntent.putExtra(ContractCreateActivity.CONTRACT_TYPE_EXTRA, ContractType.Rent);

        final IRentContract contractMock = mock(IRentContract.class);
        SimplePromise<ITradeContract> deployPromise = Async.toPromise(new Callable<ITradeContract>() {
            @Override
            public IRentContract call() throws Exception {
                return contractMock;
            }
        });

        when(context.getServiceProvider().getContractService().deployRentContract(any(BigInteger.class), any(BigInteger.class), any(TimeUnit.class), any(String.class), any(String.class), any(ArrayList.class), any(boolean.class)))
                .thenReturn(deployPromise);

        getInstrumentation().addMonitor(ContractOverviewActivity.class.getName(), null, true);

        //act
        rule.launchActivity(startIntent);
        onView(withId(R.id.contract_title)).perform(typeText("title")).perform(closeSoftKeyboard());
        onView(withId(R.id.contract_description)).perform(typeText("description")).perform(closeSoftKeyboard());
        onView(withId(R.id.contract_deposit)).perform(typeText("10")).perform(closeSoftKeyboard());
        onView(withId(R.id.contract_price)).perform(typeText("1")).perform(closeSoftKeyboard());
        onView(withId(R.id.action_deploy_contract)).perform(click());

        //assert
        verify(context.getTransactionManager()).toTransaction(deployPromise);
        verify(context.getServiceProvider().getContractService()).saveContract(contractMock, selectedAccount);
    }
}
