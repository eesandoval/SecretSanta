package tech.anri.secretsanta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Date;

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
    public final static String IS_VOID = "is_void";
    public final static String POST_UPDATE_DATE = "post_update_date";

    public final static String IMAGES_TABLE = "Images";
    public final static String IMAGE_ID = "image_id";
    public final static String IMAGE = "image";
    public final static String IMAGE_UPDATE_DATE = "image_update_date";

    public final static String USERS_TABLE = "Users";
    public final static String USER_PASSWORD = "user_password";
    public final static String USER_IMAGE = "user_image";
    public final static String USER_EMAIL = "user_email";
    public final static String USER_NAME = "user_name";
    public final static String USER_UPDATE_DATE = "user_update_date";


    private final String SELECT_QUERY = "SELECT * FROM " + POSTS_TABLE + "A " + "" +
            "INNER JOIN " + IMAGES_TABLE + " B ON A.post_id = B.post_id " +
            "INNER JOIN " + USERS_TABLE + " C ON C.user_id = A.user_id";


    public DatabaseHelper(Context context) {
        openHelper = new DatabaseOpenHelper(context);
        database = openHelper.getWritableDatabase();
    }

    public void Dispose() {
        database.close();
    }

    public long createPost(int post_id, int user_id, String header, String body) {
        ContentValues values = new ContentValues();
        values.put(POST_ID, post_id);
        values.put(USER_ID, user_id);
        values.put(HEADER, header);
        values.put(BODY, body);
        values.put(IS_VOID, 0);
        values.put(POST_UPDATE_DATE, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        return database.insert(POSTS_TABLE, null, values);
    }

    public long createImage(int image_id, int post_id, byte[] image) {
        ContentValues values = new ContentValues();
        values.put(IMAGE_ID, image_id);
        values.put(POST_ID, post_id);
        values.put(IMAGE, image);
        values.put(IMAGE_UPDATE_DATE, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        return database.insert(IMAGES_TABLE, null, values);
    }

    public long createPost(int post_id, int user_id, String header, String body, String post_update_date, int is_void) {
        ContentValues values = new ContentValues();
        values.put(POST_ID, post_id);
        values.put(USER_ID, user_id);
        values.put(HEADER, header);
        values.put(BODY, body);
        values.put(IS_VOID, is_void);
        values.put(POST_UPDATE_DATE, post_update_date);
        return database.insert(POSTS_TABLE, null, values);
    }

    public long createImage(int image_id, int post_id, byte[] image, String image_update_date) {
        ContentValues values = new ContentValues();
        values.put(IMAGE_ID, image_id);
        values.put(POST_ID, post_id);
        values.put(IMAGE, image);
        values.put(IMAGE_UPDATE_DATE, image_update_date);
        return database.insert(IMAGES_TABLE, null, values);
    }

    public Cursor selectRecords() {
        Cursor mCursor = database.rawQuery(SELECT_QUERY, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor selectAllUsers() {
        Cursor mCursor = database.rawQuery("SELECT user_id, user_name, user_image, user_email, user_update_date FROM Users;", null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public void deleteUser(int userid) {
        database.delete(USERS_TABLE, "user_id=" + userid, null);
    }

    public void deletePost(int postid) {
        database.delete(POSTS_TABLE, "post_id=" + postid, null);
    }

    public void deleteImage(int imageid) {
        database.delete(IMAGES_TABLE, "image_id=" + imageid, null);
    }

    public Cursor selectAllPosts() {
        Cursor mCursor = database.rawQuery("SELECT post_id, user_id, header, body, post_update_date, is_void FROM Posts;", null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor selectAllImages() {
        Cursor mCursor = database.rawQuery("SELECT image_id, post_id, image, post_id, image_update_date FROM Images;", null);
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
        if (mCursor != null && mCursor.getCount() != 0 && !mCursor.isNull(0)) {
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
        values.put(USER_UPDATE_DATE, new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
        values.put(USER_PASSWORD, password);
        mCursor.close();
        database.insert(USERS_TABLE, null, values);
        return userid;
    }

    public int createUser(String userName, byte[] image, String emailAddress, int userId, String updateDate, String password) {
        ContentValues values = new ContentValues();
        values.put(USER_ID, userId);
        values.put(USER_NAME, userName);
        values.put(USER_IMAGE, image);
        values.put(USER_EMAIL, emailAddress);
        values.put(USER_UPDATE_DATE, updateDate);
        values.put(USER_PASSWORD, password); // Salted and hashed I hope!
        database.insert(USERS_TABLE, null, values);
        return userId;
    }

    public int updateUser(String userName, String password, String emailAddress, int userid, @Nullable Bitmap image) {
        ContentValues values = new ContentValues();
        byte[] imageBlob;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        values.put(USER_NAME, userName);
        values.put(USER_EMAIL, emailAddress);
        values.put(USER_UPDATE_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
        if (image != null) {
            image.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
            imageBlob = outputStream.toByteArray();
            values.put(USER_IMAGE, imageBlob);
        }
        values.put(USER_PASSWORD, password);
        database.update(USERS_TABLE, values, "user_id=" + userid, null);
        return userid;
    }

    public int updatePost(int postid, int userid, String header, String body, boolean isvoid) {
        ContentValues values = new ContentValues();
        byte[] imageBlob;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        values.put(POST_ID, postid);
        values.put(USER_ID, userid);
        values.put(HEADER, header);
        values.put(BODY, body);
        values.put(IS_VOID, isvoid);
        values.put(POST_UPDATE_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
        database.update(POSTS_TABLE, values, "post_id=" + postid, null);
        return postid;
    }

}
