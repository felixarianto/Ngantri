package id.co.fxcorp.ngantri;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import id.co.fxcorp.db.AntriDB;
import id.co.fxcorp.db.AntriModel;
import id.co.fxcorp.db.PlaceDB;
import id.co.fxcorp.db.PlaceModel;
import id.co.fxcorp.db.UserDB;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle("Pencarian Tempat");


        RecyclerView rcv_place = findViewById(R.id.rcv_place);
        init(rcv_place);

        SearchView src_place = findViewById(R.id.src_place);
        src_place.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            private String keyWord = "";

            @Override
            public boolean onQueryTextSubmit(String s) {
                SETS_PLACE.clear();
                mNearbyAdapter.DATA.clear();
                mNearbyAdapter.notifyDataSetChanged();
                findPlace(keyWord = s.toLowerCase());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }

            private void findPlace(final String keyword) {
                PlaceDB.getMyPlace().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        checkPlace(dataSnapshot);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                if (PlaceDB.mLastLatLng != null) {
                    PlaceDB.getNearby(PlaceDB.mLastLatLng).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            checkPlace(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            private HashSet<String> SETS_PLACE = new HashSet<>();
            private void checkPlace(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> data = dataSnapshot.getChildren().iterator();
                while (data.hasNext()) {
                    PlaceModel place = new PlaceModel(data.next());
                    if (place.getName() != null && place.getName().toLowerCase().contains(keyWord)) {
                        if (SETS_PLACE.add(place.getPlaceId())) {
                            mNearbyAdapter.DATA.add(place);
                            mNearbyAdapter.notifyItemInserted(mNearbyAdapter.DATA.size() - 1);
                        }
                    }
                }
            }

        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return false;
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
}
