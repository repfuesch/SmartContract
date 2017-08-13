package ch.uzh.ifi.csg.smartcontract.app.mocks;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnitRunner;

/**
 * Custom {@link AndroidJUnitRunner} class that creates a new Application with the
 * {@link TestAppContext}
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
