package id.co.fxcorp.util;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

import id.co.fxcorp.ngantri.R;

public class PlacePickerActivity extends AppCompatActivity implements OnMapReadyCallback {


    private final String TAG = "PlacePickerActivity";

    private GoogleMap mMap;
    private ImageView img_pin;
    private TextView txt_address;
    private FloatingActionButton btn_current;
    private LinearLayout lyt_place;

    private LatLng mLatLng;
    private String mAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_picker_activity);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        img_pin =findViewById(R.id.img_pin);
        txt_address =findViewById(R.id.txt_address);
        btn_current =findViewById(R.id.btn_current);
        lyt_place =findViewById(R.id.lyt_place);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            Log.w(TAG, "GPS Location not found");
            return;
        }
        final LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 18f));

        btn_current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    Log.w(TAG, "GPS Location not found");
                    return;
                }
                final LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 18f));
            }
        });

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                try {
                    mAddress = null;
                    mLatLng = null;
                    lyt_place.setVisibility(View.INVISIBLE);
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) img_pin.getLayoutParams();
                    lp.bottomMargin = Dpi.dp(80);
                    img_pin.requestLayout();
                    TransitionManager.beginDelayedTransition((ViewGroup) img_pin.getParent());
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try {
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) img_pin.getLayoutParams();
                    lp.bottomMargin = Dpi.dp(40);
                    img_pin.requestLayout();
                    TransitionManager.beginDelayedTransition((ViewGroup) img_pin.getParent());


                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(PlacePickerActivity.this, Locale.getDefault());

                    mLatLng = mMap.getCameraPosition().target;

                    addresses = geocoder.getFromLocation(mLatLng.latitude, mLatLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                    mAddress = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    txt_address.setText(mAddress);

                    lyt_place.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        });

        lyt_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAddress != null) {
                    Intent intent = new Intent();
                    intent.putExtra("latitude",  mLatLng.latitude);
                    intent.putExtra("longitude", mLatLng.longitude);
                    intent.putExtra("address",   mAddress);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return false;
    }
}
