package ovh.enterprise.socialnetwork;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;
import com.squareup.picasso.Picasso; // .28

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle; // .6 // v7.app
    private RecyclerView postList;
    private Toolbar mToolbar; // v7 support

    private FirebaseAuth mAuth; // .12
    private DatabaseReference UsersRef, PostRef;  // . 23, 27

     private ImageButton AddNewPostButton; // . 25


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance(); // .13
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users"); // . 23
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts"); // .27    # 23

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home"); // int res ID // ustawiamy nazwe toolbara

        AddNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button); // . 25


        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout); // 1. drawable_layout to jest nadrzędny layout w activity_main.xml
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close); // .7 // dodajemy menu 3 pasków do action bara// toogble needs two strings - one is the open second is the close, so we ned go to the values and create these strings
        drawerLayout.addDrawerListener(actionBarDrawerToggle); // .8
        actionBarDrawerToggle.syncState(); // .9
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);// .10
        navigationView = (NavigationView) findViewById(R.id.navigation_view); // 2.  navigation_view z activity_main.xml
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header); // 5. dodajemy tą jedną linią  navigation_header.xml do navigation_menu.xml tak że pokazują się teraz scalone

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() { // 4.  po wywołaniu tej metody mamy dostep do opcji z res/menu/navigation_mmenu.xml
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                // aby mieć dostęp do do opcji z res/menu/navigation_mmenu.xml musimy wywołać jeszcze te metodę:
                UserMenuSelector (item);
                return false;
            }
        });

      //  /*
        AddNewPostButton.setOnClickListener(new View.OnClickListener()  // . 25
        {
            @Override
            public void onClick(View v)
            {
                SendUserToPostActivity();
            }
        });
     //   */

        postList = (RecyclerView) findViewById(R.id.all_users_post_list); // . 26   # 23
        postList.setHasFixedSize(true); // . 26   # 23
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this); // . 26   # 23

        // we want to post new post on the top / . 26   # 23
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        DislayAllUsersPosts(); // . 26   # 23


    } ///////////////////////////////// onCreate koniec /////////////////////////////////////////

    private void DislayAllUsersPosts()  // . 27   # 23 first we muust implement     implementation 'com.firebaseui:firebase-ui-database:2.2.0'
    {
        FirebaseRecyclerAdapter<Posts, PostViewHolder> firebaseRecyclerAdapter = // T VH // . 27   # 23
        new FirebaseRecyclerAdapter<Posts, PostViewHolder>
                (
                        Posts.class,
                        R.layout.all_post_layout,
                        PostViewHolder.class,
                        PostRef // reference to the firebase database
                )
        {
            @Override
            protected void populateViewHolder(PostViewHolder viewHolder, Posts model, int position) // . 27
            {
                viewHolder.setFullname(model.getFullname());  // . 29
                viewHolder.setTime(model.getTime()); // . 29
                viewHolder.setDate(model.getDate()); // . 29
                viewHolder.setDescription(model.getDescription());  // . 29
                viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage()); // . 29
                viewHolder.setPostimage(getApplicationContext(), model.getPostimage()); // . 29
            }
        };

        postList.setAdapter(firebaseRecyclerAdapter); // .28
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder // . 27   # 23
    {
        View mView; // . 27   # 23

        public PostViewHolder(View itemView) // mView = itemView;
        {
            super(itemView);
            mView = itemView; // . 27   # 23
        }

        public void setFullname(String fullname) // .28  // method from Posts.class
        {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage) // .28  // method from Posts.class
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_imagee); // de.hdodenhof.circleimageview.CircleImageView from all_posts_layput.xml
            Picasso.with(ctx).load(profileimage).into(image);  // we can display that image using ppicasso
        }

        public void setTime (String time) // .28
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time); // R.id.post_time fraom all_post_layout.xml
            PostTime.setText("  " + time); // 3spaces befor display time and date
        }
        public void setDate(String date) // .28
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("   " + date);
        }

        public void setDescription(String description) // .28
        {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx, String postimage) // .28
        {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.with(ctx).load(postimage).into(PostImage);  // we can display that image using ppicasso
        }

    }

    private void SendUserToPostActivity()  // . 25
    {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        addNewPostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(addNewPostIntent);
        finish();
    }

    @Override // .14 onStart będzie automatycznie sprawdzała czy user jest zalogowany czy nie. torzymy ją poza onCreatem
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser(); // .15
        if (currentUser == null) // .16 sprawdzamy czy user jest zautentykowany jak nie to wysyłamy go do login activity
        {
            SendUserToLoginActivity(); // .17  jeśli wykryjemy że user nie jest zalogowany wysyłamy go do login activity naszą własną metodą
        }
        else // . 23 # 12, 2,3 m sprawdzmy czu uzytkownik istnieje w bazie danych firebase.
        {
            CheckUserExistance(); // . 23
        }
    }

    private void CheckUserExistance() // . 23 we chceck if user exist or not in firebase database // bardzo ważna walidacja
    {
        final String current_user_id =  mAuth.getCurrentUser().getUid(); // user who is currently online and who is currently trying to login
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.hasChild(current_user_id)) // if this user is authenticated but his record is not present in firbase database //  id (np.: fXPrF6LlrMSLm58rhm8hh5VCpQJ2) does not exist in firebase database // child is current_user_id (unice current user id - firebase > authentication > Identyfikator UID użytkownika > fXPrF6LlrMSLm58rhm8hh5VCpQJ2
                {
                    SendUserToSetupActivity();
                }
            }

            @Override // . 23 automatycznie dodana
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void SendUserToSetupActivity() // . 23
    {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish(); /// >>> a teraz przechodzimy do walidacji w RegisterActivity i LoginActivity
    }

    private void SendUserToLoginActivity() // .18
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class); // .19 wysyłamy usera do LoginActivity
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // .20 walidjacja
        startActivity(loginIntent); // .21
        finish(); // .22 >>>>>>>>> i przechodzimy do okodowania LoginActivity
    }

    // .11 dodajemy metode onOptionsItemSelected() (poza onCreate()) zeby hamburger reagował na kliknięcie i otwierał szufladę
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) // 3.
    {
        switch (item.getItemId())
        {
            case R.id.nav_post: // .25   res/menu/nav_post
                SendUserToPostActivity();
                break;

            case R.id.nav_profile:
                SendUserToSetupActivity();
                break;

            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_friends:
                Toast.makeText(this, "Friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_find_friends:
                Toast.makeText(this, "Find friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_messages:
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_Logout:
                mAuth.signOut(); // . 22 wylogowujemy sie z firebase # 10 3m
                SendUserToLoginActivity(); // . 22 po wylogowaniu wysyłamy użytkownika do strony logowania >>>> dalej przechodzimy do okodowania login activity
                break;
        }


    }


}
