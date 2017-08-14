package ch.uzh.ifi.csg.smartcontract.app.common.controls;

import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to show and hide {@link ProgressBar} components in specified layout containers
 */
public class BusyIndicator {

    private static int VISIBLE_TAG = 121654435;
    private static int GRAVITY_TAG = 545465454;

    private static List<LinearLayout> activeIndicators;
    private static Handler handler;

    static{
        activeIndicators = new ArrayList<LinearLayout>();
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Displays a ProgressBar in the center of the LinearLayout container.
     *
     * @param layout
     */
    public static void show(final LinearLayout layout)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(layout == null)
                    return;

                if (activeIndicators.contains(layout)) {
                    activeIndicators.add(layout);
                    return;
                }else{
                    activeIndicators.add(layout);
                }

                //hide all childs of the layout container but mark components that are not visible
                // with the VISIBLE_TAG
                for(int i=0;i<layout.getChildCount(); ++i)
                {
                    View child = layout.getChildAt(i);

                    if(child.getVisibility() == View.VISIBLE)
                    {
                        child.setTag(VISIBLE_TAG, true);
                    }else{
                        child.setTag(VISIBLE_TAG, false);
                    }

                    child.setVisibility(View.GONE);
                }

                //initialize the ProgressBar
                ProgressBar progressBar = new ProgressBar(layout.getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
                progressBar.setLayoutParams(layoutParams);
                progressBar.setTag("progressbar");

                //Add the ProgressBar and position it in the center of the container
                layout.setTag(GRAVITY_TAG, getGravity(layout));
                layout.setGravity(Gravity.CENTER);
                layout.addView(progressBar);
            }
        });
    }

    private static int getGravity(LinearLayout linearLayout)
    {
        int gravity = -1;

        try {
            final Field staticField = LinearLayout.class.getDeclaredField("mGravity");
            staticField.setAccessible(true);
            gravity =  staticField.getInt(linearLayout);
        }
        catch (NoSuchFieldException e) {}
        catch (IllegalArgumentException e) {}
        catch (IllegalAccessException e) {}

        return gravity;
    }

    /**
     * Removes the ProgressBar from the LinearLayout container and makes its child visible again.
     *
     * @param layout
     */
    public static void hide(final LinearLayout layout)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            if(!activeIndicators.contains(layout))
                return;

            activeIndicators.remove(layout);
            if(activeIndicators.contains(layout))
                return;

            layout.removeView(layout.findViewWithTag("progressbar"));

            for(int i=0;i<layout.getChildCount(); ++i) {
                View child = layout.getChildAt(i);
                if (child.getTag(VISIBLE_TAG) != null && (boolean) child.getTag(VISIBLE_TAG)) {
                    child.setVisibility(View.VISIBLE);
                }
                child.setTag(VISIBLE_TAG, null);
            }

            if(layout.getTag(GRAVITY_TAG) != null)
                layout.setGravity((int)layout.getTag(GRAVITY_TAG));

            layout.setTag(GRAVITY_TAG, null);

            }
        });
    }

    private static void runOnUiThread(Runnable runnable)
    {
        handler.post(runnable);
    }
}
