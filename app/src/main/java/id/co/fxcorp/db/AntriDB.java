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

    public static Query getMyAntriList() {
        return FirebaseDatabase.getInstance()
                .getReference(ANTRIAN)
                .orderByChild("cust_id").equalTo(UserDB.MySELF.id);
    }

    public static Query getAntriListAtPlace(String place_id) {
        return FirebaseDatabase.getInstance()
                .getReference(ANTRIAN)
                .orderByChild("place_id").equalTo(place_id);
    }

    public static Task<Void> call(String antri_id, long call_count, String call_msg) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(ANTRIAN);
        HashMap<String, Object> map = new HashMap<>();
        map.put("call_count", call_count);
        map.put("call_msg", call_msg);
        return ref.child(antri_id).updateChildren(map);
    }

    public static Task<Void> setStatus(String antri_id, String status) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(ANTRIAN);
        HashMap<String, Object> map = new HashMap<>();
        map.put("status", status);
        return ref.child(antri_id).updateChildren(map);
    }

}
