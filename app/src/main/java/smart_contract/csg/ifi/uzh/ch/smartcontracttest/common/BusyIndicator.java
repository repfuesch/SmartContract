package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by flo on 11.06.17.
 */

public class BusyIndicator {

    private static Map<LinearLayout, ProgressBar> activeIndicators;
    private static Handler handler;

    static{
        activeIndicators = new HashMap<>();
        handler = new Handler(Looper.getMainLooper());
    }

    public static void show(final LinearLayout layout)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<layout.getChildCount(); ++i)
                {
                    layout.getChildAt(i).setVisibility(View.GONE);
                }

                layout.setGravity(Gravity.CENTER);
                ProgressBar progressBar = new ProgressBar(layout.getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
                progressBar.setLayoutParams(layoutParams);
                layout.addView(progressBar);

                activeIndicators.put(layout, progressBar);
            }
        });
    }

    public static void hide(final LinearLayout layout)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(!activeIndicators.containsKey(layout))
                    return;

                layout.removeView(activeIndicators.get(layout));
                activeIndicators.remove(layout);

                for(int i=0;i<layout.getChildCount(); ++i)
                {
                    layout.getChildAt(i).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private static void runOnUiThread(Runnable runnable)
    {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
