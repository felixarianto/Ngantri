package id.co.fxcorp.db;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import id.co.fxcorp.util.DateUtil;

public class AntriDB {

    public static final String TAG = "AntriDB";
    public static final String ANTRIAN = "ANTRIAN";

    public static Query getItem(long time, String place_id, long number) {
        return FirebaseDatabase.getInstance()
                .getReference(ANTRIAN)
                .child(place_id + "-" + DateUtil.formatDateReverse(time) + "-" + number);
    }

    public static Task<Void> insert(AntriModel model) {
        model.id = model.place_id + "-" + DateUtil.formatDateReverse(model.time) + "-" + model.number;
        return FirebaseDatabase.getInstance()
                .getReference(ANTRIAN)
                .child(model.id).setValue(model);
    }

    public static void remove(String place_id) {
        getAntriListAtPlace(place_id).addChildEventListener(new ChildEvent(){
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    dataSnapshot.getRef().removeValue();
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        });
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

    public static Task<Void> setComplete(final String place_id, String antri_id, String status) {
        PlaceDB.getPlace(place_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    PlaceModel place = new PlaceModel(dataSnapshot);
                    PlaceDB.setNumberQty(place.getPlaceId(), place.getNumberQty() - 1, place.getNumberLast());
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(ANTRIAN);
        HashMap<String, Object> map = new HashMap<>();
        map.put("status", status);
        return ref.child(antri_id).updateChildren(map);
    }

}
