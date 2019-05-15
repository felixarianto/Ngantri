package id.co.fxcorp.ngantri;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import id.co.fxcorp.db.DB;
import id.co.fxcorp.db.PlaceDB;
import id.co.fxcorp.db.PlaceModel;
import id.co.fxcorp.util.MapStatic;
import id.co.fxcorp.util.PlacePickerActivity;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class OpenActivity extends AppCompatActivity {

    private final String TAG = "OpenActivity";
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_CAMERA = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private SubmitTask mSubmitTask = null;

    // UI references.
    private EditText edt_name;
    private TextView txt_address;
    private AutoCompleteTextView edt_type;
    private EditText edt_description;
    private EditText edt_open;
    private EditText edt_closed;
    private AppCompatCheckBox chk_sen;
    private AppCompatCheckBox chk_sel;
    private AppCompatCheckBox chk_rab;
    private AppCompatCheckBox chk_kam;
    private AppCompatCheckBox chk_jum;
    private AppCompatCheckBox chk_sab;
    private AppCompatCheckBox chk_min;
    private View    prg_sumbit;
    private View    scr_form;
    private ImageView img_photo;
    private ImageView img_location;
    private Button    btn_location;


    String userId = "user1";

    PlaceModel mPlaceDB = new PlaceModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);
        scr_form    = findViewById(R.id.scr_form);
        img_photo   = findViewById(R.id.img_photo);
        edt_name    = findViewById(R.id.edt_name);
        txt_address = findViewById(R.id.txt_address);
        edt_type    = findViewById(R.id.edt_type);
        edt_description = findViewById(R.id.edt_description);
        edt_open   = findViewById(R.id.edt_open);
        edt_closed = findViewById(R.id.edt_closed);
        chk_sen = findViewById(R.id.chk_sen);
        chk_sel = findViewById(R.id.chk_sel);
        chk_rab = findViewById(R.id.chk_rab);
        chk_kam = findViewById(R.id.chk_kam);
        chk_jum = findViewById(R.id.chk_jum);
        chk_sab = findViewById(R.id.chk_sab);
        chk_min = findViewById(R.id.chk_min);
        prg_sumbit = findViewById(R.id.prg_sumbit);
        btn_location = findViewById(R.id.btn_location);
        img_location = findViewById(R.id.img_location);

        prepareType();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btn_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivityForResult(new Intent(OpenActivity.this, PlacePickerActivity.class), PLACE_PICKER_REQUEST);
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        });

        if (getIntent() != null && getIntent().hasExtra(PlaceModel.PLACE_ID)) {
            //Edit
            fill(getIntent().getStringExtra(PlaceModel.PLACE_ID));
        }
        else {
            //New
            mPlaceDB.put(PlaceModel.PLACE_ID, "P-" + AppInfo.getUserId() + "-" + Long.toHexString(System.currentTimeMillis()));
        }

    }

    final int PLACE_PICKER_REQUEST = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.open, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.submit) {
            submit();
            return true;
        }
        else if (id == android.R.id.home) {
            onBackPressed();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            switch (requestCode) {
                case PLACE_PICKER_REQUEST:
                    if (resultCode == RESULT_OK) {
                        double latitude  = data.getDoubleExtra("latitude", 0);
                        double longitude = data.getDoubleExtra("longitude", 0);
                        String address   = data.getStringExtra("address");

                        mPlaceDB.put(PlaceModel.LATLNG,  Arrays.asList(latitude, longitude));
                        mPlaceDB.put(PlaceModel.ADDRESS, address);
                        mPlaceDB.setGroup(new LatLng(latitude, longitude));

                        txt_address.setText(address);

                        Glide.with(img_location)
                        .load(MapStatic.getImageUrl(OpenActivity.this, latitude, longitude))
                        .into(img_location);
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

    }

    private void fill(String placeid) {
        PlaceDB.getPlace(placeid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    PlaceModel value = new PlaceModel(dataSnapshot);
                    if (value != null) {
                        mPlaceDB = value;
                        edt_name.setText(value.getString(PlaceModel.NAME));
                        edt_type.setText(value.getString(PlaceModel.TYPE));
                        edt_description.setText(value.getString(PlaceModel.DESCRIPTION));
                        txt_address.setText(value.getString(PlaceModel.ADDRESS));

                        List<Double> latlng = (List) value.get(PlaceModel.LATLNG);

                        Glide.with(img_location)
                                .load(MapStatic.getImageUrl(OpenActivity.this, latlng.get(0), latlng.get(1)))
                                .into(img_location);

                        List<String> workhour = (List) value.get(PlaceModel.WORK_HOUR);

                        edt_open  .setText(workhour.get(0));
                        edt_closed.setText(workhour.get(1));
                        chk_sen.setChecked(workhour.get(2).equals("1"));
                        chk_sel.setChecked(workhour.get(3).equals("1"));
                        chk_rab.setChecked(workhour.get(4).equals("1"));
                        chk_kam.setChecked(workhour.get(5).equals("1"));
                        chk_jum.setChecked(workhour.get(6).equals("1"));
                        chk_sab.setChecked(workhour.get(7).equals("1"));
                        chk_min.setChecked(workhour.get(8).equals("1"));
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

    private boolean changePhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(CAMERA)) {
                    Snackbar.make(edt_name, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                            .setAction(android.R.string.ok, new View.OnClickListener() {
                                @Override
                                @TargetApi(Build.VERSION_CODES.M)
                                public void onClick(View v) {
                                    requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA);
                                }
                            });
                } else {
                    requestPermissions(new String[]{READ_CONTACTS}, REQUEST_CAMERA);
                }
                return false;
            }
        }

        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                changePhoto();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void submit() {
        if (mSubmitTask != null) {
            return;
        }

        // Reset errors.
        edt_name.setError(null);
        txt_address.setError(null);
        edt_type.setError(null);
        edt_description.setError(null);

        showProgress(true);

        mPlaceDB.put(PlaceModel.NAME, edt_name.getText().toString());
        mPlaceDB.put(PlaceModel.TYPE, edt_type.getText().toString());
        mPlaceDB.put(PlaceModel.DESCRIPTION, edt_description.getText().toString());

        List<String> workhour = Arrays.asList(
                  edt_open.getText().toString()
                , edt_closed.getText().toString()
                , chk_sen.isChecked() ? "1" : "0"
                , chk_sel.isChecked() ? "1" : "0"
                , chk_rab.isChecked() ? "1" : "0"
                , chk_kam.isChecked() ? "1" : "0"
                , chk_jum.isChecked() ? "1" : "0"
                , chk_sab.isChecked() ? "1" : "0"
                , chk_min.isChecked() ? "1" : "0"
        );
        mPlaceDB.put(PlaceModel.WORK_HOUR, workhour);

        PlaceDB.insert(mPlaceDB).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                showProgress(false);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                edt_name.setError("Error " + e.getMessage());
                edt_name.requestFocus();
            }
        });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            scr_form.setVisibility(show ? View.GONE : View.VISIBLE);
            scr_form.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    scr_form.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            prg_sumbit.setVisibility(show ? View.VISIBLE : View.GONE);
            prg_sumbit.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    prg_sumbit.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            prg_sumbit.setVisibility(show ? View.VISIBLE : View.GONE);
            scr_form.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void prepareType() {
        List<String> opsi = new ArrayList<>();
        opsi.add("Kantor Polisi");
        opsi.add("Puskesmas");
        opsi.add("Rumah Sakit");
        opsi.add("Tempat Makan");
        opsi.add("Tempat Umum");

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(OpenActivity.this,
                        android.R.layout.simple_dropdown_item_1line, opsi);

        edt_type.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class SubmitTask extends AsyncTask<Void, Void, Boolean> {

        SubmitTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.

                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSubmitTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                edt_name.setError("Somthing error here");
                edt_name.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mSubmitTask = null;
            showProgress(false);
        }
    }


}

