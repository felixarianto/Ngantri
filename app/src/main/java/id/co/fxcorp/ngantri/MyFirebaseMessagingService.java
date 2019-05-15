package id.co.fxcorp.ngantri;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
    }
}
