package id.co.fxcorp.ngantri;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ConcurrentHashMap;

import id.co.fxcorp.barcode.QrCodeScannerActivity;
import id.co.fxcorp.db.ChildEvent;
import id.co.fxcorp.db.PlaceDB;
import id.co.fxcorp.db.PlaceModel;
import id.co.fxcorp.db.UserDB;
import id.co.fxcorp.signin.SignInActivity;
import id.co.fxcorp.signin.SignUpActivity;
import id.co.fxcorp.util.Dpi;
import id.co.fxcorp.util.PlacePickerActivity;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, AppService.Callback {

    private final String TAG = "MainActivity";

    private DrawerLayout dwr_view;
    NavigationView nav_view;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_main);


        if (Build.BRAND.equalsIgnoreCase("xiaomi")) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            startActivity(intent);
        }

        AppService.registerPostCallback(this);

        Dpi.init(this);
        initDrawer();
        initSearch();
        initBottomSheet();
        initList();
        initMap();
        initUserInfo();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {
                case REQEUST_SCAN_QRCODE:
                    if (resultCode == RESULT_OK) {
                        PlaceDB.getPlace(data.getStringExtra("result")).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                PlaceModel place = new PlaceModel(dataSnapshot);
                                if (place.getOwner().equals(UserDB.MySELF.id)) {
                                    DialogChoosePlace.open(MainActivity.this, place);
                                }
                                else {
                                    DialogChoosePlace.take(MainActivity.this, place, null);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppService.unregisterPostCallback(this);
        clearMyPlace();
        NearbyPlacesList.get().release();
        MyAntriList.get().release();
    }

    @Override
    public void onIncomingPost(AppService.PostId p_post_id, String p_data) {
        switch (p_post_id) {
            case SIGN_UP:
            case SIGN_IN:
            {
                initUserInfo();
            }
            break;
            case SIGN_OUT:
            {
                initUserInfo();
                clearMyPlace();
                MyAntriList.get().release();
            }
            break;
        }
    }

    private void initUserInfo() {
        if (UserDB.MySELF != null) {
            txt_name.setText(UserDB.MySELF.name);
            txt_email.setText(UserDB.MySELF.email);
            Glide.with(img_photo).load(UserDB.MySELF.photo)
                    .apply(new RequestOptions().placeholder(R.drawable.ic_person_default).diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(img_photo);
            loadMyPlace();
            MyAntriList.get().listen(findViewById(R.id.lyt_antri));
        }
        else {
            txt_name.setText("Masuk | Daftar");
            txt_email.setText("");
            img_photo.setImageResource(R.drawable.ic_person_default);
        }
    }

    Query      mPlaceQuery;
    ChildEvent mPlaceListEvent;
    private void loadMyPlace() {
        if (UserDB.MySELF == null || mPlaceListEvent != null) {
            return;
        }
        mPlaceQuery = PlaceDB.getMyPlace();
        mPlaceQuery.addChildEventListener(mPlaceListEvent = new ChildEvent() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    PlaceModel place = new PlaceModel(dataSnapshot);
                    MY_PLACE_MAP.put(place.getString(PlaceModel.PLACE_ID), place);

                    Intent intent = new Intent();
                    intent.setAction("OPEN_PLACE");
                    intent.putExtra(PlaceModel.PLACE_ID, place.getString(PlaceModel.PLACE_ID));
                    intent.putExtra(PlaceModel.NAME,     place.getString(PlaceModel.NAME));

                    Drawable icon = getResources().getDrawable(place.isOnline() ? R.drawable.ic_dot : R.drawable.ic_circle_border);
                    nav_view.getMenu().findItem(R.id.nav_place).getSubMenu()
                            .add(0, place.getPlaceId().hashCode(), MY_PLACE_MAP.size(), place.getString(PlaceModel.NAME))
                            .setIcon(icon)
                            .setIntent(intent);


                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    PlaceModel place = new PlaceModel(dataSnapshot);
                    MY_PLACE_MAP.put(place.getString(PlaceModel.PLACE_ID), place);

                    Intent intent = new Intent();
                    intent.setAction("OPEN_PLACE");
                    intent.putExtra(PlaceModel.PLACE_ID, place.getString(PlaceModel.PLACE_ID));
                    intent.putExtra(PlaceModel.NAME,     place.getString(PlaceModel.NAME));

                    Drawable icon = getResources().getDrawable(place.isOnline() ? R.drawable.ic_dot : R.drawable.ic_circle_border);

                    nav_view.getMenu().findItem(R.id.nav_place).getSubMenu().findItem(place.getPlaceId().hashCode())
                            .setTitle(place.getName())
                            .setIcon(icon)
                            .setIntent(intent);

                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }



        });
    }

    private void clearMyPlace() {
        if (mPlaceQuery != null) {
            mPlaceQuery.removeEventListener(mPlaceListEvent);
            mPlaceListEvent = null;
        }
        nav_view.getMenu().findItem(R.id.nav_place).getSubMenu().clear();
    }


    private ImageView img_photo;
    private TextView  txt_name;
    private TextView  txt_email;
    ConcurrentHashMap<String, PlaceModel> MY_PLACE_MAP = new ConcurrentHashMap<>();
    private void initDrawer() {
        dwr_view = findViewById(R.id.dwr_view);
        nav_view = findViewById(R.id.nav_view);

        img_photo = nav_view.getHeaderView(0).findViewById(R.id.img_photo);
        txt_name  = nav_view.getHeaderView(0).findViewById(R.id.txt_name);
        txt_email = nav_view.getHeaderView(0).findViewById(R.id.txt_email);

        ((View) txt_name.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UserDB.MySELF == null) {
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                }
                else {
                    startActivity(new Intent(MainActivity.this, SignUpActivity.class)
                                      .putExtra("id", UserDB.MySELF.id));
                }
            }
        });

        nav_view.setNavigationItemSelectedListener(this);
    }

    private final int REQEUST_SCAN_QRCODE = 21;
    private void initSearch() {
        TextView txt_search = findViewById(R.id.txt_search);
        txt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });
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

        ImageButton btn_qrcode = findViewById(R.id.btn_qrcode);
        btn_qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, 432);
                        return;
                    }
                }
                startActivityForResult(new Intent(MainActivity.this, QrCodeScannerActivity.class), REQEUST_SCAN_QRCODE);
            }
        });

    }

    private CardView crd_gps;
    private void initBottomSheet() {
        LinearLayout bottom_sheet = findViewById(R.id.bottom_sheet);
        crd_gps = bottom_sheet.findViewById(R.id.crd_gps);
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
        MyAntriList.get().init((RecyclerView)findViewById(R.id.rcv_antrian));
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
                        if (UserDB.checkLoginState(MainActivity.this)) {
                            startActivityForResult(new Intent(MainActivity.this, PlaceActivity.class), 1);
                        }
                    } else {
                        final Intent intent = item.getIntent();
                        if (intent != null && "OPEN_PLACE".equals(intent.getAction())) {
                            PlaceModel place = MY_PLACE_MAP.get(intent.getStringExtra(PlaceModel.PLACE_ID));
                            DialogChoosePlace.open(MainActivity.this, place);
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

    private LocationListener mLocationListener;
    private void prepareMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_LOCATION);
            return;
        }
        crd_gps.setVisibility(View.VISIBLE);

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mLocationListener == null) {
            mLocationListener = new LocationListener() {

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

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 60 * 5, 1000, mLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 60 * 5, 1000, mLocationListener);
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

        crd_gps.setVisibility(View.GONE);
        NearbyPlacesList.get().listen(latlng);

    }



    private Circle lastUserCircle;
    private ValueAnimator lastPulseAnimator;





}
