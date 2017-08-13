package smart_contract.csg.ifi.uzh.ch.smartcontract;

import android.app.Instrumentation;
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
import java.util.Arrays;
import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import ch.uzh.ifi.csg.contract.util.Web3Util;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import smart_contract.csg.ifi.uzh.ch.smartcontract.detail.display.ContractDetailActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontract.detail.display.PurchaseContractDetailFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontract.detail.display.RentContractDetailFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontract.p2p.dialog.P2pExportDialog;
import smart_contract.csg.ifi.uzh.ch.smartcontract.profile.ProfileFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontract.qrcode.QrScanningActivity;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Instrumented tests for the {@link ContractDetailActivity} class
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ContractDetailTest extends InstrumentedTestBase {

    //contains the inputs and expected outputs to check the state of the transaction buttons
    private static Iterable<Object[]> interactionTestParameters = Arrays.asList(new Object[][]{
            { ContractType.Purchase, ContractState.Created, true, R.id.abort_button },
            { ContractType.Purchase, ContractState.Created, false, R.id.buy_button },
            { ContractType.Purchase, ContractState.Locked, true, -1 },
            { ContractType.Purchase, ContractState.Locked, false, R.id.confirm_button },
            { ContractType.Purchase, ContractState.Inactive, true, -1 },
            { ContractType.Purchase, ContractState.Inactive, false, -1 },
            { ContractType.Rent, ContractState.Created, true,  R.id.abort_button },
            { ContractType.Rent, ContractState.Created, false, R.id.rent_button },
            { ContractType.Rent, ContractState.Locked, true, R.id.reclaim_button },
            { ContractType.Rent, ContractState.Locked, false, -1 },
            { ContractType.Rent, ContractState.AwaitPayment, true, R.id.reclaim_button },
            { ContractType.Rent, ContractState.AwaitPayment, false, R.id.return_button },
            { ContractType.Rent, ContractState.Inactive, true, -1 },
            { ContractType.Rent, ContractState.Inactive, false, -1 },
    });

    @Before
    public void setup() throws Exception {
        super.setup();
    }

    @Rule
    public ActivityTestRule<ContractDetailActivity> rule  = new  ActivityTestRule<>(ContractDetailActivity.class, true, false);

    /**
     * Checks that the {@link PurchaseContractDetailFragment} is loaded with the correct field values when
     * the Activity is started with {@link ContractType#Purchase} intent.
     */
    @Test
    public void onCreate_WhenCreatedWithPurchaseContractIntent_ThenPurchaseContractDetailFragmentLoadedCorrectly() throws Exception {
        //arrange
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Purchase, purchaseContract.getContractAddress(), selectedAccount))
                .thenReturn(promise(((ITradeContract)purchaseContract)));

        //setup start intent for Activity
        Intent startIntent = new Intent();
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, ContractType.Purchase);
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, purchaseContract.getContractAddress());

        //act
        ContractDetailActivity activity = rule.launchActivity(startIntent);

        //assert
        assertThat(activity.getFragmentManager().findFragmentByTag("Details"), instanceOf(PurchaseContractDetailFragment.class));

        //assert fields
        onView(withId(R.id.general_title)).check(matches(withText(purchaseContract.getTitle().get())));
        onView(withId(R.id.general_address)).check(matches(withText(purchaseContract.getContractAddress())));
        BigDecimal priceCurrency = context.getServiceProvider().getExchangeService().convertToCurrency(Web3Util.toEther(purchaseContract.getPrice().get()), Currency.USD).get();
        onView(withId(R.id.general_price)).check(matches(withText(priceCurrency.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())));
        onView(withId(R.id.general_description)).check(matches(withText(purchaseContract.getDescription().get())));
        onView(withId(R.id.general_state)).check(matches(withText(purchaseContract.getState().get().toString())));
    }

    /**
     * Checks that the {@link RentContractDetailFragment} is loaded with the correct field values when
     * the Activity is started with {@link ContractType#Rent} intent.
     */
    @Test
    public void onCreate_WhenCreatedWithRentContractIntent_ThenRentContractDetailFragmentLoadedCorrectly() throws Exception {
        //arrange
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Rent, rentContract.getContractAddress(), selectedAccount))
                .thenReturn(promise((ITradeContract)rentContract));

        Intent startIntent = new Intent();
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, ContractType.Rent);
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, rentContract.getContractAddress());

        //act
        ContractDetailActivity activity = rule.launchActivity(startIntent);

        //assert
        assertThat(activity.getFragmentManager().findFragmentByTag("Details"), instanceOf(RentContractDetailFragment.class));

        //assert fields
        onView(withId(R.id.general_title)).check(matches(withText(rentContract.getTitle().get())));
        onView(withId(R.id.general_address)).check(matches(withText(rentContract.getContractAddress())));
        onView(withId(R.id.general_description)).check(matches(withText(rentContract.getDescription().get())));
        onView(withId(R.id.general_state)).check(matches(withText(rentContract.getState().get().toString())));
        BigDecimal depositCurrency = context.getServiceProvider().getExchangeService().convertToCurrency(Web3Util.toEther(rentContract.getDeposit().get()), Currency.USD).get();
        onView(withId(R.id.rent_deposit)).check(matches(withText(depositCurrency.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())));
        BigDecimal currentFeeCurrency = context.getServiceProvider().getExchangeService().convertToCurrency(Web3Util.toEther(rentContract.getRentingFee().get()), Currency.USD).get();
        onView(withId(R.id.rent_current_fee)).check(matches(withText(currentFeeCurrency.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())));
    }

    /**
     * Verifies that the state of the transaction buttons is correct for all combinations of the
     * {@link ContractState} of the contract and the role (seller/buyer) of the user in the contract.
     *
     * Accepted input/output pairs are contained in {@link #interactionTestParameters}
     */
    @Test
    public void onCreate_WhenContractInState_ThenCorrectButtonEnabled() throws Exception {
        for(Object[] parameters : interactionTestParameters)
        {
            //parse parameters
            ContractType type = (ContractType)parameters[0];
            ContractState state = (ContractState)parameters[1];
            boolean isSeller = (Boolean)parameters[2];
            Integer enabledButtonId = (Integer)parameters[3];

            if(type == ContractType.Purchase)
            {
                onCreate_WhenPurchaseContractInState_ThenCorrectButtonEnabled(state, isSeller, enabledButtonId);
            }else{
                onCreate_WhenRentContractInState_ThenCorrectButtonEnabled(state, isSeller, enabledButtonId);
            }
        }
    }

    /**
     * Verifies the button states for the {@link PurchaseContractDetailFragment}
     */
    private void onCreate_WhenPurchaseContractInState_ThenCorrectButtonEnabled(ContractState state, boolean isSeller, Integer enabledButtonId) throws Exception {
        //arrange

        //setup service call
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Purchase, purchaseContract.getContractAddress(), selectedAccount))
                .thenReturn(promise((ITradeContract)purchaseContract));

        //setup attribute values for the contract
        when(purchaseContract.getState()).thenReturn(promise(state));

        if(!isSeller)
        {
            when(purchaseContract.getSeller()).thenReturn(promise("other_account"));
            when(purchaseContract.getBuyer()).thenReturn(promise(selectedAccount));
        }else{
            when(purchaseContract.getSeller()).thenReturn(promise(selectedAccount));
            when(purchaseContract.getBuyer()).thenReturn(promise("other_account"));
        }

        //setup the start intents
        Intent startIntent = new Intent();
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, ContractType.Purchase);
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, purchaseContract.getContractAddress());

        //act
        rule.launchActivity(startIntent);

        //assert

        //assert the states of the buttons
        if(enabledButtonId == R.id.abort_button)
        {
            onView(withId(R.id.abort_button)).check(matches(isEnabled()));
        }else{
            onView(withId(R.id.abort_button)).check(matches(not(isEnabled())));
        }

        if(enabledButtonId == R.id.buy_button)
        {
            onView(withId(R.id.buy_button)).check(matches(isEnabled()));
        }else{
            onView(withId(R.id.buy_button)).check(matches(not(isEnabled())));
        }

        if(enabledButtonId == R.id.confirm_button)
        {
            onView(withId(R.id.confirm_button)).check(matches(isEnabled()));
        }else{
            onView(withId(R.id.confirm_button)).check(matches(not(isEnabled())));
        }

        rule.getActivity().finish();
    }

    /**
     * Verifies the button states for the {@link RentContractDetailFragment}
     */
    private void onCreate_WhenRentContractInState_ThenCorrectButtonEnabled(ContractState state, boolean isSeller, Integer enabledButtonId) throws Exception {
        //arrange

        //setup service call
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Rent, rentContract.getContractAddress(), selectedAccount))
                .thenReturn(promise((ITradeContract)rentContract));


        //setup attribute values for the contract
        when(rentContract.getState()).thenReturn(promise(state));
        if(!isSeller)
        {
            when(rentContract.getSeller()).thenReturn(promise("other_account"));
            when(rentContract.getBuyer()).thenReturn(promise(selectedAccount));
        }else{
            when(rentContract.getSeller()).thenReturn(promise(selectedAccount));
            when(rentContract.getBuyer()).thenReturn(promise("other_account"));
        }

        //setup start intent for activity
        Intent startIntent = new Intent();
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, ContractType.Rent);
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, rentContract.getContractAddress());

        //act
        rule.launchActivity(startIntent);

        //assert

        //assert the button states
        if(enabledButtonId == R.id.abort_button)
        {
            onView(withId(R.id.abort_button)).check(matches(isEnabled()));
        }else{
            onView(withId(R.id.abort_button)).check(matches(not(isEnabled())));
        }

        if(enabledButtonId == R.id.rent_button)
        {
            onView(withId(R.id.rent_button)).check(matches(isEnabled()));
        }else{
            onView(withId(R.id.rent_button)).check(matches(not(isEnabled())));
        }

        if(enabledButtonId == R.id.reclaim_button)
        {
            onView(withId(R.id.reclaim_button)).check(matches(isEnabled()));
        }else{
            onView(withId(R.id.reclaim_button)).check(matches(not(isEnabled())));
        }

        if(enabledButtonId == R.id.return_button)
        {
            onView(withId(R.id.return_button)).check(matches(isEnabled()));
        }else{
            onView(withId(R.id.return_button)).check(matches(not(isEnabled())));
        }

        rule.getActivity().finish();
    }

    /**
     * Checks that the interaction view is invisible when the contract requires verification and does not
     * have a user profile.
     */
    @Test
    public void onCreate_IfIdentityNotVerified_ThenContractInteractionInvisible() throws Exception {
        //arrange
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Purchase, purchaseContract.getContractAddress(), selectedAccount))
                .thenReturn(promise((ITradeContract)purchaseContract));

        when(purchaseContract.getVerifyIdentity()).thenReturn(promise(true));
        when(purchaseContract.getUserProfile()).thenReturn(new UserProfile());

        //setup start intent
        Intent startIntent = new Intent();
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, ContractType.Purchase);
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, purchaseContract.getContractAddress());

        //act
        rule.launchActivity(startIntent);

        //assert
        onView(withId(R.id.contract_interactions)).check(matches(not(isDisplayed())));
    }

    /**
     * Checks that the {@link ProfileFragment} is added after the result of the {@link QrScanningActivity}
     * is returned.
     *
     * Note: This test may fail when executed in series with the other tests. It will however pass when executed alone...
     */
    @Test
    public void onScanProfile_AfterProfileScanned_ThenProfileFragmentAddedAndProfileSaved() throws Exception {
        //arrange
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Purchase, purchaseContract.getContractAddress(), selectedAccount))
                .thenReturn(promise((ITradeContract)purchaseContract));

        when(purchaseContract.getVerifyIdentity()).thenReturn(promise(true));
        UserProfile userProfile = new UserProfile();
        when(purchaseContract.getUserProfile()).thenReturn(userProfile);

        //setup result intent of QrScanning activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(QrScanningActivity.MESSAGE_PROFILE_DATA, new GsonSerializationService().serialize(testProfile));
        Instrumentation.ActivityResult scanResult = new Instrumentation.ActivityResult(ContractDetailActivity.SCAN_PROFILE_INFO_REQUEST, resultIntent);
        Instrumentation.ActivityMonitor activityMonitor =  getInstrumentation().addMonitor(QrScanningActivity.class.getName(), scanResult, true);

        //setup start intent
        Intent startIntent = new Intent();
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, ContractType.Purchase);
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, purchaseContract.getContractAddress());

        //act
        rule.launchActivity(startIntent);
        onView(withId(R.id.action_scan_profile)).perform(click());

        //wait for scan activity
        getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5);

        //assert contract is set on contract and contract is saved
        ArgumentCaptor<UserProfile> argumentCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(purchaseContract).setUserProfile(argumentCaptor.capture());
        assertEquals(new GsonSerializationService().serialize(argumentCaptor.getValue()), new GsonSerializationService().serialize(testProfile));

        verify(context.getServiceProvider().getContractService()).saveContract(purchaseContract, selectedAccount);

        //assert profile fragment added and read-only
        ProfileFragment profileFragment = (ProfileFragment)rule.getActivity().getFragmentManager().findFragmentByTag("Profile");
        assertNotNull(profileFragment);
        assertEquals(profileFragment.getMode(), ProfileFragment.ProfileMode.ReadOnly);
    }

    /**
     * Checks that the received {@link UserProfile} from the {@link P2pExportDialog} is saved and the
     * {@link ProfileFragment} is displayed
     */
    @Test
    public void onContractExport_WhenUserProfileReceived_ThenProfileSavedAndProfileTabAdded() throws Throwable {
        //arrange
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Purchase, purchaseContract.getContractAddress(), selectedAccount))
                .thenReturn(promise((ITradeContract)purchaseContract));

        when(purchaseContract.getVerifyIdentity()).thenReturn(promise(true));
        when(purchaseContract.getUserProfile()).thenReturn(new UserProfile());

        //setup start intent
        Intent startIntent = new Intent();
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, ContractType.Purchase);
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, purchaseContract.getContractAddress());

        //act
        rule.launchActivity(startIntent);

        //execute callback handler
        rule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rule.getActivity().onContractDataExchanged(testProfile);
            }
        });

        //assert contract is set on contract and contract is saved
        ArgumentCaptor<UserProfile> argumentCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(purchaseContract).setUserProfile(argumentCaptor.capture());
        assertEquals(new GsonSerializationService().serialize(argumentCaptor.getValue()), new GsonSerializationService().serialize(testProfile));

        verify(context.getServiceProvider().getContractService()).saveContract(purchaseContract, selectedAccount);

        //assert profile fragment added and read-only
        ProfileFragment profileFragment = (ProfileFragment)rule.getActivity().getFragmentManager().findFragmentByTag("Profile");
        assertNotNull(profileFragment);
        assertEquals(profileFragment.getMode(), ProfileFragment.ProfileMode.ReadOnly);
    }
}
