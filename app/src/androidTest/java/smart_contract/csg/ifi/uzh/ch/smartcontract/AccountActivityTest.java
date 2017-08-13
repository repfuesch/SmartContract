package smart_contract.csg.ifi.uzh.ch.smartcontract;

import android.content.Intent;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.datamodel.Account;
import ch.uzh.ifi.csg.contract.service.account.AccountService;
import smart_contract.csg.ifi.uzh.ch.smartcontract.ViewHelper.CustomViewActions;
import smart_contract.csg.ifi.uzh.ch.smartcontract.account.AccountActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontract.account.AccountDialogFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontract.account.AccountFragment;
import smart_contract.csg.ifi.uzh.ch.smartcontract.account.AccountRecyclerViewAdapter;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.setting.SettingProvider;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Instrumented tests for the {@link AccountActivity} code
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class AccountActivityTest extends InstrumentedTestBase {

    private List<Account> accountList;

    @Rule
    public ActivityTestRule<AccountActivity> rule  = new  ActivityTestRule<>(AccountActivity.class, true, false);

    @Before
    public void setup() throws Exception {
        super.setup();

        //setup account list
        AccountService accountService = context.getServiceProvider().getAccountService();
        Account[] accounts = new Account[]{ new Account("id1", "label1", "walletFile1"), new Account("id2", "label2", "walletFile2") };
        accountList = Arrays.asList(accounts);
        when(accountService.getAccounts()).thenReturn(Async.toPromise(new Callable<List<Account>>() {
            @Override
            public List<Account> call() throws Exception {
                return accountList;
            }
        }));

        //make sure selected account is not set
        SettingProvider settingProvider = context.getSettingProvider();
        when(settingProvider.getSelectedAccount()).thenReturn("");

        rule.launchActivity(new Intent());
    }

    /**
     * Checks that the account list is shown after the Activity has been created
     */
    @Test
    public void Created_WhenCreated_ThenAccountListLoaded()
    {
        AccountActivity accountActivity = rule.getActivity();
        AccountFragment accountFragment = (AccountFragment)accountActivity.getFragmentManager().findFragmentById(R.id.account_fragment);
        RecyclerView accountList = (RecyclerView) accountFragment.getView().findViewById(R.id.account_list);
        AccountRecyclerViewAdapter listAdapter = (AccountRecyclerViewAdapter)accountList.getAdapter();

        assertThat(listAdapter.getItemCount(), is(2));
    }

    /**
     * Tests that the account is changed when an account is unlocked with the correct credentials
     */
    @Test
    public void Login_WhenPasswordMatches_ThenAccountChanged()
    {
        //arrange
        when(context.getServiceProvider().getAccountService().unlockAccount(any(Account.class), any(String.class))).thenReturn(promise(true));

        //act
        onView(withId(R.id.account_list)).perform(actionOnItemAtPosition(0, click()));
        onView(withId(R.id.account_list)).perform(actionOnItemAtPosition(0, CustomViewActions.typeTextIntoChildViewWithId(R.id.account_password_field, "password")));
        onView(withId(R.id.account_list)).perform(actionOnItemAtPosition(0, CustomViewActions.clickChildViewWithId(R.id.account_login_button)));

        //assert
        ArgumentCaptor<Intent> argument1 = ArgumentCaptor.forClass(Intent.class);

        //check that account change is broadcasted with correct intent
        verify(context.getBroadCastService()).sendBroadcast(argument1.capture());
        assertThat(argument1.getValue().getAction(), is(AccountActivity.ACTION_ACCOUNT_CHANGED));
        assertThat(argument1.getValue().getStringExtra(AccountActivity. MESSAGE_ACCOUNT_CHANGED), is("id1"));
    }

    /**
     * Tests that an error message is shown when the user attempts to unlock an account with the wrong password
     */
    @Test
    public void Login_WhenPasswordDoesNotMatch_ThenErrorShown()
    {
        //arrange
        when(context.getServiceProvider().getAccountService().unlockAccount(any(Account.class), any(String.class))).thenReturn(Async.toPromise(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return false;
            }
        }));

        //act
        onView(withId(R.id.account_list)).perform(actionOnItemAtPosition(0, click()));
        onView(withId(R.id.account_list)).perform(actionOnItemAtPosition(0, CustomViewActions.typeTextIntoChildViewWithId(R.id.account_password_field, "password")));
        onView(withId(R.id.account_list)).perform(actionOnItemAtPosition(0, CustomViewActions.clickChildViewWithId(R.id.account_login_button)));

        //assert
        verify(context.getMessageService(), times(1)).showErrorMessage(any(String.class));
    }

    /**
     * Verifies that a new account is created and selected when the user fills out the {@link AccountDialogFragment}
     */
    @Test
    public void CreateAccount_WhenClicked_ThenDialogOpensAndAccountAdded() throws Throwable {
        //arrange
        when(context.getServiceProvider().getAccountService().createAccount(any(String.class), any(String.class)))
                .thenReturn(Async.toPromise(new Callable<Account>() {
                    @Override
                    public Account call() throws Exception {
                        return new Account("id", "account", "walletfile");
                    }
                }));

        //act
        onView(withId(R.id.account_add_button)).perform(click());
        onView(withId(R.id.field_account_name)).perform(typeText("account"));
        onView(withId(R.id.field_password)).perform(typeText("password")).perform(closeSoftKeyboard());
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click());

        //assert
        verify(context.getServiceProvider().getAccountService()).createAccount("account", "password");
        verify(context.getBroadCastService()).sendBroadcast(any(Intent.class));
    }
}
