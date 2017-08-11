package smart_contract.csg.ifi.uzh.ch.smartcontracttest.ViewHelper;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Methods that return custom {@link ViewAction} instances
 */
public class CustomViewActions {

    /**
     * {@link ViewAction} that clicks on a child view with the specified id
     *
     * @param id; the id of the child view
     * @return the ViewAction
     */
    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                v.performClick();
            }
        };
    }

    /**
     * {@link ViewAction} that inserts the specified text into the view with the specified id
     *
     * @param id; the id of the child view
     * @param text: the text to insert
     * @return the ViewAction
     */
    public static ViewAction typeTextIntoChildViewWithId(final int id, final String text) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Insert text into a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TextView textView = (TextView)view.findViewById(id);
                textView.setText(text);
            }
        };
    }

    /**
     * Blocks the test UI thread for a specified amount of time.
     *
     * @param millis: the time in milliseconds
     * @return the ViewAction
     */
    public static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}
