package id.co.fxcorp.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class MapStatic {
//       Zoom Level
//        1: World
//        5: Landmass/continent
//        10: City
//        15: Streets
//        20: Buildings

    public static String getImageUrl(Context context, double latitude, double longitude) {
        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle       bundle = app.metaData;
            String key = bundle.getString("com.google.android.geo.API_KEY");
            return "https://maps.googleapis.com/maps/api/staticmap?center=" + latitude + ","  + longitude + "&zoom=17&size=900x300&maptype=roadmap&markers=color:red|" + latitude + ","  + longitude + "&key=" + key;
        } catch (Exception e) {
            Log.e("MapStatic", "getImageUrl", e);
        }
        return null;
    }

}
