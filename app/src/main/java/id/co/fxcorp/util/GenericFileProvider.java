package id.co.fxcorp.util;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;

public class GenericFileProvider extends FileProvider {

    public static Uri getUriForFile(Context context, String path) {
        return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", new File(path));
    }

}
