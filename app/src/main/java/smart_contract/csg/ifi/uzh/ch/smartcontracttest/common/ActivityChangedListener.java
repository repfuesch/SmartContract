package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by flo on 23.06.17.
 */

public interface ActivityChangedListener {

    void onActivityResumed(AppCompatActivity activity);
    void onActivityStopped(AppCompatActivity activity);
}
