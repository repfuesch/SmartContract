package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

import android.app.ActivityManager;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

/**
 * Created by flo on 11.06.17.
 */

public class BusyIndicator {

    private static int VISIBLE_TAG = 121654435;

    private static List<ViewGroup> activeIndicators;
    private static Handler handler;

    static{
        activeIndicators = new ArrayList<>();
        handler = new Handler(Looper.getMainLooper());
    }

    public static void show(final LinearLayout layout)
    {
        if(layout == null)
            return;

        if(activeIndicators.contains(layout))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

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

                layout.setGravity(Gravity.CENTER);
                ProgressBar progressBar = new ProgressBar(layout.getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
                progressBar.setLayoutParams(layoutParams);
                progressBar.setTag("progressbar");
                layout.addView(progressBar);

                activeIndicators.add(layout);
            }
        });
    }

    public static void hide(final LinearLayout layout)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(!activeIndicators.contains(layout))
                    return;

                layout.removeView(layout.findViewWithTag("progressbar"));

                for(int i=0;i<layout.getChildCount(); ++i)
                {
                    View child = layout.getChildAt(i);
                    if(child.getTag(VISIBLE_TAG) != null && child.getTag(VISIBLE_TAG).equals(true))
                        child.setVisibility(View.VISIBLE);

                    child.setTag(VISIBLE_TAG, null);
                }

                activeIndicators.remove(layout);
            }
        });
    }

    private static void runOnUiThread(Runnable runnable)
    {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
