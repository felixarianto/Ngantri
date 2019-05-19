package id.co.fxcorp.ngantri;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;

import java.util.concurrent.ConcurrentHashMap;

import id.co.fxcorp.db.ChildEvent;
import id.co.fxcorp.db.PlaceDB;
import id.co.fxcorp.db.PlaceModel;
import id.co.fxcorp.util.Dpi;
import id.co.fxcorp.util.PlacePickerActivity;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private final String TAG = "MainActivity";

    private DrawerLayout dwr_view;
    NavigationView nav_view;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        Dpi.init(this);
        initDrawer();
        initSearch();
        initBottomSheet();
        initList();
        initMap();
    }

    ConcurrentHashMap<String, PlaceModel> MY_PLACE_MAP = new ConcurrentHashMap<>();
    private void initDrawer() {
        dwr_view = findViewById(R.id.dwr_view);
        nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);

        PlaceDB.getMyPlace().addChildEventListener(new ChildEvent() {


            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    PlaceModel model = new PlaceModel(dataSnapshot);
                    MY_PLACE_MAP.put(model.getString(PlaceModel.PLACE_ID), model);

                    Intent intent = new Intent(MainActivity.this, OpenActivity.class);
                    intent.setAction("OPEN_PLACE");
                    intent.putExtra(PlaceModel.PLACE_ID, model.getString(PlaceModel.PLACE_ID));
                    intent.putExtra(PlaceModel.NAME,     model.getString(PlaceModel.NAME));

                    nav_view.getMenu().findItem(R.id.nav_place).getSubMenu()
                            .add(0, View.generateViewId(), MY_PLACE_MAP.size(), model.getString(PlaceModel.NAME))
                            .setIcon(R.drawable.ic_location)
                            .setIntent(intent);
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    PlaceModel model = new PlaceModel(dataSnapshot);
                    MY_PLACE_MAP.put(model.getString(PlaceModel.PLACE_ID), model);
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

        });
    }

    private void initSearch() {
        ImageButton btn_menu = findViewById(R.id.btn_menu);
        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dwr_view.isDrawerOpen(GravityCompat.START)) {
                    dwr_view.closeDrawer(GravityCompat.START);
                } else {
                    dwr_view.openDrawer(GravityCompat.START);
                }
            }
        });

        ImageButton btn_profile = findViewById(R.id.btn_profile);
        btn_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //SIGN UP
                //SIGN IN
            }
        });
    }

    private void initBottomSheet() {
        LinearLayout bottom_sheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior bottom_sheet_behaviour = BottomSheetBehavior.from(bottom_sheet);
        bottom_sheet_behaviour.setPeekHeight(400);
        bottom_sheet_behaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottom_sheet_behaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void initList() {
        NearbyPlacesList.get().init((RecyclerView)findViewById(R.id.rcv_nearby));
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onBackPressed() {
        if (dwr_view.isDrawerOpen(GravityCompat.START)) {
            dwr_view.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        // Handle navigation view item clicks here.
        dwr_view.closeDrawer(GravityCompat.START);
        dwr_view.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    int id = item.getItemId();
                    if (id == R.id.nav_open) {
                        startActivityForResult(new Intent(MainActivity.this, OpenActivity.class), 1);
                    } else if (id == R.id.nav_ads) {
                        startActivityForResult(new Intent(MainActivity.this, PlacePickerActivity.class), 1);
                    } else {
                        final Intent intent = item.getIntent();
                        if (intent != null && "OPEN_PLACE".equals(intent.getAction())) {
                            PlaceModel place = MY_PLACE_MAP.get(intent.getStringExtra(PlaceModel.PLACE_ID));
                            DialogChoosePlace.open(MainActivity.this, place, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(MainActivity.this, PortalActivity.class).putExtras(intent.getExtras()));
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        }, 200);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            mMap = googleMap;
            prepareMyLocation();
        } catch (Exception | Error e) {
            Log.e(TAG, "", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_LOCATION) {
            prepareMyLocation();
        }
    }

    private static int PERMISSION_LOCATION = 1;

    private boolean locationListenerReady = false;
    private void prepareMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_LOCATION);
            return;
        }

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationListenerReady) {
            locationListenerReady = true;
            LocationListener locationListener = new LocationListener() {

                public void onLocationChanged(Location location) {
                    try {
                        prepareMyLocation();
                    } catch (Exception | Error e) {
                        Log.e(TAG, "", e);
                    }
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {}

                public void onProviderEnabled(String provider) {}

                public void onProviderDisabled(String provider) {}
            };

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 60 * 5, 1000, locationListener);
        }

        final Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            Log.w(TAG, "GPS Location not found");
            return;
        }

        final LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 18f));

        if (lastPulseAnimator != null) {
            lastPulseAnimator.cancel();
        }
        if (lastUserCircle != null)
            lastUserCircle.setCenter(latlng);

        ValueAnimator va = ValueAnimator.ofFloat(15, 20);
        va.setDuration(3000);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (lastUserCircle != null)
                    lastUserCircle.setRadius((Float) animation.getAnimatedValue());
                else {
                    lastUserCircle = mMap.addCircle(new CircleOptions()
                            .center(latlng)
                            .radius((Float) animation.getAnimatedValue())
                            .strokeColor(Color.WHITE)
                            .strokeWidth(5f)
                            .fillColor(getResources().getColor(R.color.overlay_light_blue_300)));
                }
            }
        });
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.setRepeatMode(ValueAnimator.REVERSE);
        va.start();
        lastPulseAnimator = va;

        NearbyPlacesList.get().show(latlng);

    }

    private Circle lastUserCircle;
    private ValueAnimator lastPulseAnimator;



}
