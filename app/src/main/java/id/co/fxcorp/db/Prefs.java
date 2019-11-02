package id.co.fxcorp.db;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static String PREF = "FaFwPref";
    public static void setAccount(Context context, String email, String password) {
        SharedPreferences pref = context.getSharedPreferences(PREF, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("acc_email",    email);
        editor.putString("acc_password", password);
        editor.commit();
    }
    public static String[] getAccount(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF, 0); // 0 - for private mode
        String email    = pref.getString("acc_email", "");
        String password = pref.getString("acc_password", "");

        if (email.isEmpty() || password.isEmpty()) {
            return null;
        }
        return new String[]{email, password};
    }

    public static void setAutoStartAsking(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF, 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("auto_start",    true);
        editor.commit();
    }
    public static boolean getAutoStartAsking(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF, 0); // 0 - for private mode
        return pref.getBoolean("auto_start", false);
    }

}
