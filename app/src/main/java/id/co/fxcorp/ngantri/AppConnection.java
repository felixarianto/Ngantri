package id.co.fxcorp.ngantri;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class AppConnection extends BroadcastReceiver {

    private static String TAG = "AppConnection";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        try {
//            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//            if (null != activeNetwork && activeNetwork.isConnected()) {
//                return true;
//            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }



}
