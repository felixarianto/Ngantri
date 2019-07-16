package id.co.fxcorp.message;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import id.co.fxcorp.db.ChatDB;
import id.co.fxcorp.db.ChatModel;
import id.co.fxcorp.db.ChildEvent;
import id.co.fxcorp.db.UserDB;
import id.co.fxcorp.ngantri.MainActivity;
import id.co.fxcorp.ngantri.R;
import id.co.fxcorp.ngantri.SimpleRecyclerAdapter;
import id.co.fxcorp.ngantri.SimpleRecyclerVH;
import id.co.fxcorp.storage.Storage;
import id.co.fxcorp.util.DateUtil;
import id.co.fxcorp.util.Dpi;

public class MessagingActivity extends AppCompatActivity {

    private final static String TAG = "MessagingActivity";
    String mGroup;
    long mNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging_activity);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayUseLogoEnabled(true);
            Glide.with(this).load(getIntent().getStringExtra("thumb")).apply(new RequestOptions().circleCrop())
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            getSupportActionBar().setIcon(resource);
                        }
                    });
        }



        setTitle(getIntent().getStringExtra("title"));
        mGroup  = getIntent().getStringExtra("group");
        mNumber = getIntent().getLongExtra("number", 0l);

        initComposer();
        initList();
        observeChat();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unobserveChat();
    }

    private void initComposer() {
        final EditText edt_text = findViewById(R.id.edt_text);

        final FloatingActionButton btn_send = findViewById(R.id.btn_send);
        btn_send.setEnabled(false);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edt_text.getText().toString().isEmpty()) {
                    return;
                }
                ChatModel model = new ChatModel();
                model.userid = UserDB.MySELF.id;
                model.name   = UserDB.MySELF.name;
                model.created_time = System.currentTimeMillis();
                model.group = mGroup;
                model.id    = model.userid + "-" + Long.toHexString(model.created_time);
                model.text  = edt_text.getText().toString();
                model.number = mNumber;
                model.status = ChatModel.STATUS_SEND;

                edt_text.setText("");
                mAdapter.DATA.add(0, new ChatHolder.ItemHolder(MessagingActivity.this, model));
                mAdapter.notifyItemInserted(0);

                rcvw.smoothScrollToPosition(0);

                ChatModel chat = model.clone();
                chat.status = ChatModel.STATUS_DELIVERED;
                ChatDB.insert(chat).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MessagingActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "", e);
                    }
                });
            }
        });

        edt_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                btn_send.setEnabled(editable.toString().trim().length() != 0);
            }
        });

        final ImageButton btn_attach = findViewById(R.id.btn_attach);
        btn_attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage
                .activity()
                .start(MessagingActivity.this);
            }
        });
    }


    RecyclerView rcvw;
    SwipeRefreshLayout swp_lyt;
    LinearLayoutManager rcvw_layout_manager;
    ChatAdapter mAdapter;
    private void initList() {
        rcvw = findViewById(R.id.rcv_list);
        rcvw.setItemViewCacheSize(20);
        rcvw.setHasFixedSize(true);
        rcvw_layout_manager = (LinearLayoutManager) rcvw.getLayoutManager();
        rcvw_layout_manager.setInitialPrefetchItemCount(7);
        mAdapter = new ChatAdapter();
        mAdapter.setHasStableIds(true);
        rcvw.setAdapter(mAdapter);
        swp_lyt = findViewById(R.id.swp_lyt);
        swp_lyt.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                try {
                    long sc_time = mAdapter.DATA.isEmpty() ? 0 : mAdapter.DATA.get(mAdapter.DATA.size() - 1).created_time;
                    ChatDB.getMoreChat(mGroup, sc_time, 30).addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            try {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                                        final ArrayList<ChatHolder.ItemHolder> adding = new ArrayList<>();
                                        while (iterator.hasNext()) {
                                            ChatModel chat = iterator.next().getValue(ChatModel.class);
                                            if (chat != null) {
                                                boolean skip = false;
                                                for (int i = mAdapter.DATA.size() - 1; i >= 0; i--) {
                                                    if (mAdapter.DATA.get(i).id.equals(chat.id)) {
                                                        skip = true;
                                                        break;
                                                    }
                                                    else if (mAdapter.DATA.get(i).created_time > chat.created_time) {
                                                        break;
                                                    }
                                                }
                                                if (skip) {
                                                    continue;
                                                }
                                                adding.add(0, new ChatHolder.ItemHolder(MessagingActivity.this, chat));
                                            }
                                        }

                                        mAdapter.DATA.addAll(adding);
                                        final int ecx = mAdapter.DATA.size() - 1;
                                        final int scx = mAdapter.DATA.size() - adding.size();
                                        rcvw.getHandler().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    if (adding.isEmpty()) {
                                                        swp_lyt.setRefreshing(false);
                                                        return;
                                                    }
                                                    if (scx > 0) {
                                                        if (mAdapter.DATA.get(scx).userid.equals(mAdapter.DATA.get(scx - 1).userid)) {
                                                            mAdapter.notifyItemChanged(scx - 1);
                                                        }
                                                    }
                                                    mAdapter.notifyItemRangeInserted(scx, ecx);
                                                    swp_lyt.setRefreshing(false);
                                                } catch (Exception e) {
                                                    Log.e(TAG, "", e);
                                                }
                                            }
                                        });
                                    }
                                }.start();

                            } catch (Exception e) {
                                Log.e(TAG, "", e);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        });
    }

    Query mChatQuery;
    ChildEvent mChatEventListener;
    private void observeChat() {
        mChatQuery = ChatDB.getLastChat(mGroup, 30);
        mChatQuery.addChildEventListener(mChatEventListener = new ChildEvent() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    ChatModel chat = dataSnapshot.getValue(ChatModel.class);
                    if (chat != null) {
                        if (chat.userid.equals(UserDB.MySELF.id)) {
                            if (!mAdapter.DATA.isEmpty()) {
                                for (int i = 0; i < mAdapter.DATA.size(); i++) {
                                    if (mAdapter.DATA.get(i).id.equals(chat.id)) {
                                        if (!TextUtils.isEmpty(chat.image)) {
                                            chat.image = mAdapter.DATA.get(i).image;
                                        }
                                        mAdapter.DATA.set(i, new ChatHolder.ItemHolder(MessagingActivity.this, chat));
                                        mAdapter.notifyItemChanged(i);
                                        return;
                                    }
                                }
                                mAdapter.DATA.add(0, new ChatHolder.ItemHolder(MessagingActivity.this, chat));
                                mAdapter.notifyItemInserted(0);
                            }
                            else {
                                mAdapter.DATA.add(0, new ChatHolder.ItemHolder(MessagingActivity.this, chat));
                                mAdapter.notifyItemInserted(0);
                            }
                        }
                        else {
                            mAdapter.DATA.add(0, new ChatHolder.ItemHolder(MessagingActivity.this, chat));
                            mAdapter.notifyItemInserted(0);
                            if (rcvw_layout_manager.findFirstVisibleItemPosition() < 5) {
                                rcvw.smoothScrollToPosition(0);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        });
    }
    private void unobserveChat() {
        if (mChatQuery != null) {
            mChatQuery.removeEventListener(mChatEventListener);
            mChatEventListener = null;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            switch (requestCode) {
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    if (resultCode == RESULT_OK) {
                        sendImage(data);
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                    }
                    break;

            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    private void sendImage(Intent data) {
        CropImage.ActivityResult result = CropImage.getActivityResult(data);
        final Uri resultUri = result.getUri();

        File file = new File(resultUri.getPath());
        if (!file.exists()) {
            Toast.makeText(MessagingActivity.this, "Not Exists ", Toast.LENGTH_SHORT).show();
            return;
        }



        ChatModel model = new ChatModel();
        model.userid = UserDB.MySELF.id;
        model.name   = UserDB.MySELF.name;
        model.created_time = System.currentTimeMillis();
        model.group = mGroup;
        model.id     = model.userid + "-" + Long.toHexString(model.created_time);
        model.image  = resultUri.getPath();
        model.number = mNumber;
        model.status = ChatModel.STATUS_SEND;

        double scale = result.getCropRect().height() / result.getCropRect().width();

        if (scale > 1.3) {
            model.image_scale = 1;
        }
        else if (scale < 0.3) {
            model.image_scale = 2;
        }

        mAdapter.DATA.add(0, new ChatHolder.ItemHolder(MessagingActivity.this, model));
        mAdapter.notifyItemInserted(0);

        final ChatModel chat = model.clone();
        chat.image = model.id + ".jpg";

        rcvw.smoothScrollToPosition(0);

        final StorageReference ref = Storage.images(chat.image);
        ref.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    chat.image  = task.getResult().toString();
                    chat.status = ChatModel.STATUS_DELIVERED;
                    ChatDB.insert(chat).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MessagingActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "", e);
                        }
                    });
                } else {
                    Toast.makeText(MessagingActivity.this, "Upload image bermasalah, periksa internet Anda", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
