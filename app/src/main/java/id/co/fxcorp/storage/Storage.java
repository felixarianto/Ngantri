package id.co.fxcorp.storage;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import id.co.fxcorp.ngantri.AppInfo;

public class Storage {

    private static final String PATH_IMAGES = "images/";

    public static StorageReference images(String filename) {
        FirebaseStorage fs = FirebaseStorage.getInstance();
        return fs.getReference()
                .child(AppInfo.getUserId())
                .child(PATH_IMAGES + filename);
    }

    public static StorageReference images(String userid, String filename) {
        FirebaseStorage fs = FirebaseStorage.getInstance();
        return fs.getReference()
                .child(userid)
                .child(PATH_IMAGES + filename);
    }
}
