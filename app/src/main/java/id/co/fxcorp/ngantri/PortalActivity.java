package id.co.fxcorp.ngantri;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import id.co.fxcorp.db.AntriDB;
import id.co.fxcorp.db.AntriModel;
import id.co.fxcorp.db.PlaceDB;
import id.co.fxcorp.db.PlaceModel;

public class PortalActivity extends AppCompatActivity {

    private final String TAG = "PortalActivity";

    private String mPlaceId = "";
    private PlaceModel mPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        mPlaceId = intent.getStringExtra(PlaceModel.PLACE_ID);

        setTitle(intent.getStringExtra(PlaceModel.NAME));
        setContentView(R.layout.portal_activity);

        initAntrian();
        initChat();

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
        DialogChoosePlace.close(PortalActivity.this, mPlace, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }

        });

    }

    private ImageView img_photo;
    private TextView txt_name;
    private TextView txt_number;
    private ImageButton btn_previous;
    private ImageButton btn_next;
    private Button      btn_call;
    private AppCompatCheckBox chk_call_next;
    private CardView crd_bottom;

    private long  mCurrentNumber = 0;
    private long  mLastNumber    = 0;
    private int   mCallQty = 0;
    private void initAntrian() {
        img_photo   = findViewById(R.id.img_photo);
        txt_name    = findViewById(R.id.txt_name);
        txt_number  = findViewById(R.id.txt_number);
        btn_previous = findViewById(R.id.btn_previous);
        btn_next = findViewById(R.id.btn_next);
        btn_call = findViewById(R.id.btn_call);
        chk_call_next = findViewById(R.id.chk_call_next);
        crd_bottom    = findViewById(R.id.crd_bottom);

        txt_name.setText("");
        btn_call.setText("");
        btn_call.setEnabled(false);
        img_photo.setImageBitmap(null);

        PlaceDB.getPlace(mPlaceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    PlaceModel place = new PlaceModel(dataSnapshot);
                    if (place != null) {
                        mPlace = place;
                        mCurrentNumber = place.getNumberCurrent();
                        mLastNumber    = place.getNumberLast();
                        txt_number.setText(mCurrentNumber + "");
                        btn_call  .setText("PANGGIL");
                        UI_HANDLER.post(mLoadAntrianRunnable);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PlaceDB.setNumberCurrent(mPlaceId, mCurrentNumber)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mCallQty++;
                            btn_call.setText("PANGGIL " + mCallQty);
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNext();
            }
        });

        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToPrevious();
            }
        });
    }

    private Handler UI_HANDLER = new Handler();
    private Query mLoadQuery;
    private ValueEventListener mLoadEventListener;
    private Runnable mLoadAntrianRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mLoadQuery = AntriDB.getItem(mPlaceId, mCurrentNumber);
                mLoadQuery.addListenerForSingleValueEvent(mLoadEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            AntriModel antri = dataSnapshot.getValue(AntriModel.class);
                            if (antri != null) {
                                txt_name  .setText(antri.cust_name);
                                btn_call.setEnabled(true);
                                Glide.with(img_photo).load(antri.cust_photo).into(img_photo);

                                mLoadQuery = null;
                                mLoadEventListener = null;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }
    };
    private void goToNext() {
        if (mCurrentNumber == mLastNumber) {
            return;
        }
        mCurrentNumber++;
        mCallQty = 0;
        txt_number.setText(mCurrentNumber + "");
        txt_name.setText("");
        btn_call.setText("PANGGIL");
        btn_call.setEnabled(false);
        img_photo.setImageBitmap(null);

        if (mLoadQuery != null && mLoadEventListener != null) {
            mLoadQuery.removeEventListener(mLoadEventListener);
        }

        UI_HANDLER.postDelayed(mLoadAntrianRunnable, 300);
    }

    private void goToPrevious() {
        if (mCurrentNumber == 0) {
            return;
        }
        mCurrentNumber--;
        mCallQty = 0;
        txt_number.setText(mCurrentNumber + "");
        txt_name.setText("");
        btn_call.setText("PANGGIL");
        btn_call.setEnabled(false);
        img_photo.setImageBitmap(null);

        if (mLoadQuery != null && mLoadEventListener != null) {
            mLoadQuery.removeEventListener(mLoadEventListener);
        }

        UI_HANDLER.postDelayed(mLoadAntrianRunnable, 300);

    }

    private void initChat() {

    }
}
