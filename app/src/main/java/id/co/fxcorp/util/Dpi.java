package id.co.fxcorp.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class Dpi {

    static float dpi = 0;
    public static void init(Context context) {
        dpi = context.getResources().getDisplayMetrics().density;
    }

    public static int px(int dp){
        return (int) ((dp * dpi) + 0.5f);
    }

    public static int dp(int px){
        return (int) ((px / dpi) + 0.5f);
    }

}
