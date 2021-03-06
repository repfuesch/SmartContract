package ch.uzh.ifi.csg.smartcontract.app.common.permission;

import ch.uzh.ifi.csg.smartcontract.app.common.ActivityChangedListener;

/**
 * Interface to check and request Application Permissions at runtime.
 */
public interface PermissionProvider extends ActivityChangedListener {

    String READ_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    String CAMERA = "android.permission.CAMERA";

    /**
     * Checks if the application has the specified Android permission
     * @param permission
     * @return
     */
    boolean hasPermission(String permission);

    /**
     * Requests the specified Android permission.
     *
     * @param permission
     */
    void requestPermission(String permission);
}
