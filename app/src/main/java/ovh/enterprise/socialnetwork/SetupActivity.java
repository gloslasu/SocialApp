package ovh.enterprise.socialnetwork;

  // .00 krok   #00 v  # 00 dokumentacja tech

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, CountryName; // .1
    private Button SaveInformationbutton; // .1
    private CircleImageView ProfileImage; // .1

    private FirebaseAuth mAuth;  //.5    # 13     tech. 6
    private DatabaseReference UsersRef;  //.6      # 13     tech. 7
    String currentUserID;  //.10     # 13     tech. 9
    private ProgressDialog loadingBar;  // .17  #13  # 16

    static final int GALLERY_PICK_REQUEST = 1; // .22  #13  # 24   // The request code
    private StorageReference UserProfileImageRef; // # 14  #..........................

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        UserName = (EditText) findViewById(R.id.setup_username); // .1
        FullName = (EditText) findViewById(R.id.setup_full_name); // .1
        CountryName = (EditText) findViewById(R.id.setup_country_name); // .1
        SaveInformationbutton = (Button) findViewById(R.id.setup_information_button); // .1
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image); // .1

        loadingBar = new ProgressDialog(this); // .18  #13  # 17

        mAuth = FirebaseAuth.getInstance(); //.7     # 13    tech. 8
        currentUserID = mAuth.getCurrentUser().getUid(); //.11     # 13    tech. 10   //  we need unike user id for every users
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID); // . 8    # 13    tech. 8

        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images"); // #14...................... // we create folder in Firebase Storage named "Profile Images"


        SaveInformationbutton.setOnClickListener(new View.OnClickListener() { // .2    # 13    # tech.4
            @Override
            public void onClick(View v)
            {
                SaveAccountSetupInformation(); // .3     # 13     # tech.4
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() // .20  #14  # 22
        {
            @Override // .20  #14  # 22
            public void onClick(View v) // .20  #13  # 22
            {
                pickGallery();  // .21  #14  # 23

            }
        });

    } /////////////////////////////// onCreate //////////////////////////////////

    private void pickGallery() // .22  #14  # 23
    {
        // Show only images, no videos or anything else  http://codetheory.in/android-pick-select-image-from-gallery-with-intents/
        Intent galleryIntent = new Intent(); // .22  #14  # 23
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT); // .22  #14  # 23
        galleryIntent.setType("image/*"); // .22  #14  # 23  // we must tell with type of filel we want to pick from the mobile phone
        startActivityForResult(galleryIntent, GALLERY_PICK_REQUEST); // .21  #14  # 23  // we need variable Gallery_Pick so we must create it // https://developer.android.com/training/basics/intents/result
    }

    @Override //  .23  #14
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK_REQUEST && resultCode == RESULT_OK && data != null)
            {
                Uri uri = data.getData();
                ProfileImage.setImageURI(uri);
                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg"); //.24  #14  #28   set name of image with uniqe user ID +
                filePath.putFile(uri); //.25  #14  #29  // save crop image in firebase database
            }
    }

    private void SaveAccountSetupInformation() //  .4   # 13     # tech.5
    {
        String username = UserName.getText().toString();  //  .4   # 13     # tech.5.1
        String fullname = FullName.getText().toString();  //  .4   # 13     # tech.5.1
        String country = CountryName.getText().toString();//  .4   # 13     # tech.5.1

        loadingBar.setTitle("Saving Information"); // .17  #13  # 18
        loadingBar.setMessage("please wait, while  we are saving Your data...");  // .17  #13  # 18
        loadingBar.show();  // .17  #13  # 18
        loadingBar.setCanceledOnTouchOutside(true);  // .17  #13  # 18

        HashMap userMap = new HashMap();  //  .12   # 13   # tech.11  // tworzymy drzewo wpisów w DATABASE > REAL TIME DATABASE WFIREBASE
        userMap.put("username", username); //  .12   # 13   # tech.11
        userMap.put("fullname", fullname); // .12   # 13   # tech.11
        userMap.put("country", country);  // .12   # 13   # tech.11
        userMap.put("gender", "none"); // .12   # 13   # tech.11
        userMap.put("dob", "none"); //  .12   # 13   # tech.11 day of birthday
        userMap.put("relationshipstatus", "none"); // .12   # 13   # tech.11

        UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() // .13  #13  # 12    12.30 addOnCompleteListener dodajemy zeby nam powiedzialo czy jest kompletne czy nie
        {
            @Override // .13  #13  # 12
            public void onComplete(@NonNull Task task) // .13  #13  # 12
            {
                if (task.isSuccessful()) // .14  #13  # 12
                {
                    loadingBar.dismiss(); // .18  #13  # 19
                    SendUserToMainActivity();  // .16  #13  # 14
                    Toast.makeText(SetupActivity.this, "Your account is created succesfully", Toast.LENGTH_LONG); // .19  #13  # 20 recording in storage firebase database succesfull
                }
                else  // .16  #13  # 14
                {
                    String message = task.getException().getMessage(); // in message we couth error type // .16  #13  # 14
                    loadingBar.dismiss(); // .18  #13  # 19
                    Toast.makeText(SetupActivity.this, "Error: "+ message, Toast.LENGTH_LONG); // .19 #13  # 21
                }
            }
        });
    }

    private void SendUserToMainActivity() // .16  #13  # 15
    {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class); // .16  #13  # 15
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // .16  #13  # 15 walidjacja, po przejściu do MainActivity jak klikniemy przycisk "wstecz" to nie bedziemy mogli zpowrotem przejśc do SetupActivity.
        startActivity(mainIntent); // .16  #13  # 15
        finish(); // .16  #13  # 15
    }
}


