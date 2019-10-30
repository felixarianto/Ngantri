package id.co.fxcorp.ngantri;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AppRestart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!AppService.isCreated) {
            Log.i("AppRestart", "Service Restart Oooooooooooooppppssssss!!!!");
            context.startService(new Intent(context, AppService.class));
        }
        else {
            Log.i("AppRestart", "Service Already Started");
        }
    }

}
