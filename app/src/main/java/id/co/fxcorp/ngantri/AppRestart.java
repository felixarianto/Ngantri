package id.co.fxcorp.ngantri;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppRestart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!AppService.isCreated) {
            context.startService(new Intent(context, AppService.class));
        }
    }

}
