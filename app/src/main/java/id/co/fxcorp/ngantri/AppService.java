package id.co.fxcorp.ngantri;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import id.co.fxcorp.db.AntriDB;
import id.co.fxcorp.db.AntriModel;
import id.co.fxcorp.db.ChildEvent;
import id.co.fxcorp.db.PlaceDB;
import id.co.fxcorp.db.PlaceModel;
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
        loadSignInSession();
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

    private void loadSignInSession() {
        final String[] email_password = Prefs.getAccount(AppService.this);
        if (email_password == null) {
            return;
        }
        signIn(AppService.this, email_password[0], email_password[1], null);
    }

    public static void signUp(Context context, UserModel user) {
        Prefs.setAccount(context, user.email, user.password);
        UserDB.MySELF  = user;
        post(PostId.SIGN_UP, user.id);
    }
    public interface SignInListener {
        void OnResult(boolean status, UserModel user);
    }
    public static void signIn(final Context context, String email, final String password, final SignInListener listener) {

        UserDB.login(email).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserModel user = dataSnapshot.getChildren().iterator().next().getValue(UserModel.class);
                    if (user != null && user.password.equals(password)) {
                        Prefs.setAccount(context, user.email, user.password);
                        UserDB.MySELF  = user;
                        watchMyAntriList(context, user);
                        post(PostId.SIGN_IN, user.id);
                        if (listener != null) {
                            listener.OnResult(true, user);
                        }
                        return;
                    }
                    if (listener != null) {
                        listener.OnResult(false, null);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (listener != null) {
                    listener.OnResult(false, null);
                }
            }

        });
    }

    private static ChildEvent mWatchMyAntriListener;
    private static void watchMyAntriList(final Context context, UserModel user) {
        AntriDB.getMyAntriList().addChildEventListener(mWatchMyAntriListener = new ChildEvent() {

            private HashMap<String, AntriModel> ANTRI_MAP = new HashMap<>();

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    final AntriModel antri = dataSnapshot.getValue(AntriModel.class);
                    if (!antri.isComplete()) {
                        boolean notif_call = false;
                        AntriModel exist = ANTRI_MAP.get(antri.id);
                        if (exist == null) {
                            if (antri.call_count > 0) {
                                notif_call = true;
                            }
                        }
                        else {
                            if (exist.call_count != antri.call_count) {
                                notif_call = true;
                            }
                        }
                        ANTRI_MAP.put(antri.id, antri);

                        if (notif_call) {
                            new Thread() {
                                @Override
                                public void run() {
                                    Notif.notifyCall(context, antri);
                                }
                            }.start();

                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    final AntriModel antri = dataSnapshot.getValue(AntriModel.class);
                    if (!antri.isComplete()) {
                        boolean notif_call = false;
                        AntriModel exist = ANTRI_MAP.get(antri.id);
                        if (exist == null) {
                            if (antri.call_count > 0) {
                                notif_call = true;
                            }
                        }
                        else {
                            if (exist.call_count != antri.call_count) {
                                notif_call = true;
                            }
                        }
                        ANTRI_MAP.put(antri.id, antri);

                        if (notif_call) {
                            new Thread() {
                                @Override
                                public void run() {
                                    Notif.notifyCall(context, antri);
                                }
                            }.start();

                        }
                    }
                    else {
                        if (ANTRI_MAP.remove(antri.id) != null) {
                            new Thread() {
                                @Override
                                public void run() {
                                    Notif.notifyComplete(context, antri);
                                }
                            }.start();
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                try {
                    final AntriModel antri = dataSnapshot.getValue(AntriModel.class);
                    if (ANTRI_MAP.remove(antri.id) != null) {
                        new Thread() {
                            @Override
                            public void run() {
                                Notif.notifyClosed(context, antri);
                            }
                        }.start();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

        });

        PlaceDB.getMyPlace().addChildEventListener(new ChildEvent() {

            private HashMap<String, ChildEvent> PLACE_ANTRI_EVENTS = new HashMap<>();

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    PlaceModel place = new PlaceModel(dataSnapshot);
                    ChildEvent event = new ChildEvent() {

                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            try {
                                final AntriModel antri = dataSnapshot.getValue(AntriModel.class);
                                if (antri.status == null && ((System.currentTimeMillis() - antri.created_time) < 3000)) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            Notif.notifyIncoming(context, antri);
                                        }
                                    }.start();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "", e);
                            }
                        }
                    };
                    AntriDB.getAntriListAtPlace(place.getPlaceId()).addChildEventListener(event);
                    PLACE_ANTRI_EVENTS.put(place.getPlaceId(), event);
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                try {
                    PlaceModel place = new PlaceModel(dataSnapshot);
                    PLACE_ANTRI_EVENTS.remove(place.getPlaceId());
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        });
    }

    public static void signOut(Context context) {
        Prefs.setAccount(context, "", "");
        UserDB.MySELF  = null;
        post(PostId.SIGN_OUT, "");
    }

    public static boolean isConnected(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (null != activeNetwork && activeNetwork.isConnected()) {
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return false;
    }

    public interface Callback {
        void onIncomingPost(PostId p_post_id, String p_data);
    }

    private static ArrayList<Callback> CALLBACKS = new ArrayList<>();
    public static void registerPostCallback(Callback callback) {
        if (!CALLBACKS.contains(callback)) {
            CALLBACKS.add(callback);
        }
    }
    public static void unregisterPostCallback(Callback callback) {
        CALLBACKS.remove(callback);
    }
    public static void post(PostId p_post_id, String p_data) {
        for (Callback callback : CALLBACKS) {
            try {
                callback.onIncomingPost(p_post_id, p_data);
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }
    }


    public enum PostId {SIGN_UP, SIGN_IN, SIGN_OUT}

}
