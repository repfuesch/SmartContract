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
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.Callable;
import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.util.Web3Util;
import ch.uzh.ifi.csg.contract.contract.ContractState;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.exchange.Currency;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display.ContractDetailActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display.PurchaseContractDetailFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display.RentContractDetailFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile.ProfileFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.qrcode.QrScanningActivity;

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
 * Created by flo on 15.07.17.
 */

@MediumTest
@RunWith(AndroidJUnit4.class)
public class ContractDetailTest extends InstrumentedTestBase {

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

    @Test
    public void onCreate_WhenCreatedWithPurchaseContractIntent_ThenPurchaseContractDetailFragmentLoadedCorrectly() throws Exception {
        //arrange
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Purchase, purchaseContract.getContractAddress(), selectedAccount))
                .thenReturn(Async.toPromise(new Callable<ITradeContract>() {
                    @Override
                    public ITradeContract call() throws Exception {
                        return purchaseContract;
                    }
                }));

        Intent startIntent = new Intent();
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, ContractType.Purchase);
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, purchaseContract.getContractAddress());

        //act
        ContractDetailActivity activity = rule.launchActivity(startIntent);

        //assert
        assertThat(activity.getFragmentManager().findFragmentByTag("Details"), instanceOf(PurchaseContractDetailFragment.class));

        //assert fields
        onView(withId(R.id.general_title)).check(matches(withText(purchaseContract.getTitle())));
        onView(withId(R.id.general_address)).check(matches(withText(purchaseContract.getContractAddress())));
        BigDecimal priceCurrency = context.getServiceProvider().getExchangeService().convertToCurrency(Web3Util.toEther(purchaseContract.getPrice()), Currency.USD).get();
        onView(withId(R.id.general_price)).check(matches(withText(priceCurrency.toString())));
        onView(withId(R.id.general_description)).check(matches(withText(purchaseContract.getDescription())));
        onView(withId(R.id.general_state)).check(matches(withText(purchaseContract.getState().toString())));
    }

    @Test
    public void onCreate_WhenCreatedWithRentContractIntent_ThenRentContractDetailFragmentLoadedCorrectly() throws Exception {
        //arrange
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Rent, rentContract.getContractAddress(), selectedAccount))
                .thenReturn(Async.toPromise(new Callable<ITradeContract>() {
                    @Override
                    public ITradeContract call() throws Exception {
                        return rentContract;
                    }
                }));

        Intent startIntent = new Intent();
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, ContractType.Rent);
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, rentContract.getContractAddress());

        //act
        ContractDetailActivity activity = rule.launchActivity(startIntent);

        //assert
        assertThat(activity.getFragmentManager().findFragmentByTag("Details"), instanceOf(RentContractDetailFragment.class));

        //assert fields
        onView(withId(R.id.general_title)).check(matches(withText(rentContract.getTitle())));
        onView(withId(R.id.general_address)).check(matches(withText(rentContract.getContractAddress())));
        onView(withId(R.id.general_description)).check(matches(withText(rentContract.getDescription())));
        onView(withId(R.id.general_state)).check(matches(withText(rentContract.getState().toString())));
        BigDecimal depositCurrency = context.getServiceProvider().getExchangeService().convertToCurrency(Web3Util.toEther(rentContract.getDeposit()), Currency.USD).get();
        onView(withId(R.id.rent_deposit)).check(matches(withText(depositCurrency.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())));
        BigDecimal currentFeeCurrency = context.getServiceProvider().getExchangeService().convertToCurrency(Web3Util.toEther(rentContract.getRentingFee()), Currency.USD).get();
        onView(withId(R.id.rent_current_fee)).check(matches(withText(currentFeeCurrency.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString())));
    }

    @Test
    public void onCreate_WhenContractInState_ThenCorrectButtonEnabled() throws Exception {
        for(Object[] parameters : interactionTestParameters)
        {
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

    private void onCreate_WhenPurchaseContractInState_ThenCorrectButtonEnabled(ContractState state, boolean isSeller, Integer enabledButtonId) throws Exception {
        //arrange
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Purchase, purchaseContract.getContractAddress(), selectedAccount))
                .thenReturn(Async.toPromise(new Callable<ITradeContract>() {
                    @Override
                    public ITradeContract call() throws Exception {
                        return purchaseContract;
                    }
                }));

        when(purchaseContract.getState()).thenReturn(state);
        if(!isSeller)
        {
            when(purchaseContract.getSeller()).thenReturn("other_account");
            when(purchaseContract.getBuyer()).thenReturn(selectedAccount);
        }else{
            when(purchaseContract.getSeller()).thenReturn(selectedAccount);
            when(purchaseContract.getBuyer()).thenReturn("other_account");
        }

        Intent startIntent = new Intent();
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, ContractType.Purchase);
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, purchaseContract.getContractAddress());

        //act
        rule.launchActivity(startIntent);

        //assert
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

    private void onCreate_WhenRentContractInState_ThenCorrectButtonEnabled(ContractState state, boolean isSeller, Integer enabledButtonId) throws Exception {
        //arrange
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Rent, rentContract.getContractAddress(), selectedAccount))
                .thenReturn(Async.toPromise(new Callable<ITradeContract>() {
                    @Override
                    public ITradeContract call() throws Exception {
                        return rentContract;
                    }
                }));

        when(rentContract.getState()).thenReturn(state);
        if(!isSeller)
        {
            when(rentContract.getSeller()).thenReturn("other_account");
            when(rentContract.getBuyer()).thenReturn(selectedAccount);
        }else{
            when(rentContract.getSeller()).thenReturn(selectedAccount);
            when(rentContract.getBuyer()).thenReturn("other_account");
        }


        Intent startIntent = new Intent();
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, ContractType.Rent);
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, rentContract.getContractAddress());

        //act
        rule.launchActivity(startIntent);

        //assert
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

    @Test
    public void onCreate_IfIdentityNotVerified_ThenContractInteractionInvisible() throws Exception {
        //arrange
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Purchase, purchaseContract.getContractAddress(), selectedAccount))
                .thenReturn(Async.toPromise(new Callable<ITradeContract>() {
                    @Override
                    public ITradeContract call() throws Exception {
                        return purchaseContract;
                    }
                }));

        when(purchaseContract.getVerifyIdentity()).thenReturn(true);
        when(purchaseContract.getUserProfile()).thenReturn(new UserProfile());

        Intent startIntent = new Intent();
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, ContractType.Purchase);
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, purchaseContract.getContractAddress());

        //act
        rule.launchActivity(startIntent);

        //assert
        onView(withId(R.id.contract_interactions)).check(matches(not(isDisplayed())));
    }

    @Test
    public void onScanProfile_AfterProfileScanned_ThenProfileFragmentAddedAndProfileSaved() throws Exception {
        //arrange
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Purchase, purchaseContract.getContractAddress(), selectedAccount))
                .thenReturn(Async.toPromise(new Callable<ITradeContract>() {
                    @Override
                    public ITradeContract call() throws Exception {
                        return purchaseContract;
                    }
                }));

        when(purchaseContract.getVerifyIdentity()).thenReturn(true);
        UserProfile userProfile = new UserProfile();
        when(purchaseContract.getUserProfile()).thenReturn(userProfile);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(QrScanningActivity.MESSAGE_PROFILE_DATA, testProfile.write());
        Instrumentation.ActivityResult scanResult = new Instrumentation.ActivityResult(ContractDetailActivity.SCAN_PROFILE_INFO_REQUEST, resultIntent);
        Instrumentation.ActivityMonitor activityMonitor =  getInstrumentation().addMonitor(QrScanningActivity.class.getName(), scanResult, true);

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
        assertEquals(argumentCaptor.getValue().getVCard().write(), testProfile.write());

        verify(context.getServiceProvider().getContractService()).saveContract(purchaseContract, selectedAccount);

        //assert profile fragment added and read-only
        ProfileFragment profileFragment = (ProfileFragment)rule.getActivity().getFragmentManager().findFragmentByTag("Profile");
        assertNotNull(profileFragment);
        assertEquals(profileFragment.getMode(), ProfileFragment.ProfileMode.ReadOnly);
    }

    @Test
    public void onContractExport_WhenUserProfileReceived_ThenProfileSavedAndProfileTabAdded() throws Throwable {
        //arrange
        when(context.getServiceProvider().getContractService().loadContract(ContractType.Purchase, purchaseContract.getContractAddress(), selectedAccount))
                .thenReturn(Async.toPromise(new Callable<ITradeContract>() {
                    @Override
                    public ITradeContract call() throws Exception {
                        return purchaseContract;
                    }
                }));

        when(purchaseContract.getVerifyIdentity()).thenReturn(true);
        when(purchaseContract.getUserProfile()).thenReturn(new UserProfile());

        Intent startIntent = new Intent();
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_TYPE, ContractType.Purchase);
        startIntent.putExtra(ContractDetailActivity.EXTRA_CONTRACT_ADDRESS, purchaseContract.getContractAddress());

        //act
        rule.launchActivity(startIntent);
        final UserProfile profile = new UserProfile();
        profile.setVCard(testProfile);

        rule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rule.getActivity().onContractDataExchanged(profile);
            }
        });

        //assert contract is set on contract and contract is saved
        ArgumentCaptor<UserProfile> argumentCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(purchaseContract).setUserProfile(argumentCaptor.capture());
        assertEquals(argumentCaptor.getValue().getVCard().write(), testProfile.write());

        verify(context.getServiceProvider().getContractService()).saveContract(purchaseContract, selectedAccount);

        //assert profile fragment added and read-only
        ProfileFragment profileFragment = (ProfileFragment)rule.getActivity().getFragmentManager().findFragmentByTag("Profile");
        assertNotNull(profileFragment);
        assertEquals(profileFragment.getMode(), ProfileFragment.ProfileMode.ReadOnly);
    }
}
