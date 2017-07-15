package smart_contract.csg.ifi.uzh.ch.smartcontracttest;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.IContractObservable;
import ch.uzh.ifi.csg.contract.contract.IContractObserver;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.ViewHelper.CustomViewActions;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.ViewHelper.RecyclerViewItemCountAssertion;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.account.AccountActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.broadcast.LocalBroadcastService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionManager;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create.ContractCreateActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display.ContractDetailActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.mocks.TestAppContext;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.ContractOverviewActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.qrcode.QrScanningActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static smart_contract.csg.ifi.uzh.ch.smartcontracttest.ViewHelper.RecyclerViewMatcher.withRecyclerView;

/**
 * Created by flo on 14.07.17.
 */

@MediumTest
@RunWith(AndroidJUnit4.class)
public class OverviewActivityTest extends InstrumentedTestBase {

    @Rule
    public ActivityTestRule<ContractOverviewActivity> rule  = new  ActivityTestRule<>(ContractOverviewActivity.class, true, false);

    @Before
    public void setup() throws Exception {
        super.setup();
        when(context.getServiceProvider().getContractService().loadContracts(selectedAccount))
                .thenReturn(Async.toPromise(new Callable<List<ITradeContract>>() {
                    @Override
                    public List<ITradeContract> call() throws Exception {
                        return contractList;
                    }
                }));
    }

    @Test
    public void onCreate_WhenCreated_ThenContractsLoadedAndDisplayedAndEventsRegistered() throws Throwable {
        //arrange


        //act
        rule.launchActivity(new Intent());

        //assert field values
        onView(withId(R.id.purchase_list)).check(new RecyclerViewItemCountAssertion(2));

        onView(withRecyclerView(R.id.purchase_list).atPositionOnView(0, R.id.list_detail_contract_type))
                .check(matches(withText("Purchase Contract")));

        onView(withRecyclerView(R.id.purchase_list).atPositionOnView(0, R.id.list_detail_title))
                .check(matches(withText(purchaseContract.getTitle())));

        onView(withRecyclerView(R.id.purchase_list).atPositionOnView(0, R.id.list_detail_state))
                .check(matches(withText(purchaseContract.getState().toString())));

        onView(withRecyclerView(R.id.purchase_list).atPositionOnView(1, R.id.list_detail_contract_type))
                .check(matches(withText("Rent Contract")));

        onView(withRecyclerView(R.id.purchase_list).atPositionOnView(1, R.id.list_detail_title))
                .check(matches(withText(rentContract.getTitle())));

        onView(withRecyclerView(R.id.purchase_list).atPositionOnView(1, R.id.list_detail_state))
                .check(matches(withText(rentContract.getState().toString())));
    }

    @Test
    public void onDestroy_WhenDestroyed_ThenContractsUnregistered() throws Throwable {
        /*
        //act
        rule.launchActivity(new Intent());
        onView(isRoot()).perform(CustomViewActions.waitFor(500));

        rule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getInstrumentation().callActivityOnDestroy(rule.getActivity());
            }
        });
*/

        //arrange
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(AccountActivity.class.getName(), null, true);

        //act
        rule.launchActivity(new Intent());
        //navigate to account activity
        onView(withId(R.id.action_login)).perform(click());
       // AccountActivity nextActivity = (AccountActivity)getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);
        onView(isRoot()).perform(CustomViewActions.waitFor(500));

        //assert that list items are registered exactly once on the contract
        verify(purchaseContract, times(1)).addObserver(any(IContractObserver.class));
        verify(rentContract, times(1)).addObserver(any(IContractObserver.class));

        //assert that list items are unregistered exactly once on the contract
        //assert that list items are registered exactly once on the contract
        verify(purchaseContract, times(1)).removeObserver(any(IContractObserver.class));
        verify(rentContract, times(1)).removeObserver(any(IContractObserver.class));
    }

    @Test
    public void onCreate_WhenCreated_ThenAccountBalanceUpdated()
    {
        //arrange

        //act
        rule.launchActivity(new Intent());

        //assert
        onView(withId(R.id.account_balance_field)).check(matches(isDisplayed()));
        onView(withId(R.id.account_balance_field)).check(matches(withText("1000")));
    }

    @Test
    public void listItem_WhenClicked_ThenDetailActivityForContractOpened()
    {
        //arrange
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(ContractDetailActivity.class.getName(), null, false);
        when(context.getServiceProvider().getContractService().loadContract(purchaseContract.getContractType(), purchaseContract.getContractAddress(), selectedAccount))
                .thenReturn(Async.toPromise(new Callable<ITradeContract>() {
                    @Override
                    public ITradeContract call() throws Exception {
                        return purchaseContract;
                    }
                }));

        //act
        rule.launchActivity(new Intent());
        onView(withRecyclerView(R.id.purchase_list).atPosition(0)).perform(click());

        //assert
        ContractDetailActivity nextActivity = (ContractDetailActivity)getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);

