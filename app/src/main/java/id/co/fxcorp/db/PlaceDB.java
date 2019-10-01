package id.co.fxcorp.db;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import id.co.fxcorp.ngantri.AppInfo;
import id.co.fxcorp.util.DateUtil;

public class PlaceDB {

    public static final String PLACE = "PLACE";
    public static final String SEQNO = "SEQNO";

    public static String toGPlace(LatLng latlng) {
        return (String.format("%.2f", latlng.latitude) + ":" + String.format("%.2f", latlng.longitude)).replaceAll("\\.", "x");
    }

    public static LatLng mLastLatLng;
    public static Query getNearby(LatLng latlng) {
        mLastLatLng = latlng;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PLACE);
        String g_place = (String.format("%.2f", latlng.latitude) + ":" + String.format("%.2f", latlng.longitude)).replaceAll("\\.", "x");

        return ref.orderByChild(g_place).equalTo("g_place");
    }

    public static Task<Void> insert(PlaceModel value) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PLACE);
        return ref.child(value.getString(PlaceModel.PLACE_ID)).setValue(value);
    }

    public static Task<Void> remove(String place_id) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PLACE);
        return ref.child(place_id).removeValue();
    }

    public static Task<Void> setNumberCurrent(String place_id, long number) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PLACE);
        HashMap<String, Object> map = new HashMap<>();
        map.put(PlaceModel.NUMBER_CURRENT, number);
        return ref.child(place_id).updateChildren(map);
    }

    public static Task<Void> setOnline(String place_id, boolean online) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PLACE);
        HashMap<String, Object> map = new HashMap<>();
        map.put(PlaceModel.ONLINE, online ? 1L : 0L);
        return ref.child(place_id).updateChildren(map);
    }

    public static Task<Void> setNumberQty(String place_id, long qty, long last) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PLACE);
        HashMap<String, Object> map = new HashMap<>();
        map.put(PlaceModel.NUMBER_QTY,  qty);
        map.put(PlaceModel.NUMBER_LAST, last);
        return ref.child(place_id).updateChildren(map);
    }

    public static void setLastOpen(final String place_id, final long time) {
        FirebaseDatabase.getInstance().getReference(PLACE)
        .child(place_id)
        .child(SEQNO)
        .child(DateUtil.formatDateReverse(time)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long last_number = dataSnapshot.getValue(Long.class);
                if (last_number == null) {
                    last_number = 0l;
                }
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PLACE);
                HashMap<String, Object> map = new HashMap<>();
                map.put(PlaceModel.NUMBER_CURRENT,  0);
                map.put(PlaceModel.NUMBER_LAST,  last_number);
                map.put(PlaceModel.NUMBER_QTY,   last_number);
                map.put(PlaceModel.LAST_OPEN,  time);
                ref.child(place_id).updateChildren(map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static Query getMyPlace() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PLACE);
        return ref.orderByChild(PlaceModel.OWNER).equalTo(AppInfo.getUserId());
    }

    public static Query getPlace(String place_id) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PLACE);
        return ref.child(place_id);
    }


}
