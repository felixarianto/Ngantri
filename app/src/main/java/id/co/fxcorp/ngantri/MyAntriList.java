package id.co.fxcorp.ngantri;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.net.URLEncoder;
import java.util.Locale;

import id.co.fxcorp.db.AntriDB;
import id.co.fxcorp.db.AntriModel;
import id.co.fxcorp.db.PlaceDB;
import id.co.fxcorp.db.PlaceModel;
import id.co.fxcorp.db.UserDB;
import id.co.fxcorp.message.MessagingActivity;
import id.co.fxcorp.util.DateUtil;

public class MyAntriList {

    private final String TAG = "MyAntriList";

    private static MyAntriList mInstances;
    public static MyAntriList get() {
        if (mInstances == null) {
            mInstances = new MyAntriList();
        }
        return mInstances;
    }

    private RecyclerView rcv;
    private SimpleRecyclerAdapter<AntriModel> mAntriAdapter;
    public void init(RecyclerView rcvList) {
        rcv = rcvList;
        rcv.setAdapter(mAntriAdapter = new SimpleRecyclerAdapter<AntriModel>(R.layout.antri_adapter){
            @Override
            public void onBind(final SimpleRecyclerVH holder, final AntriModel antri, int position) {

                ImageView img_photo  = holder.itemView.findViewById(R.id.img_photo);
                TextView txt_number  = holder.itemView.findViewById(R.id.txt_number);
                TextView txt_name    = holder.itemView.findViewById(R.id.txt_name);
                TextView txt_time    = holder.itemView.findViewById(R.id.txt_time);
                TextView txt_address = holder.itemView.findViewById(R.id.txt_address);
                ImageButton btn_more  = holder.itemView.findViewById(R.id.btn_more);

                txt_number     .setText("No. " + antri.number);
                txt_name       .setText(antri.place_name);
                txt_time       .setText(DateUtil.formatTime(antri.created_time));
                txt_address    .setText(antri.call_count == 0 ? "Menunggu Antrian" : "Panggilan Ke - " + antri.call_count);
                Glide.with(img_photo).load(antri.place_photo)
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(img_photo);

                btn_more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showMenu(((Activity) view.getContext()).getWindow().getDecorView(), antri);
                    }
                });
            }

            private void showMenu(final View parent, final AntriModel antri) {
                // Create the Snackbar
                final Snackbar snackbar = Snackbar.make(parent, "", Snackbar.LENGTH_LONG);
                Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
                TextView textView = layout.findViewById(android.support.design.R.id.snackbar_text);
                textView.setVisibility(View.INVISIBLE);

                View snackView = LayoutInflater.from(parent.getContext()).inflate(R.layout.antri_option, null);
                TextView txt_title = snackView.findViewById(R.id.txt_title);
                View btn_chat = snackView.findViewById(R.id.btn_chat);
                View btn_loc = snackView.findViewById(R.id.btn_loc);
                View btn_cancel = snackView.findViewById(R.id.btn_cancel);

                txt_title.setText(antri.place_name);
                btn_loc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PlaceDB.getPlace(antri.place_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                try {
                                    snackbar.dismiss();
                                    PlaceModel place = new PlaceModel(dataSnapshot);
                                    LatLng latlng = place.getLatLng();

                                    String uri = "http://maps.google.com/maps?q=loc:" + latlng.latitude + "," + latlng.longitude + " (" + URLEncoder.encode(place.getName()) + ")";
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                    parent.getContext().startActivity(intent);
                                } catch (Exception e) {
                                    Log.e(TAG, "", e);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

                btn_chat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                        Intent intent = new Intent(view.getContext(), MessagingActivity.class);
                        intent.putExtra("title",  antri.place_name + " " + DateUtil.formatDate(System.currentTimeMillis()));
                        intent.putExtra("thumb",  antri.place_photo);
                        intent.putExtra("group",  antri.place_id + "-" + DateUtil.formatDateReverse(System.currentTimeMillis()));
                        intent.putExtra("number", antri.number);
                        view.getContext().startActivity(intent);
                    }
                });

                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });
                layout.setPadding(0,0,0,0);
                layout.addView(snackView, 0);

                snackbar.setDuration(Snackbar.LENGTH_SHORT).show();

            }
        });
        Log.d(TAG, "init");
    }

    private Query              mLastQuery;
    private ChildEventListener mLastQueryListener;
    public void listen(final View view) {
        if (UserDB.MySELF == null) {
            return;
        }

        if (mLastQueryListener != null) {
            mLastQuery.removeEventListener(mLastQueryListener);
        }

        mAntriAdapter.DATA.clear();
        mAntriAdapter.notifyDataSetChanged();

        Log.d(TAG, "listen");
        mLastQuery = AntriDB.getMyAntriList();
        mLastQuery.addChildEventListener(mLastQueryListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable final String prevChildKey) {
                try {
                    AntriModel antri = dataSnapshot.getValue(AntriModel.class);
                    if (!antri.isComplete()) {
                        mAntriAdapter.DATA.add(0, antri);
                        mAntriAdapter.notifyItemInserted(0);
                        updateVisibility(view);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String prevChildKey) {
                try {
                    AntriModel antri = dataSnapshot.getValue(AntriModel.class);
                    for (int i = 0; i < mAntriAdapter.DATA.size(); i++) {
                        if (mAntriAdapter.DATA.get(i).id.equals(antri.id)) {
                            if (!antri.isComplete()) {
                                mAntriAdapter.DATA.set(i, antri);
                                mAntriAdapter.notifyItemChanged(i);
                            }
                            else {
                                mAntriAdapter.DATA.remove(i);
                                mAntriAdapter.notifyItemRemoved(i);
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                try {
                    AntriModel antri = dataSnapshot.getValue(AntriModel.class);
                    for (int i = 0; i < mAntriAdapter.DATA.size(); i++) {
                        if (mAntriAdapter.DATA.get(i).id.equals(antri.id)) {
                            mAntriAdapter.DATA.remove(i);
                            mAntriAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                    updateVisibility(view);
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    Runnable mShowRunnable;
    private void updateVisibility(final View view) {
        if (mShowRunnable == null) {
            mShowRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mAntriAdapter.DATA.isEmpty()) {
                        view.setVisibility(View.GONE);
                    }
                    else {
                        view.setVisibility(View.VISIBLE);
                    }
                }
            };
        }
        view.getHandler().removeCallbacks(mShowRunnable);
        view.getHandler().postDelayed(mShowRunnable, 800);
    }

    public void release() {
        if (mLastQueryListener != null) {
            mLastQuery.removeEventListener(mLastQueryListener);
            mLastQuery = null;
            mLastQueryListener = null;
        }
        mAntriAdapter.DATA.clear();
        mAntriAdapter.notifyDataSetChanged();

        mInstances = null;
    }

}
