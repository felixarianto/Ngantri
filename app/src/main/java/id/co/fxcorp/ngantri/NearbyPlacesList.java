package id.co.fxcorp.ngantri;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;

import java.util.List;

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

                Glide.with(img_photo).load(place.getPhoto()).into(img_photo);

                List<String> workhour = (List<String>) place.get(PlaceModel.WORK_HOUR);
                if (workhour != null) {
                    txt_hour.setText("Buka " + workhour.get(0) + ":00 s/d " + workhour.get(1) + ":00");
                }

                if (place.isOnline()) {
                    txt_number.setText(place.getNumberQty() + "");
                    holder.itemView.findViewById(R.id.lyt_number).setVisibility(View.VISIBLE);
                    holder.itemView.findViewById(R.id.txt_closed).setVisibility(View.GONE);
                }
                else {
                    holder.itemView.findViewById(R.id.lyt_number).setVisibility(View.GONE);
                    holder.itemView.findViewById(R.id.txt_closed).setVisibility(View.VISIBLE);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (UserDB.MySELF != null && place.getOwner().equals(UserDB.MySELF.id)) {
                            DialogChoosePlace.open(rcv.getContext(), place);
                        }
                        else {
                            DialogChoosePlace.take(rcv.getContext(), place);
                        }
                    }
                });
            }
        });
    }

    private Query              mLastQuery;
    private ChildEventListener mLastQueryListener;
    private String mLastGPlaceId;
    public void show(LatLng latlng) {
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

}
