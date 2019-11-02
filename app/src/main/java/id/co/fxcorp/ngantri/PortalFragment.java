package id.co.fxcorp.ngantri;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import id.co.fxcorp.db.AntriDB;
import id.co.fxcorp.db.AntriModel;
import id.co.fxcorp.db.ChatDB;
import id.co.fxcorp.db.ChatModel;
import id.co.fxcorp.db.PlaceDB;
import id.co.fxcorp.db.UserDB;

public class PortalFragment extends Fragment {

    private static String TAG = "PortalFragment";

    static PortalFragment init(String p_place_id, long p_number, String p_name, String p_photo) {
        PortalFragment frg = new PortalFragment();
        Bundle args = new Bundle();
        args.putString("place_id", p_place_id);
        args.putLong("number", p_number);
        args.putString("name", p_name);
        args.putString("photo", p_photo);
        frg.setArguments(args);
        return frg;
    }

    String mPlaceId = "";
    long   mNumber = 0;
    String mName = "";
    String mPhoto = "";
    String mGroup = "";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlaceId = getArguments() != null ? getArguments().getString("place_id") : "";
        mNumber  = getArguments() != null ? getArguments().getLong("number") : 0;
        mName    = getArguments() != null ? getArguments().getString("name") : "";
        mPhoto   = getArguments() != null ? getArguments().getString("photo") : "";
        mGroup   = mPlaceId;
    }

    private TextToSpeech mTextToSpeech;
    private ImageView img_photo;
    private TextView txt_name;
    private TextView txt_number;
    private TextView txt_end;
    private View lyt_button;
    private Button   btn_call;
    private Button btn_end;

    private Handler HANDLER;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.portal_fragment, container,false);
        img_photo   = layout.findViewById(R.id.img_photo);
        txt_name    = layout.findViewById(R.id.txt_name);
        txt_number  = layout.findViewById(R.id.txt_number);
        txt_end  = layout.findViewById(R.id.txt_end);
        btn_call = layout.findViewById(R.id.btn_call);
        btn_end  = layout.findViewById(R.id.btn_turn);
        lyt_button  = layout.findViewById(R.id.lyt_button);

        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call();
            }
        });

        btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                end();
            }
        });

        mTextToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

            }
        });

        txt_number.setText(mNumber + "");
        txt_name  .setText(mName);
        Glide.with(img_photo).load(mPhoto).apply(new RequestOptions().placeholder(R.drawable.ic_person_default)).into(img_photo);

        HANDLER = new Handler();
        HANDLER.postDelayed(mLoadRunnable, 1000);
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (HANDLER != null && mLoadRunnable != null) {
            HANDLER.removeCallbacks(mLoadRunnable);
        }
        if (mLoadQuery != null && mLoadEventListener != null) {
            mLoadQuery.removeEventListener(mLoadEventListener);
        }
    }


    private AntriModel mAntri;
    private Query mLoadQuery;
    private ValueEventListener mLoadEventListener;
    private Runnable mLoadRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mLoadQuery = AntriDB.getItem(System.currentTimeMillis(), mPlaceId, mNumber);
                mLoadQuery.addValueEventListener(mLoadEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            AntriModel antri = dataSnapshot.getValue(AntriModel.class);
                            if (antri != null) {
                                mAntri = antri;
                                if (antri.status != null && antri.status.equals("Selesai")) {
                                    txt_end.setVisibility(View.VISIBLE);
                                    lyt_button.setVisibility(View.GONE);
                                }
                                else {
                                    txt_end    .setVisibility(View.GONE);
                                    lyt_button.setVisibility(View.VISIBLE);
                                    if (antri.call_count == 0) {
                                        btn_call.setText("Panggil");
                                    }
                                    else {
                                        btn_call.setText(antri.call_count + "");
                                    }
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
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }
    };

    private void call() {
        if (mAntri != null) {
            final long callQty = mAntri.call_count +  1;
            AntriDB.call(mAntri.id, callQty, "")
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        String text = "Antrian nomor " + txt_number.getText().toString() + " " + txt_name.getText().toString();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mTextToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
                        } else {
                            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                        }
                        PlaceDB.setNumberCurrent(mPlaceId, mAntri.number);
                        sendChat(callQty, "Panggilan kepada antrian nomor " + txt_number.getText().toString() + " atas nama " + txt_name.getText().toString());
                    }
                    else {
                        Toast.makeText(getContext(), "Koneksi bermasalah", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    private void sendChat(long callQty, String msg) {
        ChatModel model = new ChatModel();
        model.userid = UserDB.MySELF.id;
        model.name   = UserDB.MySELF.name;
        model.created_time = System.currentTimeMillis();
        model.group = mGroup;
        model.id    = model.userid + "-" + Long.toHexString(model.created_time);
        model.text  = msg;
        model.call  = callQty;
        model.number = mNumber;
        model.status = ChatModel.STATUS_DELIVERED;

        ChatDB.insert(model).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "", e);
            }
        });
    }

    private void end() {
        if (mAntri != null) {
            AntriDB.setComplete(mAntri.place_id, mAntri.id, "Selesai")
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        btn_call.setVisibility(View.GONE);
                        btn_end .setVisibility(View.VISIBLE);
                        btn_end.setText("Selesai");
                        btn_end.setEnabled(false);

                    }
                    else {
                        Toast.makeText(getContext(), "Koneksi bermasalah", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
