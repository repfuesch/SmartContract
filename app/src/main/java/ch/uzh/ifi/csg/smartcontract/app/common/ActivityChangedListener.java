package ch.uzh.ifi.csg.smartcontract.app.common;

/**
 * Interface that can be implemented to get notified when an Activity was stopped or has resumed
 */
public interface ActivityChangedListener {

    /**
     * Invoked when an ActivityBase Activity is resumed
     *
     * @param activity
     */
    void onActivityResumed(ActivityBase activity);

    /**
     * Invoked when an ActivityBase Activity is stopped
     *
     * @param activity
     */
    void onActivityStopped(ActivityBase activity);
}
