package id.co.fxcorp.ngantri;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;


import id.co.fxcorp.db.AntriModel;

public class Notif {

    private static final String TAG = "Notif";
    private static final String CHANNEL = "CHANNEL_3";
    public static final int ID_CALL = 1;

    public static void notifyCall(Context context, AntriModel antri) {
        try {
            NotificationManager manager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

            Uri sound_uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.sound_notif);
            Notification.Builder builder = new Notification.Builder(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = manager.getNotificationChannel(CHANNEL);
                if (channel == null) {
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    channel = new NotificationChannel(CHANNEL, "Panggilan Masuk", importance);
                    channel.setVibrationPattern(new long[]{200l, 500l});
                    channel.setSound(sound_uri, new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT).build());
                    manager.createNotificationChannel(channel);
                }
                builder.setChannelId(CHANNEL);
            }
            else {
                builder.setSound(sound_uri);
            }

            builder.setContentTitle(antri.place_name);
            builder.setContentText(antri.call_msg);
            builder.setNumber(Long.valueOf(antri.call_count).intValue());
            builder.setSmallIcon(R.drawable.ic_ngantri);

            Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
            inboxStyle.setBuilder(builder);
            inboxStyle.setBigContentTitle(antri.place_name);
            inboxStyle.addLine(antri.call_msg);

            Log.w(TAG, antri.call_msg + " " + antri.place_name);

            manager.notify(ID_CALL, builder.build());
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    public static void cancel(Context context, int id) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
        manager.cancel(id);
    }

}
