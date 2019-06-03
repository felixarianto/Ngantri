package id.co.fxcorp.signin;

import android.app.Activity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class Account {

    private static String email;

    public static void initAccount(Activity activity ) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account != null) {
            email = account.getEmail();
        }

    }

}
