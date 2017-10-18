package tech.anri.secretsanta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;

/**
 * Created by Rayziken on 10/17/2017.
 */

public class DatabaseHelper {
    private DatabaseOpenHelper openHelper;
    private SQLiteDatabase database;

    public final static String POSTS_TABLE = "Posts";
    public final static String POST_ID = "post_id";
    public final static String USER_ID = "user_id";
    public final static String HEADER = "header";
    public final static String BODY = "body";

    public final static String IMAGES_TABLE = "Images";
    public final static String IMAGE_ID = "image_id";
    public final static String IMAGE = "image";

    public final static String USERS_TABLE = "Users";
    public final static String USER_IMAGE = "user_image";
    public final static String USER_EMAIL = "user_email";
    public final static String USER_NAME = "user_name";


    private final String SELECT_QUERY = "SELECT * FROM " + POSTS_TABLE + "A " + "" +
                                        "INNER JOIN " + IMAGES_TABLE + " B ON A.post_id = B.post_id " +
                                        "INNER JOIN " + USERS_TABLE + " C ON C.user_id = A.user_id";


    public DatabaseHelper(Context context) {
        openHelper = new DatabaseOpenHelper(context);
        database = openHelper.getWritableDatabase();
    }

    public long createPost(int post_id, int user_id, String header, String body) {
        ContentValues values = new ContentValues();
        values.put(POST_ID, post_id);
        values.put(USER_ID, user_id);
        values.put(HEADER, header);
        values.put(BODY, body);
        return database.insert(POSTS_TABLE, null, values);
    }

    public long createImage(int image_id, int post_id, byte[] image) {
        ContentValues values = new ContentValues();
        values.put(IMAGE_ID, image_id);
        values.put(POST_ID, post_id);
        values.put(IMAGE, image);
        return database.insert(IMAGES_TABLE, null, values);
    }

    public Cursor selectRecords() {
        Cursor mCursor = database.rawQuery(SELECT_QUERY, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Bitmap selectImage(String rawquery) {
        Bitmap result = null;
        Cursor mCursor = database.rawQuery(rawquery, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        if (!mCursor.isNull(0)) {
            result = BitmapFactory.decodeByteArray(mCursor.getBlob(0), 0, mCursor.getBlob(0).length);
        }
        mCursor.close();
        return result;
    }

    public Cursor selectRecordsRaw(String rawquery) {
        Cursor mCursor = database.rawQuery(rawquery, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean emptyDatabase() {
        Cursor mCursor = database.rawQuery("SELECT COUNT(*) FROM Posts", null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor.getInt(0) == 0;
    }

    public int createUser(String userName, String password, String emailAddress) {
        ContentValues values = new ContentValues();
        Cursor mCursor = database.rawQuery("SELECT MAX(user_id) FROM Users", null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        mCursor.getInt(0);
        int userid = mCursor.getInt(0) + 1;
        values.put(USER_ID, userid);
        values.put(USER_NAME, userName);
        values.put(USER_EMAIL, emailAddress);
        mCursor.close();
        database.insert(USERS_TABLE, null, values);
        return userid;
    }

    public int updateUser(String userName, String password, String emailAddress, int userid, @Nullable Bitmap image) {
        ContentValues values = new ContentValues();
        byte[] imageBlob;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        values.put(USER_NAME, userName);
        values.put(USER_EMAIL, emailAddress);
        if (image != null) {
            image.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            imageBlob = outputStream.toByteArray();
            values.put(USER_IMAGE, imageBlob);
        }
        database.update(USERS_TABLE, values, "user_id=" + userid, null);
        return userid;
    }

}
