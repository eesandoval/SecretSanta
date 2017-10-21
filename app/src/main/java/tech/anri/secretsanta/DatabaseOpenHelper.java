package tech.anri.secretsanta;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Rayziken on 10/17/2017.
 */

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "SecretSanta";
    private static final int DATABASE_VERSION = 4;
    private static final String[] DATABASE_CREATE = new String[] {
            "CREATE TABLE Posts(post_id integer primary key, user_id integer not null, header text, body text, is_void integer, post_update_date text);",
            "CREATE TABLE Images(image_id integer primary key, post_id integer, image blob, image_update_date text);",
            "CREATE TABLE Users(user_id integer primary key, user_name text, user_image blob, user_email text, user_update_date text, user_password text);"};
    private static final String[] DATABASE_DESTROY = new String[] {
            "DROP TABLE IF EXISTS Posts;",
            "DROP TABLE IF EXISTS Images;",
            "DROP TABLE IF EXISTS Users;"};

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String subQuery : DATABASE_CREATE) {
            db.execSQL(subQuery);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(DatabaseOpenHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + " and truncating data");
        for (String subQuery : DATABASE_DESTROY) {
            db.execSQL(subQuery);
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putBoolean("DBUpdated", true);
        editor.commit();
        onCreate(db);
    }
}
