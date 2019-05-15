package id.co.fxcorp.db;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import id.co.fxcorp.ngantri.AppInfo;

public class PlaceDB {

    public static final String PLACE = "PLACE";

    public static Query getNearby(LatLng latlng) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PLACE);
        String g_place = (String.format("%.2f", latlng.latitude) + ":" + String.format("%.2f", latlng.longitude)).replaceAll("\\.", "x");

        System.out.println("XXX g_place " + g_place);
        return ref.orderByChild(g_place).equalTo("g_place");
    }

    public static Task<Void> insert(PlaceModel value) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(PLACE);
        return ref.child(value.getString(PlaceModel.PLACE_ID)).setValue(value);
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
