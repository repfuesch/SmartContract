package smart_contract.csg.ifi.uzh.ch.smartcontracttest;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.ViewHelper.CustomViewActions;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.ViewHelper.RecyclerViewItemCountAssertion;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.broadcast.LocalBroadcastService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.create.ContractCreateActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display.ContractDetailActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.mocks.TestAppContext;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.AddContractDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.ContractOverviewActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.list.TradeContractRecyclerViewAdapter;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.p2p.dialog.P2pImportDialog;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static smart_contract.csg.ifi.uzh.ch.smartcontracttest.ViewHelper.RecyclerViewMatcher.withRecyclerView;

/**
 * Instrumented tests for the {@link ContractOverviewActivity}
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class OverviewActivityTest extends InstrumentedTestBase {

    @Rule
    public ActivityTestRule<ContractOverviewActivity> rule  = new  ActivityTestRule<>(ContractOverviewActivity.class, true, false);

    @Before
    public void setup() throws Exception {
        super.setup();

        //setup service call to load contract list
        when(context.getServiceProvider().getContractService().loadContracts(selectedAccount))
                .thenReturn(promise(contractList));
    }

    /**
     * Check that the {@link TradeContractRecyclerViewAdapter} is initialized correctly after the
     * creation of the activity
     */
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
                .check(matches(withText(purchaseContract.getTitle().get())));

        onView(withRecyclerView(R.id.purchase_list).atPositionOnView(0, R.id.list_detail_state))
                .check(matches(withText(purchaseContract.getState().get().toString())));

        onView(withRecyclerView(R.id.purchase_list).atPositionOnView(1, R.id.list_detail_contract_type))
                .check(matches(withText("Rent Contract")));

        onView(withRecyclerView(R.id.purchase_list).atPositionOnView(1, R.id.list_detail_title))
                .check(matches(withText(rentContract.getTitle().get())));

        onView(withRecyclerView(R.id.purchase_list).atPositionOnView(1, R.id.list_detail_state))
                .check(matches(withText(rentContract.getState().get().toString())));
    }

    /**
     * checks that the account balance is set after the creation of the activity
     */
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

    /**
     * Checks that the {@link ContractDetailActivity} is started when the user clicks on a {@link TradeContractRecyclerViewAdapter.ViewHolder}
     * item.
     */
    @Test
    public void listItem_WhenClicked_ThenDetailActivityForContractOpened()
    {
        //arrange

        //setup monitor for DetailActivity
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(ContractDetailActivity.class.getName(), null, false);

        //setup service call to retrieve the contract
        when(context.getServiceProvider().getContractService().loadContract(purchaseContract.getContractType(), purchaseContract.getContractAddress(), selectedAccount))
                .thenReturn(promise((ITradeContract)purchaseContract));

        //act
        rule.launchActivity(new Intent());
        onView(withRecyclerView(R.id.purchase_list).atPosition(0)).perform(click());

        //assert
        ContractDetailActivity nextActivity = (ContractDetailActivity)getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);

        // next activity is opened and captured.
        assertNotNull(nextActivity);

        //verify start intent of activity
        Intent intent = nextActivity.getIntent();
        assertThat(intent.getStringExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS), is(purchaseContract.getContractAddress()));
        assertThat((ContractType)intent.getSerializableExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE), is(purchaseContract.getContractType()));
        nextActivity .finish();
    }

    /**
     * Checks that that a contract that is manually added in the {@link AddContractDialogFragment} is saved and added to the list
     */
    @Test
    public void onAddContract_WhenContractAddedManually_ThenContractAdded()
    {
        //arrange
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Purchase, "account_address", selectedAccount))
                .thenReturn(promise((ITradeContract)purchaseContract));

        rule.launchActivity(new Intent());

        //open dialog
        onView(withId(R.id.action_add_contract)).perform(click());

        //fill out dialog
        onView(withId(R.id.option_add_contract)).perform(click());
        onView(withId(R.id.contract_address_field)).perform(typeText("account_address"));
        onView(withText(R.string.ok)).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        //assert call
        verify(context.getServiceProvider().getContractService()).saveContract(any(ContractInfo.class), any(String.class));
        verify(context.getServiceProvider().getContractService()).loadContract(ContractType.Purchase, "account_address", selectedAccount);
        onView(withId(R.id.purchase_list)).check(new RecyclerViewItemCountAssertion(3));
    }

    /**
     * Checks that the {@link ContractCreateActivity} is started when a user wants to create a contract in the {@link AddContractDialogFragment}
     */
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

    /**
     * Checks that a contract scanned by the {@link QrScanningActivity} is saved and that the {@link ContractDetailActivity} is
     * started.
     */
    @Test
    public void onScanContract_WhenScanResultReturned_ThenContractSavedAndDetailActivityStarted()
    {
        //arrange
        String contractAddress = "contract_address";
        ContractType contractType = ContractType.Purchase;
        ContractInfo info = new ContractInfo(contractType, contractAddress);

        //setup result of scanning activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(QrScanningActivity.MESSAGE_CONTRACT_DATA, new GsonSerializationService().serialize(info));
        Instrumentation.ActivityResult activityResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent);
        Instrumentation.ActivityMonitor scanActivityMonitor = getInstrumentation().addMonitor(QrScanningActivity.class.getName(), activityResult, true);

        //add monitor for detail activity
        Instrumentation.ActivityMonitor detailMonitor = getInstrumentation().addMonitor(ContractDetailActivity.class.getName(), null, true);

        //act
        rule.launchActivity(new Intent());
        onView(withId(R.id.action_scan_contract)).perform(click());

        //assert
        getInstrumentation().waitForMonitorWithTimeout(scanActivityMonitor, 5);
        verify(context.getServiceProvider().getContractService()).saveContract(any(ContractInfo.class), any(String.class));
    }

    /**
     * Checks that a contract that is imported with the {@link P2pImportDialog} is saved and added to the list
     */
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
        verify(context.getServiceProvider().getContractService()).saveContract(any(ContractInfo.class), any(String.class));
        verify(context.getServiceProvider().getContractService()).loadContract(ContractType.Purchase, "contract_address", selectedAccount);
    }

    /**
     * Checks that a newly created contract is added to the list
     */
    @Test
    public void onContractCreated_WhenContractCreateTransactionCompleted_ThenContractAddedToList()
    {
        //arrange

        //setup service call to load the new contract
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Purchase, "new_contract_address", selectedAccount))
                .thenReturn(Async.toPromise(new Callable<ITradeContract>() {
                    @Override
                    public ITradeContract call() throws Exception {
                        return mock(ITradeContract.class);
                    }
                }));

        //initialize the BroadCastService with the actual context and set the corrent intents
        TestAppContext.BroadCastService = new LocalBroadcastService(context.getContext());
        Intent createIntent = new Intent(TransactionHandler.ACTION_TRANSACTION_CREATED);
        createIntent.putExtra(TransactionHandler.CONTRACT_TYPE, ContractType.Purchase);
        createIntent.putExtra(TransactionHandler.CONTRACT_ADDRESS, "new_contract_address");

        //act
        rule.launchActivity(new Intent());

        //broadcast intent
        context.getBroadCastService().sendBroadcast(createIntent);
        onView(isRoot()).perform(CustomViewActions.waitFor(200));

        //assert
        verify(context.getServiceProvider().getContractService()).loadContract(ContractType.Purchase, "new_contract_address", selectedAccount);
        onView(withId(R.id.purchase_list)).check(new RecyclerViewItemCountAssertion(3));
    }
}
