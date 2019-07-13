package id.co.fxcorp.db;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ChatDB {

    private static String CHAT = "CHAT";

    public static Task<Void> insert(ChatModel model) {
        return FirebaseDatabase.getInstance()
                .getReference(CHAT)
                .child(model.group).child(model.id).setValue(model);
    }

    public static Query getLastChat(String group, int limit) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(CHAT);
        return ref.child(group).orderByChild("created_time").limitToFirst(limit);
    }


}
