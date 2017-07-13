package smart_contract.csg.ifi.uzh.ch.smartcontracttest.mocks;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnitRunner;

/**
 * Created by flo on 13.07.17.
 */

public class CustomInstrumentationTestRunner extends AndroidJUnitRunner {

    @Override
    @NonNull
    public Application newApplication(@NonNull ClassLoader cl,
                                      @NonNull String className,
                                      @NonNull Context context)
            throws InstantiationException,
            IllegalAccessException,
            ClassNotFoundException {
        return Instrumentation.newApplication(TestAppContext.class, context);
    }
}
