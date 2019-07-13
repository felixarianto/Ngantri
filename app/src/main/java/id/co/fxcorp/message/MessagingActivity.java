package id.co.fxcorp.message;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

public class MessagingActivity extends AppCompatActivity {

    private final static String TAG = "MessagingActivity";
    String mGroup;
    long mNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging_activity);

        setTitle(getIntent().getStringExtra("title"));
        mGroup  = getIntent().getStringExtra("group");
        mNumber = getIntent().getLongExtra("number", 0l);

        initComposer();
        initList();
        observeChat();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unobserveChat();
    }

    private void initComposer() {
        final EditText edt_text = findViewById(R.id.edt_text);
        FloatingActionButton btn_send = findViewById(R.id.btn_send);
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
                ChatDB.insert(chat);
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
                    TextView  txt_number = holder.itemView.findViewById(R.id.txt_number);
                    TextView  txt_time = holder.itemView.findViewById(R.id.txt_time);
                    ImageView img_image = holder.itemView.findViewById(R.id.img_image);
                    LinearLayout lyt_text = holder.itemView.findViewById(R.id.lyt_text);

                    txt_time  .setText(DateUtil.formatTime(value.created_time));
                    if (txt_name != null) {
                        txt_name  .setText(value.name);
                    }

                    if (txt_number != null) {
                        if (value.number == 0l) {
                            txt_number.setText("");
                            txt_number.setVisibility(View.GONE);
                        }
                        else {
                            txt_number.setText("No. " + value.number);
                            txt_number.setVisibility(View.VISIBLE);
                        }
                    }

                    if (TextUtils.isEmpty(value.text)) {
                        txt_text.setText("");
                        txt_text.setVisibility(View.GONE);
                    }
                    else {
                        txt_text.setText(value.text);
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

                        if (value.text != null && value.text.length() < 30) {
                            lyt_text.setOrientation(LinearLayout.HORIZONTAL);
                        }
                        else {
                            lyt_text.setOrientation(LinearLayout.VERTICAL);
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
