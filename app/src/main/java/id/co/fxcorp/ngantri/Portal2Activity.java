package id.co.fxcorp.ngantri;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import id.co.fxcorp.db.AntriDB;
import id.co.fxcorp.db.AntriModel;
import id.co.fxcorp.db.PlaceDB;
import id.co.fxcorp.db.PlaceModel;

public class Portal2Activity extends AppCompatActivity {

    private final String TAG = "PortalActivity";

    private String mPlaceId = "";
    private PlaceModel mPlace;

    private TextToSpeech mTextToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        mPlaceId = intent.getStringExtra(PlaceModel.PLACE_ID);

        setTitle(intent.getStringExtra(PlaceModel.NAME));

        setContentView(R.layout.portal2_activity);
        vw_pager   = findViewById(R.id.vw_pager);
        crd_bottom = findViewById(R.id.crd_bottom);
        txt_info1  = findViewById(R.id.txt_info1);
        txt_info2  = findViewById(R.id.txt_info2);
        txt_info3  = findViewById(R.id.txt_info3);

        txt_info1.setText("");
        txt_info2.setText("");

        initPager();
        initAntrian();

        mTextToSpeech = new TextToSpeech(Portal2Activity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.portal, menu);
        return true;
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
        else if (id == R.id.close) {
            close();
        }
        return false;
    }

    private void close() {
        DialogChoosePlace.close(Portal2Activity.this, mPlace, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }

        });

    }

    private ViewPager vw_pager;
    private TextView txt_info1;
    private TextView txt_info2;
    private TextView txt_info3;
    private CardView crd_bottom;

    private void initAntrian() {
        AntriDB.getAntriListAtPlace(mPlaceId).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        AntriModel antri = iterator.next().getValue(AntriModel.class);
                        if (antri != null) {
                            if (ANTRI_MAP.get(antri.id) == null) {
                                ANTRI_MAP.put(antri.id, antri);
                                ANTRI_LIST.add(antri);
                                mAdapter.notifyDataSetChanged();
                                Log.d(TAG, "LIST " + ANTRI_LIST.size());
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

        Query query = PlaceDB.getPlace(mPlaceId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    PlaceModel place = new PlaceModel(dataSnapshot);
                    if (place != null) {
                        mPlace = place;
                        txt_info1.setText("Nomor Terkini : "  + mPlace.getNumberCurrent());
                        if (mPlace.getNumberCurrent() == 0) {
                            txt_info2.setText("Antrian Tesisa : 0");
                        }
                        else {
                            txt_info2.setText("Antrian Tersisa : " + mPlace.getNumberQty());
                        }
                        txt_info3.setText("Nomor Terakhir : " + mPlace.getNumberLast());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private HashMap<String, AntriModel> ANTRI_MAP = new HashMap<>();
    ArrayList<AntriModel> ANTRI_LIST = new ArrayList<>();
    private FragmentStatePagerAdapter mAdapter;

    private void initPager() {
        mAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            HashMap<Integer, Fragment> FRAGMENT_MAPS = new HashMap<>();

            @Override
            public Fragment getItem(int position) {
                if (FRAGMENT_MAPS.get(position) != null) {
                    return FRAGMENT_MAPS.get(position);
                }
                Fragment fg = PortalFragment.init(mPlaceId,
                        ANTRI_LIST.get(position).number,
                        ANTRI_LIST.get(position).cust_name,
                        ANTRI_LIST.get(position).cust_photo);
                FRAGMENT_MAPS.put(position, fg);
                return fg;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                super.destroyItem(container, position, object);
                FRAGMENT_MAPS.remove(position);
            }

            @Override
            public int getCount() {
                return ANTRI_LIST.size();
            }

        };

        PortalTransform fragmentTransformer = new PortalTransform(vw_pager, mAdapter);
        fragmentTransformer.enableScaling(true);

        vw_pager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {

            }
        });

        vw_pager.setAdapter(mAdapter);
        vw_pager.setPageTransformer(false, fragmentTransformer);
        vw_pager.setOffscreenPageLimit(3);

    }


}
