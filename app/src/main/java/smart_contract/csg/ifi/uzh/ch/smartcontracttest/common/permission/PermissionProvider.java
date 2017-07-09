package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.permission;

/**
 * Created by flo on 09.07.17.
 */

public interface PermissionProvider {

    String READ_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    String CAMERA = "android.permission.CAMERA";

    boolean hasPermission(String permission);
    void requestPermission(String permission);
}
