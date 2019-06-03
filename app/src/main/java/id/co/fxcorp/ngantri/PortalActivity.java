package id.co.fxcorp.ngantri;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
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
import android.widget.Toast;

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
        setContentView(R.layout.portal_activity);

        initAntrian();
        initChat();

        mTextToSpeech = new TextToSpeech(PortalActivity.this, new TextToSpeech.OnInitListener() {
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
    private TextView txt_info1;
    private TextView txt_info2;
    private TextView txt_info3;
    private ImageButton btn_previous;
    private ImageButton btn_next;
    private Button      btn_call;
    private Button      btn_turn;
    private AppCompatCheckBox chk_call_next;
    private CardView crd_bottom;

    private long mCurrentFocus = 0;
    private long  mLastNumber    = 0;
    private int   mCallQty = 0;
    private void initAntrian() {
        img_photo   = findViewById(R.id.img_photo);
        txt_name    = findViewById(R.id.txt_name);
        txt_number  = findViewById(R.id.txt_number);
        btn_previous = findViewById(R.id.btn_previous);
        btn_next = findViewById(R.id.btn_next);
        btn_call = findViewById(R.id.btn_call);
        btn_turn = findViewById(R.id.btn_turn);
        chk_call_next = findViewById(R.id.chk_call_next);
        crd_bottom    = findViewById(R.id.crd_bottom);
        txt_info1    = findViewById(R.id.txt_info1);
        txt_info2    = findViewById(R.id.txt_info2);
        txt_info3    = findViewById(R.id.txt_info3);

        btn_next     .setEnabled(false);
        btn_previous .setEnabled(false);
        btn_call     .setEnabled(false);
        btn_turn     .setEnabled(false);
        chk_call_next.setVisibility(View.INVISIBLE);

        txt_info1.setText("");
        txt_info2.setText("");
        txt_name  .setText("");
        txt_number.setText("-");
        img_photo.setImageResource(R.drawable.ic_person_default);

        final Query query = PlaceDB.getPlace(mPlaceId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    PlaceModel place = new PlaceModel(dataSnapshot);
                    if (place != null) {
                        mPlace = place;
                        if (mCurrentFocus == 0) {
                            mCurrentFocus = place.getNumberCurrent();
                            mLastNumber   = place.getNumberLast();

                            if (mCurrentFocus > 0) {
                                txt_number.setText(mCurrentFocus + "");

                                if (mCurrentFocus < mLastNumber) {
                                    btn_next.setEnabled(true);
                                }

                                if (mCurrentFocus > 1) {
                                    btn_previous.setEnabled(true);
                                }

                                UI_HANDLER.post(mLoadAntrianRunnable);
                            }
                            else {
                                txt_number.setText("0");
                                txt_name  .setText("Tidak ada antrian");
                            }
                        }
                        else {
                            mLastNumber = place.getNumberLast();
                        }

                        txt_info1.setText("Nomor Terkini : "  + mPlace.getNumberCurrent());
                        if (mPlace.getNumberCurrent() == 0) {
                            txt_info2.setText("Antrian Tesisa : 0");
                        }
                        else {
                            txt_info2.setText("Antrian Tersisa : " + (mPlace.getNumberLast() - mPlace.getNumberCurrent()));
                        }
                        txt_info3.setText("Nomor Terakhir : " + mPlace.getNumberLast());
                    }
                    else {
                        txt_number.setText("0");
                        txt_name  .setText("Tidak ada antrian");
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
                    mCallQty++;
                    btn_call.setText("PANGGILAN KE- " + mCallQty);

                    String text = "Antrian nomor " + txt_number.getText().toString() + " " + txt_name.getText().toString();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mTextToSpeech.speak(text,TextToSpeech.QUEUE_FLUSH,null,null);
                    } else {
                        mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                    }

                    String call_msg = "Panggilan ke- " + mCallQty + " Silahkan menuju ke loket.";

                    AntriDB.call(mCurrentAntrian.id, mCallQty, call_msg);

                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }
        });

        btn_turn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextTurn();
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNext();
            }
        });

        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPrevious();
            }
        });
    }

    private Handler UI_HANDLER = new Handler();
    private AntriModel mCurrentAntrian;
    private Query mLoadQuery;
    private ValueEventListener mLoadEventListener;
    private Runnable mLoadAntrianRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mLoadQuery = AntriDB.getItem(mPlaceId, mCurrentFocus);
                mLoadQuery.addValueEventListener(mLoadEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            AntriModel antri = dataSnapshot.getValue(AntriModel.class);
                            if (antri != null) {
                                txt_name  .setText(antri.cust_name);
                                Glide.with(img_photo).load(antri.cust_photo).into(img_photo);

                                if (mCurrentFocus == mPlace.getNumberCurrent()) {
                                    mCurrentAntrian = antri;
                                    btn_call.setEnabled(true);
                                    btn_turn.setEnabled(true);
                                }
                                mLoadQuery.removeEventListener(mLoadEventListener);
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
    private void showNext() {
        if (mCurrentFocus == mLastNumber) {
            return;
        }
        mCurrentFocus++;
        mCallQty = 0;

        if (mCurrentFocus == mLastNumber) {
            btn_next.setEnabled(false);
        }

        btn_previous.setEnabled(true);
        btn_call.setEnabled(false);
        btn_turn.setEnabled(false);

        txt_number.setText(mCurrentFocus + "");
        txt_name  .setText("-");
        btn_call  .setText("PANGGIL");
        img_photo.setImageResource(R.drawable.ic_person_default);

        if (mLoadQuery != null && mLoadEventListener != null) {
            mLoadQuery.removeEventListener(mLoadEventListener);
        }

        UI_HANDLER.postDelayed(mLoadAntrianRunnable, 300);
    }
    private void nextTurn() {
        if (mPlace.getNumberCurrent() == mPlace.getNumberLast()) {
            PlaceDB.setNumberCurrent(mPlaceId, 0)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mCurrentFocus = 0;
                    mCallQty = 0;

                    if (mCurrentFocus == mLastNumber) {
                        btn_next.setEnabled(false);
                    }

                    btn_previous.setEnabled(false);
                    btn_call.setEnabled(false);
                    btn_turn.setEnabled(false);

                    txt_number.setText("0");
                    txt_name  .setText("Tidak ada antrian");
                    btn_call  .setText("PANGGIL");
                    img_photo.setImageResource(R.drawable.ic_person_default);

                    if (mLoadQuery != null && mLoadEventListener != null) {
                        mLoadQuery.removeEventListener(mLoadEventListener);
                    }

                    Toast.makeText(PortalActivity.this, "Antrian telah usai!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            final long new_current = mPlace.getNumberCurrent() + 1;
            PlaceDB.setNumberCurrent(mPlaceId, new_current)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mCurrentFocus = new_current;
                    mCallQty = 0;

                    if (mCurrentFocus == mLastNumber) {
                        btn_next.setEnabled(false);
                    }

                    btn_previous.setEnabled(true);
                    btn_call.setEnabled(false);
                    btn_turn.setEnabled(false);

                    txt_number.setText(mCurrentFocus + "");
                    txt_name  .setText("-");
                    btn_call  .setText("PANGGIL");
                    img_photo.setImageResource(R.drawable.ic_person_default);

                    if (mLoadQuery != null && mLoadEventListener != null) {
                        mLoadQuery.removeEventListener(mLoadEventListener);
                    }

                    UI_HANDLER.postDelayed(mLoadAntrianRunnable, 300);
                }
            });
        }
    }

    private void showPrevious() {
        if (mCurrentFocus == 0) {
            return;
        }

        mCurrentFocus--;
        mCallQty = 0;

        if (mCurrentFocus <= 1) {
            btn_previous.setEnabled(false);
        }

        btn_next.setEnabled(true);
        btn_call.setEnabled(false);
        btn_turn.setEnabled(false);

        txt_number.setText(mCurrentFocus + "");
        txt_name  .setText("-");
        btn_call  .setText("PANGGIL");
        img_photo.setImageResource(R.drawable.ic_person_default);

        if (mLoadQuery != null && mLoadEventListener != null) {
            mLoadQuery.removeEventListener(mLoadEventListener);
        }

        UI_HANDLER.postDelayed(mLoadAntrianRunnable, 300);

    }

    private void initChat() {

    }
}
