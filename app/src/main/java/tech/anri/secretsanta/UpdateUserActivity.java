package tech.anri.secretsanta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;

public class UpdateUserActivity extends AppCompatActivity {
    private int PICK_IMAGE_REQUEST = 1;
    private Uri uri;
    private int UserID;
    private boolean ImageUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        this.UserID = sharedPreferences.getInt("userid", 0);
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        Bitmap userImage = dbHelper.selectImage("SELECT user_image FROM Users WHERE user_id = " + this.UserID);
        ImageView currentImage = (ImageView)findViewById(R.id.updated_image);
        if (userImage != null) {
            currentImage.setImageBitmap(userImage);
        } else {
            currentImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_default_image));
        }
        dbHelper.Dispose();
    }

    public void onClickConfirmUpdateButton(View view) {
        EditText newUsername = (EditText)findViewById(R.id.updated_username);
        EditText newPassword = (EditText)findViewById(R.id.updated_password);
        EditText newEmail = (EditText)findViewById(R.id.updated_email);
        ImageView newImage = (ImageView)findViewById(R.id.updated_image);
        newUsername.clearFocus();
        newPassword.clearFocus();
        newEmail.clearFocus();
        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String updatedUsername = sharedPreferences.getString("username", null);
        String updatedPassword = sharedPreferences.getString("password", null);
        String updatedEmail = sharedPreferences.getString("email", null);
        Bitmap updatedImage = null;

        if (!(newUsername.getText().toString().matches(""))) {
            updatedUsername = newUsername.getText().toString();
        }
        if (!(newPassword.getText().toString().matches(""))) {
            updatedPassword = newPassword.getText().toString();
        }
        if (!(newEmail.getText().toString().matches(""))) {
            updatedEmail = newEmail.getText().toString();
        }
        if (ImageUpdated) {
            updatedImage = ((BitmapDrawable)newImage.getDrawable()).getBitmap();
        }

        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        dbHelper.updateUser(updatedUsername, updatedPassword, updatedEmail, UserID, updatedImage);

        editor.putString("username", updatedUsername);
        editor.putString("password", updatedPassword);
        editor.putString("email", updatedEmail);
        editor.apply();
        dbHelper.Dispose();
        finish();
    }

    public void onClickUpdateImageButton(View view) {
        EditText newUsername = (EditText)findViewById(R.id.updated_username);
        EditText newPassword = (EditText)findViewById(R.id.updated_password);
        EditText newEmail = (EditText)findViewById(R.id.updated_email);
        newUsername.clearFocus();
        newPassword.clearFocus();
        newEmail.clearFocus();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    public void onClickCancelUpdateButton(View view) {
        finish();
    }

    @Override
    public void onBackPressed() {
        EditText newUsername = (EditText)findViewById(R.id.updated_username);
        EditText newPassword = (EditText)findViewById(R.id.updated_password);
        EditText newEmail = (EditText)findViewById(R.id.updated_email);
        newUsername.clearFocus();
        newPassword.clearFocus();
        newEmail.clearFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                ImageView imageView = (ImageView) findViewById(R.id.updated_image);
                imageView.setImageBitmap(bitmap);
                ImageUpdated = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
