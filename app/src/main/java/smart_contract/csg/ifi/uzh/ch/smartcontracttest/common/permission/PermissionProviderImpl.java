package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.permission;

import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityChangedListener;


/**
 * Created by flo on 09.07.17.
 */

public class PermissionProviderImpl implements PermissionProvider, ActivityChangedListener
{
    private static PermissionProviderImpl instance;

    public static PermissionProviderImpl create()
    {
        if(instance == null)
            instance = new PermissionProviderImpl();
        return instance;
    }

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
