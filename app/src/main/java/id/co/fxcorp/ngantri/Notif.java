package id.co.fxcorp.ngantri;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import id.co.fxcorp.db.AntriModel;

public class Notif {

    private static final String TAG = "Notif";
    private static final String CHANNEL = "CHANNEL_3";
    public static final int ID_CALL = 1;
    public static final int ID_INCOMING = 2;

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
            builder.setSmallIcon(R.drawable.ic_ngantri_notif);
            builder.setAutoCancel(true);

            Bitmap bitmap = null;
            if (!TextUtils.isEmpty(antri.place_photo)) {
                bitmap = Glide.with(context).asBitmap()
                        .apply(new RequestOptions().transform(new RoundedCorners(8)))
                        .load(antri.place_photo).submit(100, 100).get();
            }
            if (bitmap != null) {
                builder.setLargeIcon(bitmap);
            }

            Intent notifyIntent = new Intent(context, MainActivity.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            );

            builder.setContentIntent(pendingIntent);

            Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
            inboxStyle.setBuilder(builder);
            inboxStyle.setBigContentTitle("Panggilan ke- " + antri.call_count);
            inboxStyle.addLine(antri.call_msg);
            inboxStyle.setSummaryText("No. " + antri.number + " - " + antri.place_name);

            Log.w(TAG, antri.call_msg + " " + antri.place_name);

            manager.notify(ID_CALL, builder.build());
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    public static void notifyClosed(Context context, AntriModel antri) {
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
            builder.setSmallIcon(R.drawable.ic_ngantri_notif);
            builder.setAutoCancel(true);

            Bitmap bitmap = null;
            if (!TextUtils.isEmpty(antri.place_photo)) {
                bitmap = Glide.with(context).asBitmap()
                        .apply(new RequestOptions().transform(new RoundedCorners(8)))
                        .load(antri.place_photo).submit(100, 100).get();
            }
            if (bitmap != null) {
                builder.setLargeIcon(bitmap);
            }

            Intent notifyIntent = new Intent(context, MainActivity.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            );

            builder.setContentIntent(pendingIntent);

            Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
            inboxStyle.setBuilder(builder);
            inboxStyle.setBigContentTitle("Kami Tutup");
            inboxStyle.addLine("Mohon maaf antrian Anda dibatalkan");
            inboxStyle.setSummaryText("No. " + antri.number + " - " + antri.place_name);

            manager.notify(ID_CALL, builder.build());
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    public static void notifyComplete(Context context, AntriModel antri) {
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
            builder.setSmallIcon(R.drawable.ic_ngantri_notif);
            builder.setAutoCancel(true);

            Bitmap bitmap = null;
            if (!TextUtils.isEmpty(antri.place_photo)) {
                bitmap = Glide.with(context).asBitmap()
                        .apply(new RequestOptions().transform(new RoundedCorners(8)))
                        .load(antri.place_photo).submit(100, 100).get();
            }
            if (bitmap != null) {
                builder.setLargeIcon(bitmap);
            }

            Intent notifyIntent = new Intent(context, MainActivity.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            );

            builder.setContentIntent(pendingIntent);

            Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
            inboxStyle.setBuilder(builder);
            inboxStyle.setBigContentTitle("Antrian Selesai");
            inboxStyle.addLine("Berikan tanggapan Anda");
            inboxStyle.setSummaryText("No. " + antri.number + " - " + antri.place_name);

            Log.w(TAG, antri.call_msg + " " + antri.place_name);

            manager.notify(ID_CALL, builder.build());
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    public static void notifyIncoming(Context context, AntriModel antri) {
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
            builder.setSmallIcon(R.drawable.ic_ngantri_notif);
            builder.setAutoCancel(true);

            Bitmap bitmap = null;
            if (!TextUtils.isEmpty(antri.cust_photo)) {
                bitmap = Glide.with(context).asBitmap()
                        .apply(new RequestOptions().circleCrop())
                        .load(antri.cust_photo).submit(100, 100).get();
            }
            if (bitmap != null) {
                builder.setLargeIcon(bitmap);
            }

            Intent notifyIntent = new Intent(context, MainActivity.class);
            notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
            );

            builder.setContentIntent(pendingIntent);

            Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
            inboxStyle.setBuilder(builder);
            inboxStyle.setBigContentTitle("Antrian Baru");
            inboxStyle.addLine(antri.cust_name);
            inboxStyle.setSummaryText("No. " + antri.number + " - " + antri.place_name);

            manager.notify(ID_INCOMING, builder.build());
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    public static void cancel(Context context, int id) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
        manager.cancel(id);
    }

}
