package id.co.fxcorp.db;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

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


}
