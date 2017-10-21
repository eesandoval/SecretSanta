package tech.anri.secretsanta;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by Rayziken on 10/17/2017.
 */

public class Post {
    public int PostId;
    public int UserId;
    public String Header;
    public String Body;
    public ArrayList<Bitmap> Images = new ArrayList<>();
    public String UserName;
    public Bitmap UserImage;
    public String UserEmail;
    public boolean Voided = false;
    private Context context;
    DatabaseHelper dbHelper;

    public Post(int userId, String header, String body, Bitmap image, Context context) {
        UserId = userId;
        Header = header;
        Body = body;
        this.context = context;
        ReadUser();
        WritePost();
        WriteImage(image);
    }

    public Post(int postId, Context context) {
        PostId = postId;
        this.context = context;
        ReadPost();
        ReadUser();
        ReadImages();
    }

    public void ReadUser() {
        dbHelper = new DatabaseHelper(context);
        Cursor mCursor = dbHelper.selectRecordsRaw("SELECT user_name, user_image, user_email FROM Users WHERE user_id = " + UserId);
        Cursor c = dbHelper.selectRecordsRaw("SELECT * FROM Users");
        int id = c.getInt(0);
        if (mCursor != null) {
            UserName = mCursor.getString(0);
            if (!mCursor.isNull(1)) {
                UserImage = BitmapFactory.decodeByteArray(mCursor.getBlob(1), 0, mCursor.getBlob(1).length);
            } else {
                // Set default
                UserImage = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_default_image);
            }
            UserEmail = mCursor.getString(2);
        }
        mCursor.close();
        dbHelper.Dispose();
    }

    public void ReadPost() {
        dbHelper = new DatabaseHelper(context);
        Cursor mCursor = dbHelper.selectRecordsRaw("SELECT user_id, header, body, is_void FROM Posts WHERE post_id = " + PostId);
        if (mCursor != null) {
            UserId = mCursor.getInt(0);
            Header = mCursor.getString(1);
            Body = mCursor.getString(2);
            Voided = mCursor.getInt(3) != 0;
        }
        mCursor.close();
        dbHelper.Dispose();
    }

    public void WritePost() {
        dbHelper = new DatabaseHelper(context);
        int lastRecord = 0;

        Cursor mCursor = dbHelper.selectRecordsRaw("SELECT MAX(post_id) FROM Posts");
        if (mCursor != null) {
            lastRecord = mCursor.getInt(0);
        }
        mCursor.close();
        PostId = lastRecord + 1;

        dbHelper.createPost(PostId, UserId, Header, Body);
        dbHelper.Dispose();
    }

    public void ReadImages() {
        dbHelper = new DatabaseHelper(context);
        Images = new ArrayList<>();
        Cursor mCursor = dbHelper.selectRecordsRaw("SELECT image FROM Images WHERE post_id = " + PostId);
        if (mCursor != null && mCursor.getCount() > 0) {
            do {
                Images.add(BitmapFactory.decodeByteArray(mCursor.getBlob(0), 0, mCursor.getBlob(0).length));
            } while (mCursor.moveToNext());
        }
        mCursor.close();
        dbHelper.Dispose();
    }

    public void WriteImage(Bitmap image) {
        dbHelper = new DatabaseHelper(context);
        int lastRecord = 0;
        byte imageBlob[];
        Images.add(image);

        Cursor mCursor = dbHelper.selectRecordsRaw("SELECT MAX(image_id) FROM Images");
        if (mCursor != null) {
            lastRecord = mCursor.getInt(0);
        }
        mCursor.close();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        imageBlob = outputStream.toByteArray();

        dbHelper.createImage(lastRecord + 1, PostId, imageBlob);
        dbHelper.Dispose();
    }

    public void VoidPost() {
        dbHelper = new DatabaseHelper(context);
        this.Voided = true;
        dbHelper.updatePost(this.PostId, this.UserId, this.Header, this.Body, true);
        dbHelper.Dispose();
    }

    public Bitmap getImage(int position) {
        if (position >= this.Images.size())
            return null;
        return this.Images.get(position);
    }
}
