package smart_contract.csg.ifi.uzh.ch.smartcontracttest;

import android.content.Intent;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import ch.uzh.ifi.csg.contract.datamodel.UserProfile;
import ch.uzh.ifi.csg.contract.service.serialization.GsonSerializationService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.ViewHelper.CustomViewActions;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.broadcast.LocalBroadcastService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.setting.SettingProviderImpl;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.mocks.TestAppContext;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile.ProfileActivity;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.profile.ProfileFragment;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Instrumented tests for the {@link ProfileActivity} class
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ProfileActivityTest extends InstrumentedTestBase {

    private static Iterable<Integer> fieldIds = Arrays.asList(
            new Integer[] {R.id.field_profile_first_name, R.id.field_profile_last_name, R.id.field_profile_street, R.id.field_profile_zip, R.id.field_profile_region, R.id.field_profile_country, R.id.field_profile_city, R.id.field_profile_phone, R.id.field_profile_email });
    @Before
    public void setup() throws Exception {
        super.setup();
    }

    @Rule
    public ActivityTestRule<ProfileActivity> rule  = new  ActivityTestRule<>(ProfileActivity.class, true, false);

    /**
     * Checks that the {@link ProfileFragment} fields are initialized with the values of the selected account
     * after creation of the Activity
     */
    @Test
    public void onCreate_WhenCreated_ThenProfileForSelectedAccountLoadedAndDisplayedCorrectly()
    {
        //arrange
        when(context.getServiceProvider().getAccountService().getAccountProfile(selectedAccount)).thenReturn(testProfile);

        //act
        rule.launchActivity(new Intent());

        //assert
        ProfileFragment profileFragment = (ProfileFragment)rule.getActivity().getFragmentManager().findFragmentById(R.id.profile_fragment);
        assertNotNull(profileFragment);

        //assert field values
        onView(withId(R.id.field_profile_first_name)).check(matches(withText(testProfile.getVCard().getStructuredName().getGiven())));
        onView(withId(R.id.field_profile_last_name)).check(matches(withText(testProfile.getVCard().getStructuredName().getFamily())));
        onView(withId(R.id.field_profile_street)).check(matches(withText(testProfile.getVCard().getAddresses().get(0).getStreetAddress())));
        onView(withId(R.id.field_profile_zip)).check(matches(withText(testProfile.getVCard().getAddresses().get(0).getPostalCode())));
        onView(withId(R.id.field_profile_region)).check(matches(withText(testProfile.getVCard().getAddresses().get(0).getRegion())));
        onView(withId(R.id.field_profile_country)).check(matches(withText(testProfile.getVCard().getAddresses().get(0).getCountries().get(0))));
        onView(withId(R.id.field_profile_city)).check(matches(withText(testProfile.getVCard().getAddresses().get(0).getLocality())));
        onView(withId(R.id.field_profile_phone)).check(matches(withText(testProfile.getVCard().getTelephoneNumbers().get(0).getText())));
        onView(withId(R.id.field_profile_email)).check(matches(withText(testProfile.getVCard().getEmails().get(0).getValue())));
    }

    /**
     * Checks that the {@link ProfileFragment} is reloaded when the settings change
     */
    @Test
    public void onSettingsChanged_WhenSettingsChange_ThenProfileInformationReloaded()
    {
        //arrange
        when(context.getServiceProvider().getAccountService().getAccountProfile(selectedAccount)).thenReturn(testProfile);

        //replace BroadcastService with real implementation
        TestAppContext.BroadCastService = new LocalBroadcastService(context.getContext());

        //act
        rule.launchActivity(new Intent());

        //send broadcast intent
        context.getBroadCastService().sendBroadcast(new Intent(SettingProviderImpl.ACTION_SETTINGS_CHANGED));
        onView(isRoot()).perform(CustomViewActions.waitFor(500));

        //assert
        verify(context.getServiceProvider().getAccountService(), times(2)).getAccountProfile(selectedAccount);
    }

    /**
     *  verifies that the profile cannot be saved when at least one field is not filled out.
     */
    @Test
    public void onEdit_WhenEdited_ThenProfileCannotBeSavedWhenFieldIsMissing()
    {
        //arrange
        when(context.getServiceProvider().getAccountService().getAccountProfile(selectedAccount)).thenReturn(testProfile);

        //act
        rule.launchActivity(new Intent());

        //assert
        for(Integer id : fieldIds)
        {
            String text = ((TextView)rule.getActivity().findViewById(id)).getText().toString();
            onView(withId(id)).perform(replaceText(""));
            onView(withId(R.id.action_save_profile)).check(matches(not(isEnabled())));
            onView(withId(id)).perform(typeText(text)).perform(closeSoftKeyboard());
            onView(withId(R.id.action_save_profile)).check(matches(isEnabled()));
        }
    }

    /**
     * Verifies that the profile is saved when the user hits the 'save' button
     */
    @Test
    public void onSave_WhenSaveClicked_ThenProfileSaved()
    {
        //arrange
        when(context.getServiceProvider().getAccountService().getAccountProfile(selectedAccount)).thenReturn(testProfile);

        //act
        rule.launchActivity(new Intent());
        onView(withId(R.id.action_save_profile)).perform(click());

        //assert
        ArgumentCaptor<UserProfile> argumentCaptor =ArgumentCaptor.forClass(UserProfile.class);
        verify(context.getServiceProvider().getAccountService()).saveAccountProfile(any(String.class), argumentCaptor.capture());
        assertEquals(new GsonSerializationService().serialize(argumentCaptor.getValue()), new GsonSerializationService().serialize(testProfile));
    }
}