        // next activity is opened and captured.
        assertNotNull(nextActivity);
        Intent intent = nextActivity.getIntent();
        assertThat(intent.getStringExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS), is(purchaseContract.getContractAddress()));
        assertThat((ContractType)intent.getSerializableExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE), is(purchaseContract.getContractType()));
        nextActivity .finish();
    }

    @Test
    public void onAddContract_WhenContractAddedManually_ThenContractAdded()
    {
        //arrange
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Purchase, "account_address", selectedAccount))
                .thenReturn(Async.toPromise(new Callable<ITradeContract>() {
                    @Override
                    public ITradeContract call() throws Exception {
                        return mock(IPurchaseContract.class);
                    }
                }));

        rule.launchActivity(new Intent());

        //open dialog
        onView(withId(R.id.action_add_contract)).perform(click());

        //fill out dialog
        onView(withId(R.id.option_add_contract)).perform(click());
        onView(withId(R.id.contract_address_field)).perform(typeText("account_address"));
        onView(withText(R.string.ok)).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        //assert call
        verify(context.getServiceProvider().getContractService()).loadContract(ContractType.Purchase, "account_address", selectedAccount);
        onView(withId(R.id.purchase_list)).check(new RecyclerViewItemCountAssertion(3));
    }

    @Test
    public void onCreateContract_WhenContractCreateOptionSelected_ThenContractCreateActivityStarted()
    {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(ContractCreateActivity.class.getName(), null, false);

        rule.launchActivity(new Intent());

        //open dialog
        onView(withId(R.id.action_add_contract)).perform(click());

        //fill out dialog
        onView(withId(R.id.option_create_contract)).perform(click());
        onView(withText(R.string.ok)).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        //assert
        ContractCreateActivity nextActivity = (ContractCreateActivity) getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);

        // next activity is opened and captured.
        assertNotNull(nextActivity);
        Intent intent = nextActivity.getIntent();
        assertThat((ContractType)intent.getSerializableExtra(ContractCreateActivity.CONTRACT_TYPE_EXTRA), is(ContractType.Purchase));
    }

    @Test
    public void onScanContract_WhenScanResultReturned_ThenContractSavedAndDetailActivityStarted()
    {
        //arrange
        String contractAddress = "contract_address";
        ContractType contractType = ContractType.Purchase;

        Intent resultIntent = new Intent();
        resultIntent.putExtra(QrScanningActivity.MESSAGE_CONTRACT_ADDRESS, contractAddress);
        resultIntent.putExtra(QrScanningActivity.MESSAGE_CONTRACT_TYPE, contractType);
        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent);
        Instrumentation.ActivityMonitor scanActivityMonitor = getInstrumentation().addMonitor(QrScanningActivity.class.getName(), activityResult, true);

        Instrumentation.ActivityMonitor detailMonitor = getInstrumentation().addMonitor(ContractDetailActivity.class.getName(), null, false);
        when(context.getServiceProvider().getContractService().loadContract(contractType, contractAddress, selectedAccount))
                .thenReturn(Async.toPromise(new Callable<ITradeContract>() {
                    @Override
                    public ITradeContract call() throws Exception {
                        return purchaseContract;
                    }
                }));

        //act
        rule.launchActivity(new Intent());
        onView(withId(R.id.action_scan_contract)).perform(click());

        //assert
        getInstrumentation().waitForMonitorWithTimeout(scanActivityMonitor, 5);
        verify(context.getServiceProvider().getContractService()).saveContract(contractAddress, contractType, selectedAccount);

        ContractDetailActivity detailActivity = (ContractDetailActivity)getInstrumentation().waitForMonitorWithTimeout(detailMonitor, 5);
        Intent intent = detailActivity.getIntent();
        assertThat(intent.getStringExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS), is(contractAddress));
        assertThat((ContractType)intent.getSerializableExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE), is(contractType));
    }

    @Test
    public void onImportContract_WhenImportResultReturned_ThenContractLoaded()
    {
        //arrange
        ContractInfo importedContract = new ContractInfo(ContractType.Purchase, "contract_address");
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Purchase, "contract_address", selectedAccount))
                .thenReturn(Async.toPromise(new Callable<ITradeContract>() {
                    @Override
                    public ITradeContract call() throws Exception {
                        return mock(IPurchaseContract.class);
                    }
                }));

        //act
        rule.launchActivity(new Intent());
        rule.getActivity().onContractDataReceived(importedContract);

        //assert
        verify(context.getServiceProvider().getContractService()).loadContract(ContractType.Purchase, "contract_address", selectedAccount);
    }

    @Test
    public void onContractCreated_WhenContractCreateTransactionCompleted_ThenContractAddedToList()
    {
        //arrange
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Purchase, "new_contract_address", selectedAccount))
                .thenReturn(Async.toPromise(new Callable<ITradeContract>() {
                    @Override
                    public ITradeContract call() throws Exception {
                        return mock(ITradeContract.class);
                    }
                }));

        TestAppContext.BroadCastService = new LocalBroadcastService(context.getContext());
        Intent createIntent = new Intent(TransactionManager.ACTION_CREATE_TRANSACTION);
        createIntent.putExtra(TransactionManager.CONTRACT_TYPE, ContractType.Purchase);
        createIntent.putExtra(TransactionManager.CONTRACT_ADDRESS, "new_contract_address");

        //act
        rule.launchActivity(new Intent());
        context.getBroadCastService().sendBroadcast(createIntent);
        onView(isRoot()).perform(CustomViewActions.waitFor(200));

        //assert
        verify(context.getServiceProvider().getContractService()).loadContract(ContractType.Purchase, "new_contract_address", selectedAccount);
        onView(withId(R.id.purchase_list)).check(new RecyclerViewItemCountAssertion(3));
    }
}
