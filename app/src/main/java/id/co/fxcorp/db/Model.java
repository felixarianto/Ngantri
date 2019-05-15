package id.co.fxcorp.db;

import com.google.firebase.database.DataSnapshot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Model extends ConcurrentHashMap<String, Object> {

    public Model() {

    }
    public Model(DataSnapshot data) {
        putAll((Map<String, Object>)data.getValue());
    }

}
