package id.co.fxcorp.db;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.MutableData;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlaceModel extends Model {

    public static final String PLACE_ID = "place_id";
    public static final String NAME     = "name";
    public static final String TYPE     = "type";
    public static final String DESCRIPTION = "description";
    public static final String ADDRESS = "address";
    public static final String LATLNG  = "latlng";
    public static final String WORK_HOUR  = "work_hour";
    public static final String OWNER   = "owner";
    public static final String ONLINE  = "online";
    public static final String NUMBER_CURRENT  = "number_current";
    public static final String NUMBER_LAST     = "number_last";
    public static final String DURATION  = "duration";
    public static final String PHOTO  = "photo";

    public PlaceModel() {
        super();
    }

    public PlaceModel(DataSnapshot data) {
        super(data);
    }

    public PlaceModel(MutableData data) {
        super();
        putAll((Map<String, Object>)data.getValue());
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

    public long getLong(String key) {
        Object value = get(key);
        if (value == null) {
            return 0;
        }
        return (long) value;
    }


    public final String getPlaceId() {
        return getString(PLACE_ID);
    }

    public final String getName() {
        return getString(NAME);
    }

    public final String getType() {
        return getString(TYPE);
    }

    public final String getDescription() {
        return getString(DESCRIPTION);
    }

    public final String getAddress() {
        return getString(ADDRESS);
    }

    public final LatLng getLatLng() {
        List<Double> list = (List) get(LATLNG);
        if (list == null) {
            return null;
        }
        return new LatLng(list.get(0), list.get(1));
    }

    public final List<String> getWorkHour() {
        List<String> list = (List) get(WORK_HOUR);
        return list;
    }

    public final boolean isOnline() {
        return getLong(ONLINE) == 1;
    }

    public final long getNumberCurrent() {
        return getLong(NUMBER_CURRENT);
    }

    public final String getOwner() {
        return getString(OWNER);
    }

    public final long getDuration() {
        return getLong(DURATION);
    }

    public final long getNumberLast() {
        return getLong(NUMBER_LAST);
    }

    public final String getPhoto() {
        return getString(PHOTO);
    }


}
