package id.co.fxcorp.ngantri;

import android.app.Application;
import android.content.Intent;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (!AppService.isCreated) {
            startService(new Intent(this, AppService.class));
        }
    }

}
