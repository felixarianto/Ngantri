package id.co.fxcorp.db;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;

import java.util.Set;

public class PlaceModel extends Model {

    public static final String PLACE_ID = "place_id";
    public static final String NAME     = "name";
    public static final String TYPE     = "type";
    public static final String DESCRIPTION = "description";
    public static final String ADDRESS = "address";
    public static final String LATLNG  = "latlng";
    public static final String WORK_HOUR  = "work_hour";
    public static final String ONLINE  = "online";
    public static final String NUMBER  = "number";
    public static final String OWNER   = "owner";

    public PlaceModel() {
        super();
    }

    public PlaceModel(DataSnapshot data) {
        super(data);
    }

    public void setGroup(LatLng latlng) {
        Set<String> keys = keySet();
        for (String k : keys) {
            if ("g_place".equals(get(k))) {
                remove(k);
            }
        }
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                put((String.format("%.2f", latlng.latitude + (i * 0.01)) + ":" + String.format("%.2f", latlng.longitude+ (j * 0.01))).replaceAll("\\.", "x"), "g_place");
            }
        }
    }

    public String getString(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return (String) value;
    }

    public int getInt(String key) {
        Object value = get(key);
        if (value == null) {
            return 0;
        }
        return (int) value;
    }



}
