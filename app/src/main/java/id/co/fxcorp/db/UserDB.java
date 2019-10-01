package id.co.fxcorp.db;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import id.co.fxcorp.ngantri.MainActivity;
import id.co.fxcorp.signin.SignInActivity;

public class UserDB {

    private static String USER = "USER";

    public static UserModel MySELF;

    public static Query login(String email) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(USER);
        return ref.orderByChild("email").equalTo(email.toLowerCase());
    }

    public static Task<Void> insert(UserModel model) {
        return FirebaseDatabase.getInstance()
                .getReference(USER)
                .child(model.id).setValue(model);
    }

    public static Query getUser(String userid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(USER);
        return ref.child(userid);
    }


    public static boolean checkLoginState(final Activity activity) {
        if (MySELF == null) {
            AlertDialog.Builder alert = new AlertDialog.Builder(activity);
            alert.setMessage("Mohon Maaf, Anda harus Login terlebih dahulu");
            alert.setPositiveButton("LOGIN SEKARANG", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    activity.startActivity(new Intent(activity, SignInActivity.class));
                }
            });
            alert.setNegativeButton("NANTI SAJA", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            alert.create().show();
            return false;
        }
        else {
            return true;
        }
    }
}