/*
DOKUMENTACJA TECHNICZNA
Będziemy zapisywać w bazie danych Firebase nazwę użytkownika, jego scroopowany obrazek, Imię i nazwisko, i kraj.
1.	Deklarujemy   zmienne prywatne nazwy użytkownika (UserName), buttonu, pól tekstowych, obrazka.
2.	Pobieramy referencje do elementów layoutu i przypisujemy do zadeklarowanych zmiennych.
3.	Określamy co ma się stać po kliknięciu przycisku „SaveInformationbutton” w tym celu ustawiamy  na nim setOnClickListener.
4.	W metodzie onClick metody View.OnClickListener();  deklarujemy  metodę SaveAccountSetupInformation(); która będzie zapisywała w bazie danych podane przez użytkownika dane.
5.	Definiujemy metodę private void  SaveAccountSetupInformation();
•	Tworzymy  nowe zmienne, do których zapisujemy pobrane od użytkownika dane z pół tekstowych  i obrazka ( String username = UserName.getText().toString(); )
•	Dodajemy walidiacje (jeśli po kliknięciu save któreś pole jest puste to prosimy o uzupełnienie).

6.	Deklarujemy zmienną „mAuth”  typu FirebaseAuth  (   private FirebaseAuth mAuth; ) ponieważ będziemy potrzebowali CurrentUserID, ponieważ zapisywane dane wprowadzone przez Użytkownika będą w bazie danych przypisane do tego użytkownika o danym ID.
7.	Deklarujemy zmienną „UsersRef” do referencji DatabaseReference.
8.	Tworzymy referencję do mAuth oraz UsersRef.
9.	Deklarujemy zmienną String  „currentUserID” w której będziemy przechowywać ID użytkownika.
10.	W zmiennej „currentUserID” zapisujemy unikalne ID użytkownika pobrane z Firebase Authentication:  mAuth.getCurrentUser().getUid();
11.	Po tych wszystkich krokach jesteśmy gotowi do zapisywania danych w drzewie w Firebase Database > Real Time Database. W tym celu tworzymy HashMap w ciele metody SaveAccountSetupInformation();
12.	Po utworzeniu HashMap zapisujemy w niej dane poprzez UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {}
13.	W pliku manifestu dodajemy zezwolenie na dostęp aplikacji do internetu:
    <uses-permission android:name="android.permission.INTERNET" />
14.	Po udanym wypełnieniu i zapisaniu danych wysyłamy  Użytkownika do MainActivity, robimy to metodą SendUserToMainActivity();
15.	Definiujemy ciało metody SendUserToMainActivity(); w której umieszczamy intencję przeniesienia Użytkownika do MainActivity.
16.	Deklarujemy zmienną „loadingBar” typu ProgresDialog.
17.	Tworzymy obiekt „loadingBar” typu ProgresDialog.
18.	Przed  HashMap dodajemy komunikat „loadingBar” który się pokaże zaraz po kliknięciu „save” czyli w trakcie wysyłania do Firebase Database wprowadzonych przez Użytkownika danych.
19.	Odwołujemy „loadingBar” po tym jak task jest succesfull lub unsuccesfull.


20.	W onComleate po udanym tasku i zamknieciu „loadingBar” wyświetlamy jeszcze Toast  „Your account is created succesfully”.
21.	W przeciwnym razie po nieudanym Tasku i  zamknieciu „loadingBar”  przechwytujemy wyjątek, potem konwertujemy go na String i  wyświetlamy w Toast w zmiennej message.
////////////////// można najpierw pokazać Toasta a potem schować loadingBara /////////////////
Cropowanie obrazka
22.	Na samym końcu onCreate();  dodajemy nasłuch ikony obrazka profilowego ProfileImage.setOnClickListener… Po jego kliknięciu skierujemy Użytkownika do plików z galerii jego telefonu komórkowego.
23.	Kierujemy do galerii poprzez intencję „galleryIntent” i określamy zarazem jaki rodzaj plików nas interesuje z pamięci telefonu.
24.	We need to pass an additional integer argument „GALLERY_PICK_REQUEST” to the startActivityForResult() method. The integer argument is a "request code" that identifies your request. When you receive the result Intent, the callback provides the same request code so that your app can properly identify the result and determine how to handle it.
25.	Po wybraniu obrazka z galerii dodajemy funkcję croopowania. Najpierw jednak musimy przechwycić wybrany z galerii plik.

*/

