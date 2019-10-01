package id.co.fxcorp.signin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.theartofdev.edmodo.cropper.CropImageView.RequestSizeOptions;

import java.io.File;

import id.co.fxcorp.db.Prefs;
import id.co.fxcorp.db.UserDB;
import id.co.fxcorp.db.UserModel;
import id.co.fxcorp.ngantri.AppService;
import id.co.fxcorp.ngantri.R;
import id.co.fxcorp.storage.Storage;

/**
 * A login screen that offers login via email/password.
 */
public class SignUpActivity extends AppCompatActivity {

    private final String TAG = "OpenActivity";
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_CAMERA = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    private EditText edt_name;
    private EditText edt_email;
    private EditText edt_password;
    private EditText edt_password_confirm;
    private View    prg_sumbit;
    private View    scr_form;
    private ImageView img_photo;
    private FloatingActionButton btn_photo;
    private ProgressBar prg_photo;
    private AppCompatButton btn_signout;

    UserModel mUser = new UserModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signup_activity);
        scr_form    = findViewById(R.id.scr_form);
        img_photo   = findViewById(R.id.img_photo);
        edt_name    = findViewById(R.id.edt_name);
        edt_email = findViewById(R.id.edt_email);
        edt_password    = findViewById(R.id.edt_password);
        edt_password_confirm = findViewById(R.id.edt_password_confirm);
        btn_signout = findViewById(R.id.btn_signout);

        prg_sumbit = findViewById(R.id.prg_sumbit);
        btn_photo = findViewById(R.id.btn_photo);
        prg_photo = findViewById(R.id.prg_photo);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setTitle("Profil Saya");

        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePhoto();
            }
        });

        if (getIntent().hasExtra("id")) {
            UserDB.getUser(getIntent().getStringExtra("id")).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.exists()) {
                            mUser = dataSnapshot.getValue(UserModel.class);
                            edt_name .setText(mUser.name);
                            edt_email.setText(mUser.email);
                            edt_email.setEnabled(false);
                            edt_password.setText(mUser.password);
                            edt_password_confirm.setText(mUser.password);

                            Glide.with(img_photo).load(mUser.photo)
                                    .apply(new RequestOptions().placeholder(R.drawable.ic_person_default))
                                    .into(img_photo);

                            btn_signout.setVisibility(View.VISIBLE);
                            btn_signout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    AppService.signOut(SignUpActivity.this);
                                    finish();
                                }
                            });
                        }
                        else {
                            finish();
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
        else {
            mUser.id    = Long.toHexString(System.currentTimeMillis()).toUpperCase();
            mUser.photo = getIntent().getStringExtra("photo");
            mUser.name  = getIntent().getStringExtra("name");
            mUser.email = getIntent().getStringExtra("email");

            edt_name .setText(mUser.name);
            edt_email.setText(mUser.email);
            edt_email.setEnabled(false);

            Glide.with(img_photo).load(mUser.photo).into(img_photo);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.signup, menu);
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
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    if (resultCode == RESULT_OK) {
                        CropImage.ActivityResult result = CropImage.getActivityResult(data);
                        final Uri resultUri = result.getUri();

                        File file = new File(resultUri.getPath());
                        if (!file.exists()) {
                            Toast.makeText(SignUpActivity.this, "Not Exists ", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        prg_photo.setIndeterminate(true);
                        prg_photo.setVisibility(View.VISIBLE);

                        final StorageReference ref = Storage.images(mUser.id, mUser.email + "-pp.jpg");

                        ref.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return ref.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                prg_photo.setVisibility(View.INVISIBLE);
                                if (task.isSuccessful()) {
                                    String img_url = task.getResult().toString();
                                    mUser.photo = img_url;
                                    Glide.with(img_photo).load(resultUri).into(img_photo);
                                } else {
                                    Toast.makeText(img_photo.getContext(), "Upload image bermasalah, periksa internet Anda", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                        Exception error = result.getError();
                    }
                    break;

            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

    }

    private boolean changePhoto() {
        CropImage
        .activity()
        .setGuidelines(CropImageView.Guidelines.ON)
        .setRequestedSize(680, 680, RequestSizeOptions.RESIZE_EXACT)
        .setAspectRatio(680, 680)
        .start(this);
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */




    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void submit() {
        if (edt_name.getText().toString().isEmpty()) {
            edt_name.setError("Tidak boleh kosong");
            return;
        }

        if (edt_password.getText().toString().isEmpty()) {
            edt_password.setError("Tidak boleh kosong");
            return;
        }

        if (edt_password_confirm.getText().toString().isEmpty()) {
            edt_password_confirm.setError("Tidak boleh kosong");
            return;
        }

        if (!edt_password.getText().toString().equals(edt_password_confirm.getText().toString())) {
            edt_password_confirm.setText("");
            edt_password_confirm.setError("Konfirmasi password salah");
            return;
        }

        // Reset errors.
        edt_name.setError(null);
        edt_password.setError(null);
        edt_password_confirm.setError(null);

        showProgress(true);

        mUser.name     = edt_name.getText().toString();
        mUser.password = edt_password.getText().toString();

        UserDB.insert(mUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                AppService.signUp(SignUpActivity.this, mUser);
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

}

