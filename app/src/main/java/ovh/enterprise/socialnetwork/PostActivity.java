package ovh.enterprise.socialnetwork;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar; // .1   aby w linii setSupportActionBar(mToolbar); nie pokazywał się błąd musimy przerobić import  import android.widget.Toolbar; na import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar; // .1

    // .2 after we add image view, multipletext and button in activity.post.xml we must inicialize
    private ImageButton SelectPostImage; // .2
    private Button UptdatePostButton; // .2
    private EditText PostDescription; // .2

    private static final int Gallery_Pick = 1; // .3  # 19
    private Uri ImageUri; // .4  # 20

    private String Description; // .5  # 20
    private String saveCurrentDate, saveCurrentTime, postRandomName; // .5  # 20

    private StorageReference PostImagesReference; // .5  # 20



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        PostImagesReference = FirebaseStorage.getInstance().getReference(); //  // .5  # 20

        SelectPostImage = (ImageButton) findViewById(R.id.select_post_image); // .2
        UptdatePostButton = (Button) findViewById(R.id.uptade_post_button); // .2
        PostDescription = (EditText) findViewById(R.id.post_description) ; // .2

        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar); // .1
        setSupportActionBar(mToolbar); // .1

        // .1 add back arrow to toolbar  https://stackoverflow.com/questions/26651602/display-back-arrow-on-toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // .1
        getSupportActionBar().setDisplayShowHomeEnabled(true); // .1 dislay arrow to come back to MainActivity
        getSupportActionBar().setTitle("update post"); // .1 set title accion bar

        SelectPostImage.setOnClickListener(new View.OnClickListener() // .3 # 19 we declare what happens after click image (we want to open gallery from mobile phone)
        {
            @Override
            public void onClick(View v) // .3 # 19
            {
                OpenGallery();
            }
        });

        UptdatePostButton.setOnClickListener(new View.OnClickListener() // .5  # 20
        {
            @Override
            public void onClick(View v)
            {
                ValidatePostInfo();
            }
        });



    } /////////////////////////////////////// onCreate ////////////////////////////////////////////////////////////

    private void ValidatePostInfo()  // .5  # 20
    {
        Description = PostDescription.getText().toString();

        if(ImageUri == null)
        {
            Toast.makeText(this, "Please telect Image...", Toast.LENGTH_SHORT);
        }
        else if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please say something about your image...", Toast.LENGTH_SHORT);
        }
        else
        {
            StoringImageToFirebaseStorage(); //  when image is selected and description is wrote we can go to the storage it in firebase database
        }
    }

    private void StoringImageToFirebaseStorage() // .5  # 20 first we need the reference to firebase storage so we must create  private StorageReference PostImagesReference; and then  PostImagesReference = FirebaseStorage.getInstance().getReference();
    {
        Calendar calForDate = Calendar.getInstance(); // we take date from system
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
        saveCurrentDate = currentDate.format(calForDate.getTime()); // w zmiennej saveCurrentDate zapisujemy datę w formacie String

        Calendar calForTome= Calendar.getInstance(); // we take date from system
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForDate.getTime()); // w zmiennej saveCurrentDate zapisujemy datę w formacie String

        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filePath = PostImagesReference.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg" ); // in folder Post Images we storage image and the name of these image is real name ( child(ImageUri.getLastPathSegment() +  postRandomName + ".jpg"  ) ////we create folder in firebase storage and we called it "Post Images" // in .child() we give upload images random name. Because uploading images by users can have the same name - for that we will get first real name of image and them we +add something. we get name and add time becouse time and name of image will be different for every uploaded images
        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() // .5  # 20
        {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) // .5  # 20
            {
                if (task.isSuccessful()) // .5  # 20
                {
                    Toast.makeText(PostActivity.this, "image uploaded succesfully to Storage ", Toast.LENGTH_SHORT );
                }
                else // .5  # 20
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT );
                }
            }
        });
    }


    private void OpenGallery() // .3 # 19
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*"); // we must tell with type of filel we want to pick from the mobile phone
        startActivityForResult(galleryIntent, Gallery_Pick); // we must create final static variable of Gallery_Pick
    }

    @Override // . 4  # 20
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Gallery_Pick && resultCode==RESULT_OK && data != null)
        {
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri); // ImageUri is stored in line above ImageUri = data.getData();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)  // .1  po kliknięciu strałki na pasku akcji wracamy do MainActivity
    {
        int id = item.getItemId();
        if(id == android.R.id.home)
        {
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);  // .1
    }

    private void SendUserToMainActivity()  // .1
    {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        mainIntent.addFlags((Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        startActivity(mainIntent);
        // finish();

    }

} /////////////////// onCreate //////////////////////////
