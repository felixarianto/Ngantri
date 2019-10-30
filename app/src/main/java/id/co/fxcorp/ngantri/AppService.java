package id.co.fxcorp.ngantri;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import id.co.fxcorp.db.AntriDB;
import id.co.fxcorp.db.AntriModel;
import id.co.fxcorp.db.ChatDB;
import id.co.fxcorp.db.ChatModel;
import id.co.fxcorp.db.ChildEvent;
import id.co.fxcorp.db.PlaceDB;
import id.co.fxcorp.db.PlaceModel;
import id.co.fxcorp.db.Prefs;
import id.co.fxcorp.db.UserDB;
import id.co.fxcorp.db.UserModel;
import id.co.fxcorp.util.DateUtil;

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
        Log.i(TAG, "onCreate");
        FirebaseApp.initializeApp(this);
        loadSignInSession();
        isCreated = true;
        runAlarm(60000);
    }


    private void runAlarm(int interval) {
        Intent intent = new Intent(this, AppRestart.class);
        PendingIntent mKeepAlivePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ((AlarmManager) this.getSystemService(Context.ALARM_SERVICE)).setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, mKeepAlivePendingIntent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i(TAG, "onTaskRemoved");
        runAlarm(60000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        runAlarm(60000);
    }

    private void loadSignInSession() {
        final String[] email_password = Prefs.getAccount(AppService.this);
        if (email_password == null) {
            return;
        }
        signIn(AppService.this, email_password[0], email_password[1], null);
    }

    /*
     *
     */
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
    /*
     *
     */
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
                    if (user != null && (user.password.equals(password) || password == null)) {
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
                        observeChat(context, antri.place_id, antri.place_name, antri.place_photo, DateUtil.formatDate(System.currentTimeMillis()));
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
                                if (antri.status == null && ((System.currentTimeMillis() - antri.time) < 3000)) {
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
                    observeChat(context, place.getPlaceId(), place.getName(), place.getPhoto(), DateUtil.formatDate(System.currentTimeMillis()));
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


    private static HashSet<String> OBSERVED = new HashSet<>();
    private static Query mChatQuery;
    private static ChildEvent mChatEventListener;
    private static void observeChat(final Context context, String mGroup, final String place_name, final String place_photo, final String date) {
        if (OBSERVED.contains(mGroup)) {
            return;
        }
        OBSERVED.add(mGroup);
        final long treshold = System.currentTimeMillis() - 3000;
        mChatQuery = ChatDB.getLastChat(mGroup, 1);
        mChatQuery.addChildEventListener(mChatEventListener = new ChildEvent() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    ChatModel chat = dataSnapshot.getValue(ChatModel.class);
                    if (chat != null) {
                        if (!chat.userid.equals(UserDB.MySELF.id)) {
                            if (chat.created_time > treshold) {
                                Notif.notifyChat(context, chat, place_name, place_photo, date);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        });
    }
    private static void unobserveChat() {
        if (mChatQuery != null) {
            mChatQuery.removeEventListener(mChatEventListener);
            mChatEventListener = null;
        }
    }

}
