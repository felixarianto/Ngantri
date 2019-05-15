package id.co.fxcorp.ngantri;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Map;

import id.co.fxcorp.db.DB;
import id.co.fxcorp.db.PlaceDB;
import id.co.fxcorp.db.PlaceModel;

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
            public void onBind(SimpleRecyclerVH holder, PlaceModel place, int position) {

                TextView txt_number  = holder.itemView.findViewById(R.id.txt_number);
                TextView txt_name    = holder.itemView.findViewById(R.id.txt_name);
                TextView txt_description = holder.itemView.findViewById(R.id.txt_description);
                TextView txt_hour    = holder.itemView.findViewById(R.id.txt_hour);
                TextView txt_address = holder.itemView.findViewById(R.id.txt_address);

                txt_name       .setText(place.getString(PlaceModel.NAME));
                txt_description.setText(place.getString(PlaceModel.DESCRIPTION));
                txt_address    .setText(place.getString(PlaceModel.ADDRESS));

                List<String> workhour = (List<String>) place.get(PlaceModel.WORK_HOUR);
                if (workhour != null) {
                    txt_hour.setText("Buka " + workhour.get(0) + ":00 s/d " + workhour.get(1) + ":00");
                }

                if (place.getInt(PlaceModel.ONLINE) == 1) {
                    txt_number.setText(place.getInt(PlaceModel.NUMBER));
                    holder.itemView.findViewById(R.id.lyt_number).setVisibility(View.VISIBLE);
                }
                else {
                    holder.itemView.findViewById(R.id.lyt_number).setVisibility(View.GONE);
                }
            }
        });
    }

    public void show(LatLng latlng) {
        PlaceDB.getNearby(latlng).addChildEventListener(new ChildEventListener() {
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
