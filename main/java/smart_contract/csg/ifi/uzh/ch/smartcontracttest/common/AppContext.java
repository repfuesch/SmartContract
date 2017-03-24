package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

/**
 * Class for receiving the Application context without a reference to an Activity.
 */

public class AppContext extends Application
{
    private static AppContext instance;

    public AppContext() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

}

