package smart_contract.csg.ifi.uzh.ch.smartcontract.common.permission;

import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;

import smart_contract.csg.ifi.uzh.ch.smartcontract.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.ActivityChangedListener;


/**
 * {@link PermissionProvider} implementation. Implements the {@link ActivityChangedListener} to keep
 * track of the active Activity and uses it to check and request Android permissions.
 */
public class PermissionProviderImpl implements PermissionProvider, ActivityChangedListener
{
    private ActivityBase activity;
    private final int permissionRequestCode = 200;

    private boolean canRevokePermissions(){
        return(Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestPermission(String permission)
    {
        if(hasPermission(permission))
            return;

        String[] perms = {permission};
        ActivityCompat.requestPermissions(activity, perms, permissionRequestCode);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean hasPermission(String permission){

        if(canRevokePermissions()){

            return(activity.checkSelfPermission(permission)== PackageManager.PERMISSION_GRANTED);
        }

        return true;
    }

    @Override
    public void onActivityResumed(ActivityBase activity) {

        this.activity = activity;
    }

    @Override
    public void onActivityStopped(ActivityBase activity) {

    }
}
