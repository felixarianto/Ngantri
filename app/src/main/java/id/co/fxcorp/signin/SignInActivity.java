package id.co.fxcorp.signin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import id.co.fxcorp.db.Prefs;
import id.co.fxcorp.db.UserDB;
import id.co.fxcorp.db.UserModel;
import id.co.fxcorp.ngantri.R;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class SignInActivity extends AppCompatActivity {

    private final String TAG = "SignInActivity";

    private EditText edt_email;
    private EditText edt_password;
    private Button btn_signin;
    private TextView txt_signup;
    private View prg_signin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_activity);

        edt_email    =  findViewById(R.id.edt_email);
        edt_password =  findViewById(R.id.edt_password);
        prg_signin =  findViewById(R.id.prg_signin);
        btn_signin =  findViewById(R.id.btn_signin);
        txt_signup =  findViewById(R.id.txt_signup);

        edt_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    signin();
                    return true;
                }
                return false;
            }
        });

        btn_signin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signin();
            }
        });
        txt_signup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });

    }

    private void signin() {
        prg_signin.setVisibility(View.VISIBLE);

        UserDB.login(edt_email.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserModel user = dataSnapshot.getChildren().iterator().next().getValue(UserModel.class);
                    if (user != null && user.password.equals(edt_password.getText().toString())) {
                        Prefs.setAccount(SignInActivity.this, user.email, user.password);
                        UserDB.MySELF  = user;
                        prg_signin.setVisibility(View.GONE);
                        finish();
                        return;
                    }
                }
                Toast.makeText(SignInActivity.this, "Email atau password tidak tepat", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    int RC_SIGN_IN = 1;
    GoogleSignInClient mGoogleSignInClient;
    public void signup() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);

                Intent intent = new Intent(this, SignUpActivity.class);
                intent.putExtra("email", account.getEmail());
                intent.putExtra("name",  account.getDisplayName());
                intent.putExtra("photo", account.getPhotoUrl().toString());

                mGoogleSignInClient.signOut();

                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }


}

