package ovh.enterprise.socialnetwork;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class RegisterActivity extends AppCompatActivity {

    private EditText UserEmail, UserPassword, UserConfirmPassword; // .0
    private Button CreateAccountButton; // .0
    private ProgressDialog loadingBar; // .8

    private FirebaseAuth mAuth; // . 5

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance(); // .6

        UserEmail = (EditText) findViewById(R.id.register_email); // .1
        UserPassword = (EditText) findViewById(R.id.register_password); // .1
        UserConfirmPassword = (EditText) findViewById(R.id.register_confirm_password); // .1
        CreateAccountButton = (Button) findViewById(R.id.register_create_account); // .1
        loadingBar = new ProgressDialog(this); // .8

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {   // .2
            @Override
            public void onClick(View v)  // .2
            {
                CreateAccountButton();  // .2
            }
        });

    }

    @Override  // .12 # 12, 13 m
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) //  sprawdzamy if user is already register we send him to MainActivity
        {
            SendUserToMainActivity(); //  jeśli wykryjemy że user nie jest zalogowany wysyłamy go do login activity naszą własną metodą
        }
    }


    private void CreateAccountButton() // .3 kiedy urzytkownik kliknie button CreateAccountButton musimy pobrać to co wpisał w password, confirm password #9 3,20
    {
        String email = UserEmail.getText().toString(); // .3
        String password = UserPassword.getText().toString(); // .3
        String confirmPassword = UserConfirmPassword.getText().toString(); // .3
        // teraz walidiacja wprowadzonych danych . 4
        if (TextUtils.isEmpty(email)) // . 4
        {
            Toast.makeText(RegisterActivity.this, "please write your email...", Toast.LENGTH_SHORT); // . 4
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(RegisterActivity.this, "please write your password...", Toast.LENGTH_SHORT); // . 4
        }
        else if(TextUtils.isEmpty(confirmPassword))
        {
            Toast.makeText(RegisterActivity.this, "please confirm your password...", Toast.LENGTH_SHORT); // . 4
        }
        else if(!password.equals(confirmPassword)) // do porównywania stringów uzywamy .equals
        {
            Toast.makeText(RegisterActivity.this, "hasła nie pasują do siebie ", Toast.LENGTH_SHORT); // . 4
        }
        else // . 6
        {
            loadingBar.setTitle("Creating new Account"); // .9
            loadingBar.setMessage("please wait, while we are creating your new account.. "); // .9
            loadingBar.show(); // .9
            loadingBar.setCanceledOnTouchOutside(true); // .9 this doalog box will not  disapeerd until authenticate the user

            mAuth.createUserWithEmailAndPassword(email, password) // . 6
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() { // . 6
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)  // . 6
                        {
                            if (task.isSuccessful()) // . 7
                            {
                                SendUserToSetupActivity(); // we send user to setup activity . 11

                                Toast.makeText(RegisterActivity.this, "U R authenticated sucessfully..  ", Toast.LENGTH_SHORT); // . 7
                                loadingBar.dismiss(); // . 10
                            }
                            else
                            {
                                String message = task.getException().getMessage(); // .7 tworzymy zmienna string o nazwie message w ktorej wyswietlimy przechwycony komunikat wyjątku znalezionego błędu
                                Toast.makeText(RegisterActivity.this, "error occured" + message, Toast.LENGTH_SHORT); // . 7
                                loadingBar.dismiss(); // . 10
                            }
                        }
                    });
        }

    }

    private void SendUserToSetupActivity() // . 11
    {
        Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class); // . 11
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // . 11
        startActivity(setupIntent); // . 11
        finish(); // . 11 >> a teraz przechodzimy do okodowania przycisku Logout z MainActivity
    }

    private void SendUserToMainActivity()  // .12
    {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        startActivity(mainIntent);
        finish();
    }

}
