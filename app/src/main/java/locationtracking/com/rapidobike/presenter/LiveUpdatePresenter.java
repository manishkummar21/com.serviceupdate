package locationtracking.com.rapidobike.presenter;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import locationtracking.com.rapidobike.LocationUpdateService;

public class LiveUpdatePresenter implements ILiveupdate {

    public LiveUpdatePresenter() {
    }

    @Override
    public void startupdate(Context context) {


        if (!isMyServiceRunning(context)) {
            Intent i = new Intent(context, LocationUpdateService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(i);
            else
                context.startService(i);
        }
    }


    private boolean isMyServiceRunning(Context context) {

        //Inorder to prevent multiple instance of service creation checking service is alredy running or not
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationUpdateService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
