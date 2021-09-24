package ovh.enterprise.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

private Button LoginButton;
private EditText UserEmail, UserPassword;
private TextView NeedNewAccoutLink;

private FirebaseAuth mAuth; // .5
private ProgressDialog loadingBar; // .5

private ImageView googleSignInButton; // .7  # 25
private static final int RC_SIGN_IN = 1; // .7  # 25
private static final String TAG = "LoginActivity"; // .7  # 25
private GoogleApiClient mGoogleSignInClient;   // .7  # 25


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance(); // .5

        NeedNewAccoutLink = (TextView) findViewById(R.id.register_account_link); // .1
        UserEmail = (EditText) findViewById(R.id.login_email); // .1
        UserPassword = (EditText) findViewById(R.id.login_password); // .1
        LoginButton = (Button) findViewById(R.id.login_button); // .1
        loadingBar = new ProgressDialog(this);  // .5

        googleSignInButton = (ImageView) findViewById(R.id.google_signin_button);   // .7  # 25


        // .2 we need to set click listener to NeedNewAccoutLink when user not have any account we direct him to register activity #7, 11m
        NeedNewAccoutLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               SendUserToRegisterActivity(); // . 3
            }
        });

    // .5  okodowanie login button # 11, 1 m
    LoginButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            //  .5 we prepare method with allow user to login to their account with was created during registration
            AllowingUserToLogin();
        }
    });

    // Configure Google Sign In   //  .7   #  25
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(this)   //  .8   #  25
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener()
                {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
                    {
                        Toast.makeText(LoginActivity.this, "Connection to Google  faild.. ", Toast.LENGTH_SHORT);
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleSignInButton.setOnClickListener(new View.OnClickListener()   //  .9   #  25
        {
            @Override
            public void onClick(View v)
            {
                signIn();
            }
        });

    } ///////////////////////////////// onCreate ///////////////////////////////

    private void signIn()  // .7  # 25  we copy this from  https://firebase.google.com/docs/auth/android/google-signin and we change sth
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient); // we must inicialize mGoogleSignInClient
        startActivityForResult(signInIntent, RC_SIGN_IN); // we must inicialize RC_SIGN_IN
    }

    @Override   // .7  # 25
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN)
        {

            loadingBar.setTitle("Google Sign In");   // .13   # 25
            loadingBar.setMessage("please wait, while we are loging you using Google account .. ");   // .13   # 25
            loadingBar.setCanceledOnTouchOutside(true);   // .13   # 25
            loadingBar.show();   // .13   # 25

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);  // .10   # 25
            if (result.isSuccess())
            {
                GoogleSignInAccount account = result.getSignInAccount();   // .11   # 25
                firebaseAuthWithGoogle(account);   // .11   # 25
                Toast.makeText(this, "Please wait, we are geting your getresult . ", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Cant get Auth result ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct)
    {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            SendUserToMainActivity();  // .12   # 25
                            loadingBar.dismiss();   // .13   # 25

                        } else
                            {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String mesage = task.getException().toString(); // .12   # 25
                            SendUserToLoginActivity();
                            Toast.makeText(LoginActivity.this, "Not auth, try again " + mesage, Toast.LENGTH_SHORT).show(); // .12   # 25
                            loadingBar.dismiss();   // .13   # 25
                            }


                    }
                });
    }

    @Override // . 6
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) //  sprawdzamy if user is already log in
        {
            SendUserToMainActivity(); //  jeśli wykryjemy że user nie jest zalogowany wysyłamy go do login activity naszą własną metodą
        }

    }

    private void AllowingUserToLogin() // .5
    {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "type email", Toast.LENGTH_SHORT);
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "type password", Toast.LENGTH_SHORT);
        }
        else  // .5
        {
            loadingBar.setTitle("Login");
            loadingBar.setMessage("please wait, while we are loging you in to your account ");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();


            mAuth.signInWithEmailAndPassword(email, password)// .5
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) 
                        {
                            if(task.isSuccessful())
                            {
                                SendUserToMainActivity(); // when login is succesful we send him to main activity
                                Toast.makeText(LoginActivity.this, "you are Loged succesfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss(); // when login is successful we must dismiss loading bar
                            }
                            else
                            {
                                String message = task.getException().getMessage(); // if soething goes wrong we cath error message
                                Toast.makeText(LoginActivity.this, "error ocurreg: " +message, Toast.LENGTH_LONG).show();
                                loadingBar.dismiss(); // when login i unssuccessful we must dismiss loading bar
                            } // >>>> next we care about firebase database
                        }
                    });
        }


    }

    private void SendUserToMainActivity()  // .5
    {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToRegisterActivity() // . 3.1
    {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
        // finish();  // jak wyrzucimy ten finisz to po prejściu do RegisterActivity będzie mozna wrócić przyciskiem cofnięcia do LoginActivity. Jesli byśmy zostawili fnisch() to byśmy wychodzili z aplikacji po kliknięciu przycisku cofnij
        // . 4 >> przechodzimy do activity_register.xml a potem do okodowania RegisterActivity
    }

    private void SendUserToSetupActivity()
    {
        Intent setupIntent = new Intent(LoginActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(LoginActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

}
