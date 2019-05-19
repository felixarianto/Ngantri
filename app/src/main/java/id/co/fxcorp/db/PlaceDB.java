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

import java.util.HashMap;

import id.co.fxcorp.ngantri.AppInfo;

public class PlaceDB {

    public static final String PLACE = "PLACE";

    public static Query getNearby(LatLng latlng) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PLACE);
        String g_place = (String.format("%.2f", latlng.latitude) + ":" + String.format("%.2f", latlng.longitude)).replaceAll("\\.", "x");

        return ref.orderByChild(g_place).equalTo("g_place");
    }

    public static Task<Void> insert(PlaceModel value) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PLACE);
        return ref.child(value.getString(PlaceModel.PLACE_ID)).setValue(value);
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
        map.put(PlaceModel.ONLINE, online ? 1l : 0l);
        return ref.child(place_id).updateChildren(map);
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
