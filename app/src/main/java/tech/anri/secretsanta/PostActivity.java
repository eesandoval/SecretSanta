package tech.anri.secretsanta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;

public class PostActivity extends AppCompatActivity {
    private int PICK_IMAGE_REQUEST = 1;
    private Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void onClickSelectFileButton(View view) {
        EditText header = (EditText)findViewById(R.id.post_header);
        EditText body = (EditText)findViewById(R.id.post_body);
        body.clearFocus();
        header.clearFocus();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        ((Button)findViewById(R.id.post_button)).setEnabled(true);
    }

    public void onClickPostButton(View view) {
        EditText header = (EditText)findViewById(R.id.post_header);
        EditText body = (EditText)findViewById(R.id.post_body);
        body.clearFocus();
        header.clearFocus();
        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("url", uri.toString());
        editor.putString("Header", header.getText().toString());
        editor.putString("Body", body.getText().toString());
        editor.commit();
        setResult(1);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                ImageView imageView = (ImageView) findViewById(R.id.selected_image);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        EditText header = (EditText)findViewById(R.id.post_header);
        EditText body = (EditText)findViewById(R.id.post_body);
        header.clearFocus();
        body.clearFocus();
        setResult(0);
        finish();
    }
}
