package id.co.fxcorp.db;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class AntriDB {

    public static final String ANTRIAN = "ANTRIAN";

    public static Query getItem(String place_id, long number) {
        return FirebaseDatabase.getInstance()
                .getReference(ANTRIAN)
                .child(place_id).child(number + "");
    }

    public static Task<Void> insert(String place_id, AntriModel model) {
        return FirebaseDatabase.getInstance()
                .getReference(ANTRIAN)
                .child(place_id).child(model.number + "").setValue(model);
    }

}
