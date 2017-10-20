package tech.anri.secretsanta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private String username;
    private String password;
    private String email;
    private int userid;
    private final static int POST_ACTIVITY_CALL = 1;
    private final static int UPDATE_USER_ACTIVITY_CALL = 2;
    ArrayList<Post> posts = new ArrayList<Post>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        if (!dbHelper.emptyDatabase()) {
            updateFeed();
        }
        if (sharedPreferences.getBoolean("DBUpdated", false)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
            startActivity(intent);
            finish();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFabClick();

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        updateNavDrawer();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
       }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // TODO: Update profile
            Intent intent = new Intent(getApplicationContext(), UpdateUserActivity.class);
            startActivityForResult(intent, UPDATE_USER_ACTIVITY_CALL);

        } else if (id == R.id.nav_logout) {
            SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("email");
            editor.remove("password");
            editor.commit();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void updateFeed() {
        int maxPostId = 1;
        ArrayList<MainListViewDataModel> l = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        Cursor mCursor = dbHelper.selectRecordsRaw("SELECT MAX(post_id) FROM Posts");
        if (mCursor != null) {
            maxPostId = mCursor.getInt(0);
        }
        mCursor.close();
        posts = new ArrayList<>();
        for (int i = 0; i < maxPostId; ++i) {
            Post p = new Post(i + 1, getApplicationContext());
            if (!p.Voided) {
                posts.add(p);
                l.add(new MainListViewDataModel(p.Header, p.Body, p.Images.get(0), p.UserName, p.UserImage));
            }
        }
        MainListViewAdapter customAdapter = new MainListViewAdapter(this, R.layout.layout_list_view_main, l);
        ListView mainListView = (ListView) findViewById(R.id.main_list_view);
        mainListView.setAdapter(customAdapter);
    }

    private void updateNavDrawer() {
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        SharedPreferences sharedPreferences = this.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        this.email = sharedPreferences.getString("email", null);
        this.password = sharedPreferences.getString("password", null);
        this.username = sharedPreferences.getString("username", null);
        this.userid = sharedPreferences.getInt("userid", 0);
        Bitmap userImage = dbHelper.selectImage("SELECT user_image FROM Users WHERE user_id = " + this.userid);
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderEmail = (TextView)headerView.findViewById(R.id.nav_header_email);
        TextView navHeaderUsername = (TextView)headerView.findViewById(R.id.nav_header_username);
        navHeaderUsername.setText(this.username);
        navHeaderEmail.setText(this.email);
        ImageView navHeaderImage = (ImageView)headerView.findViewById(R.id.nav_header_user_image);
        if (!(userImage == null)) {
            navHeaderImage.setImageBitmap(userImage);
        }
    }

    private void onFabClick() {
        Intent intent = new Intent(getApplicationContext(), PostActivity.class);
        startActivityForResult(intent, POST_ACTIVITY_CALL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case POST_ACTIVITY_CALL:
                if (resultCode != 0) {
                    SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
                    Uri uri = Uri.parse(sharedPreferences.getString("url", null));
                    String header = sharedPreferences.getString("Header", null);
                    String body = sharedPreferences.getString("Body", null);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("uri");
                    editor.remove("Header");
                    editor.remove("Body");
                    editor.commit();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        Post p = new Post(userid, header, body, bitmap, getApplicationContext());
                        posts.add(p);
                        updateFeed();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            break;
            case UPDATE_USER_ACTIVITY_CALL:
                updateFeed();
                updateNavDrawer();
            break;
        }
    }
}
