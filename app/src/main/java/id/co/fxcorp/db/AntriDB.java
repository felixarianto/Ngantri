package id.co.fxcorp.db;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;

public class AntriDB {

    public static final String ANTRIAN = "ANTRIAN";

    public static Query getItem(String place_id, long number) {
        return FirebaseDatabase.getInstance()
                .getReference(ANTRIAN)
                .child(place_id + "-" + number);
    }

    public static Task<Void> insert(AntriModel model) {
        model.id = model.place_id + "-" + model.number;
        return FirebaseDatabase.getInstance()
                .getReference(ANTRIAN)
                .child(model.place_id + "-" + model.number).setValue(model);
    }

    public static Query getMyAntrian(String cust_id) {
        return FirebaseDatabase.getInstance()
                .getReference(ANTRIAN)
                .orderByChild("cust_id").equalTo(cust_id);
    }

    public static Task<Void> call(String antri_id, long call_count, String call_msg) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(ANTRIAN);
        HashMap<String, Object> map = new HashMap<>();
        map.put("call_count", call_count);
        map.put("call_msg", call_msg);
        return ref.child(antri_id).updateChildren(map);
    }


}
