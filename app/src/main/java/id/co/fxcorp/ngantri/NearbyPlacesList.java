package id.co.fxcorp.ngantri;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import id.co.fxcorp.db.AntriDB;
import id.co.fxcorp.db.AntriModel;
import id.co.fxcorp.db.PlaceDB;
import id.co.fxcorp.db.PlaceModel;
import id.co.fxcorp.db.UserDB;

public class NearbyPlacesList {

    private final String TAG = "NearbyPlacesList";

    private static NearbyPlacesList mInstances;
    public static NearbyPlacesList get() {
        if (mInstances == null) {
            mInstances = new NearbyPlacesList();
        }
        return mInstances;
    }

    private RecyclerView rcv;
    private SimpleRecyclerAdapter<PlaceModel> mNearbyAdapter;
    public void init(RecyclerView rcvList) {
        rcv = rcvList;
        rcv.setAdapter(mNearbyAdapter = new SimpleRecyclerAdapter<PlaceModel>(R.layout.nearby_adapter){
            @Override
            public void onBind(SimpleRecyclerVH holder, final PlaceModel place, int position) {

                ImageView img_photo  = holder.itemView.findViewById(R.id.img_photo);
                TextView txt_number  = holder.itemView.findViewById(R.id.txt_number);
                TextView txt_name    = holder.itemView.findViewById(R.id.txt_name);
                TextView txt_description = holder.itemView.findViewById(R.id.txt_description);
                TextView txt_hour    = holder.itemView.findViewById(R.id.txt_hour);
                TextView txt_address = holder.itemView.findViewById(R.id.txt_address);

                txt_name       .setText(place.getString(PlaceModel.NAME));
                txt_description.setText(place.getString(PlaceModel.DESCRIPTION));
                txt_address    .setText(place.getString(PlaceModel.ADDRESS));

                Glide.with(img_photo).load(place.getPhoto()).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                     .into(img_photo);

                List<String> workhour = (List<String>) place.get(PlaceModel.WORK_HOUR);
                if (workhour != null) {

                    Calendar cal = Calendar.getInstance();
                    int day = cal.get(Calendar.DAY_OF_WEEK) - 1;
                    if (day == 0) {
                        day = 7;
                    }
                    day++;
                    if (day < workhour.size() && "1".equals(workhour.get(day))) {
                        txt_hour.setText("Buka " + workhour.get(0) + ":00 s/d " + workhour.get(1) + ":00");
                    }
                    else {
                        txt_hour.setText("Hari ini TUTUP");
                    }

                }

                if (place.isOnline()) {
                    txt_number.setText(place.getNumberQty() + "");
                    holder.itemView.findViewById(R.id.lyt_number).setVisibility(View.VISIBLE);
                }
                else {
                    holder.itemView.findViewById(R.id.lyt_number).setVisibility(View.GONE);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!UserDB.checkLoginState((Activity) view.getContext())) {
                            return;
                        }

                        if (UserDB.MySELF != null && place.getOwner().equals(UserDB.MySELF.id)) {
                            DialogChoosePlace.open(rcv.getContext(), place);
                        }
                        else {
                            AntriDB.getMyAntriList().addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    AntriModel exist = null;
                                    Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                                    while (iterator.hasNext()) {
                                        AntriModel model = iterator.next().getValue(AntriModel.class);
                                        if (!model.isComplete()) {
                                            if (model.place_id.equals(place.getPlaceId())) {
                                                exist = model;
                                                break;
                                            }
                                        }
                                    }
                                    DialogChoosePlace.take(rcv.getContext(), place, exist);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private Query              mLastQuery;
    private ChildEventListener mLastQueryListener;
    private String mLastGPlaceId;
    public void listen(LatLng latlng) {
        String g_placeid = PlaceDB.toGPlace(latlng);
        if (mLastGPlaceId != null && mLastGPlaceId.equals(g_placeid)) {
            return;
        }
        mLastGPlaceId = g_placeid;

        if (mLastQueryListener != null) {
            mLastQuery.removeEventListener(mLastQueryListener);
        }

        mNearbyAdapter.DATA.clear();
        mNearbyAdapter.notifyDataSetChanged();

        Log.d(TAG, "listen");

        mLastQuery = PlaceDB.getNearby(latlng);
        mLastQuery.addChildEventListener(mLastQueryListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable final String prevChildKey) {
                try {
                    PlaceModel place = new PlaceModel(dataSnapshot);
                    mNearbyAdapter.DATA.add(0, place);
                    mNearbyAdapter.notifyItemInserted(0);
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String prevChildKey) {
                try {
                    PlaceModel place = new PlaceModel(dataSnapshot);
                    for (int i = 0; i < mNearbyAdapter.DATA.size(); i++) {
                        if (mNearbyAdapter.DATA.get(i).getString(PlaceModel.PLACE_ID).equals(place.getString(PlaceModel.PLACE_ID))) {
                            mNearbyAdapter.DATA.set(i, place);
                            mNearbyAdapter.notifyItemChanged(i);
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
                    PlaceModel place = new PlaceModel(dataSnapshot);
                    for (int i = 0; i < mNearbyAdapter.DATA.size(); i++) {
                        if (mNearbyAdapter.DATA.get(i).getString(PlaceModel.PLACE_ID).equals(place.getString(PlaceModel.PLACE_ID))) {
                            mNearbyAdapter.DATA.remove(i);
                            mNearbyAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
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

    public void release() {
        mLastGPlaceId = "";
        if (mLastQueryListener != null) {
            mLastQuery.removeEventListener(mLastQueryListener);
            mLastQueryListener = null;
            mLastQuery = null;
        }
        mInstances = null;
    }

}
