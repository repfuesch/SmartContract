package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

import android.app.ActivityManager;
import android.os.Handler;
import android.os.Looper;
import android.transition.Visibility;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

/**
 * Created by flo on 11.06.17.
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

                //layout.setGravity(Gravity.CENTER);
                ProgressBar progressBar = new ProgressBar(layout.getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
                progressBar.setLayoutParams(layoutParams);
                progressBar.setTag("progressbar");
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

            for(int i=0;i<layout.getChildCount(); ++i)
            {
                View child = layout.getChildAt(i);
                if((boolean)child.getTag(VISIBLE_TAG))
                {
                    child.setVisibility(View.VISIBLE);
                }
                child.setTag(VISIBLE_TAG, null);
            }

            layout.setGravity((int)layout.getTag(GRAVITY_TAG));
            layout.setTag(GRAVITY_TAG, null);

            }
        });
    }

    private static void runOnUiThread(Runnable runnable)
    {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
