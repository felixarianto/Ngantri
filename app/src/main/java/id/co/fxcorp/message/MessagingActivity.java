package id.co.fxcorp.message;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

import id.co.fxcorp.db.ChatDB;
import id.co.fxcorp.db.ChatModel;
import id.co.fxcorp.db.ChildEvent;
import id.co.fxcorp.db.UserDB;
import id.co.fxcorp.ngantri.R;
import id.co.fxcorp.ngantri.SimpleRecyclerAdapter;
import id.co.fxcorp.ngantri.SimpleRecyclerVH;
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
                mAdapter.DATA.add(0, model);
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
    }

    RecyclerView rcvw;
    LinearLayoutManager rcvw_layout_manager;
    SimpleRecyclerAdapter<ChatModel> mAdapter;
    private void initList() {
        rcvw = findViewById(R.id.rcv_list);
        rcvw_layout_manager = (LinearLayoutManager) rcvw.getLayoutManager();
        rcvw.setAdapter(mAdapter = new SimpleRecyclerAdapter<ChatModel>(R.layout.messaging_adapter_left, R.layout.messaging_adapter_right) {

            private int[] NAME_COLOURS = new int[]{R.color.name_1, R.color.name_2, R.color.name_3,
                    R.color.name_4, R.color.name_5, R.color.name_6,
                    R.color.name_7, R.color.name_8, R.color.name_9, R.color.name_10};

            @Override
            public int getItemViewType(int position) {
                if (DATA.get(position).userid.equals(UserDB.MySELF.id)) {
                    return 1;
                }
                else {
                    return 0;
                }
            }

            @Override
            public void onBind(SimpleRecyclerVH holder, ChatModel value, int position) {
                try {
                    TextView  txt_name = holder.itemView.findViewById(R.id.txt_name);
                    TextView  txt_text = holder.itemView.findViewById(R.id.txt_text);
                    TextView  txt_time = holder.itemView.findViewById(R.id.txt_time);
                    ImageView img_image = holder.itemView.findViewById(R.id.img_image);
                    View      vw_angle  = holder.itemView.findViewById(R.id.vw_angle);

                    txt_time  .setText(DateUtil.formatTime(value.created_time));

                    int prev = position + 1;
                    if (prev < DATA.size() && DATA.get(prev).userid.equals(value.userid)) {
                        vw_angle.setVisibility(View.INVISIBLE);
                        holder.itemView.setPadding(0, 6, 0, 0);
                        if (txt_name != null) {
                            txt_name  .setText("");
                            txt_name  .setVisibility(View.GONE);
                        }
                    }
                    else {
                        vw_angle.setVisibility(View.VISIBLE);
                        holder.itemView.setPadding(0, 30, 0, 0);
                        if (txt_name != null) {
                            if (value.number == 0) {
                                txt_name  .setText(value.name);
                                txt_name.setTextColor(getResources().getColor(R.color.colorAccentLight));
                            }
                            else {
                                txt_name  .setText(value.number + " | " + value.name);
                                txt_name.setTextColor(getResources().getColor(NAME_COLOURS[Long.valueOf(value.number).intValue() % NAME_COLOURS.length]));
                            }
                            txt_name  .setVisibility(View.VISIBLE);
                        }
                    }

                    if (TextUtils.isEmpty(value.text)) {
                        txt_text.setText("");
                        txt_text.setVisibility(View.GONE);
                    }
                    else {
                        SpannableStringBuilder span = new SpannableStringBuilder();
                        span.append(value.text);
                        String space = "";
                        if (txt_time.getText().length() > 5) {
                            space = "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
                        }
                        else {
                            space = "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
                        }
                        if (value.userid.equals(UserDB.MySELF.id)) {
                            space += "&#160;&#160;&#160;&#160;";
                        }
                        span.append(Html.fromHtml(space));
                        txt_text.setText(span);
                        txt_text.setVisibility(View.VISIBLE);
                    }

                    if (TextUtils.isEmpty(value.image)) {
                        img_image.setImageResource(0);
                        img_image.setVisibility(View.GONE);
                    }
                    else {
                        Glide.with(img_image).load(value.image).into(img_image);
                        img_image.setVisibility(View.VISIBLE);
                    }

                    if (value.userid.equals(UserDB.MySELF.id)) {
                        if (value.status == ChatModel.STATUS_SEND) {
                            txt_time.setCompoundDrawablesWithIntrinsicBounds(0 , 0, R.drawable.ic_sending, 0);
                        }
                        else {
                            txt_time.setCompoundDrawablesWithIntrinsicBounds(0 , 0, R.drawable.ic_delivered, 0);
                        }
                    }
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
                                        mAdapter.DATA.set(i, chat);
                                        mAdapter.notifyItemChanged(i);
                                        return;
                                    }
                                }
                                mAdapter.DATA.add(0, chat);
                                mAdapter.notifyItemInserted(0);
                            }
                            else {
                                mAdapter.DATA.add(0, chat);
                                mAdapter.notifyItemInserted(0);
                            }
                        }
                        else {
                            mAdapter.DATA.add(0, chat);
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




}
