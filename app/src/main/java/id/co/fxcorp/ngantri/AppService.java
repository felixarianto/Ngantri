package id.co.fxcorp.ngantri;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import id.co.fxcorp.db.AntriDB;
import id.co.fxcorp.db.AntriModel;
import id.co.fxcorp.db.Prefs;
import id.co.fxcorp.db.UserDB;
import id.co.fxcorp.db.UserModel;

public class AppService extends Service {

    private final static String TAG = "AppService";

    public static boolean isCreated = false;

    public AppService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        FirebaseApp.initializeApp(this);
        waitMyAntrian();
        isCreated = true;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        sendBroadcast(new Intent(this, AppRestart.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent(this, AppRestart.class));
    }

    private void loadUserInfo() {

    }

    public void waitMyAntrian() {
        final String[] email_password = Prefs.getAccount(AppService.this);
        if (email_password == null) {
            return;
        }
        UserDB.login(email_password[0]).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Log.e(TAG, "onDataChange");
                    if (dataSnapshot.exists()) {
                        UserModel user = dataSnapshot.getChildren().iterator().next().getValue(UserModel.class);
                        if (user != null && user.password != null && user.password.equals(email_password[1])) {
                            AntriDB.getMyAntrian(user.id).addValueEventListener(new ValueEventListener() {

                                private HashMap<String, AntriModel> ANTRI_MAP = new HashMap<>();
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    try {
                                        Log.e(TAG, "onDataChangeX " + dataSnapshot.toString());
                                        boolean notif_call = false;
                                        AntriModel antri = dataSnapshot.getChildren().iterator().next().getValue(AntriModel.class);
                                        if (antri != null) {
                                            AntriModel exist = ANTRI_MAP.get(antri.id);
                                            if (exist == null) {
                                                notif_call = true;
                                            }
                                            else {
                                                if (exist.call_count != antri.call_count) {
                                                    notif_call = true;
                                                }
                                            }
                                            ANTRI_MAP.put(antri.id, antri);

                                            if (notif_call) {
                                                Notif.notifyCall(AppService.this, antri);
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "", e);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }

                            });
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
