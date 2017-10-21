package tech.anri.secretsanta;

import android.content.Context;
import android.database.Cursor;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by Rayziken on 10/19/2017.
 */

public class RemoteDatabaseHelper {
    private DatabaseHelper dbHelper;
    private static final String REMOTE_URL = "http://172.88.7.148:81/PHPServices/";
    private static final String CREATE_UPDATE_USER = "create_update_user.php";
    private static final String CREATE_UPDATE_POST = "create_update_post.php";
    private static final String CREATE_UPDATE_IMAGE = "create_update_image.php";
    private Context context;

    RemoteDatabaseHelper(Context context) {
        this.context = context;
    }

    public boolean UpdateRemoteDatabase() {
        dbHelper = new DatabaseHelper(context);
        JSONObject jsonObject = GetRemoteJSON("refresh.php");
        JSONObject jsonUsers;
        JSONObject jsonPosts;
        JSONObject jsonImages;
        try {
            jsonUsers = jsonObject.getJSONObject("users");
            jsonPosts = jsonObject.getJSONObject("posts");
            jsonImages = jsonObject.getJSONObject("images");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        // Users
        ArrayList<Integer> updateUserIds = null;
        if (jsonUsers != null ) {
            updateUserIds = Compare(jsonUsers, false, "user_ids", "user_update_dates");
        }
        if (updateUserIds != null && updateUserIds.size() > 0) {
            UpdateRemoteUsers(updateUserIds);
        }
        // Posts
        ArrayList<Integer> updatePostIds = null;
        if (jsonPosts != null) {
            updatePostIds = Compare(jsonPosts, false, "post_ids", "post_update_dates");
        }
        if (updatePostIds != null && updatePostIds.size() > 0) {
            UpdateRemotePosts(updatePostIds);
        }
        // Images
        ArrayList<Integer> updateImageIds = null;
        if (jsonPosts != null) {
            updateImageIds = Compare(jsonImages, false, "image_ids", "image_update_dates");
        }
        if (updateImageIds != null && updateImageIds.size() > 0) {
            UpdateRemoteImages(updateImageIds);
        }
        dbHelper.Dispose();
        return true;
    }

    public boolean UpdateLocalDatabase() {
        dbHelper = new DatabaseHelper(context);
        JSONObject jsonObject = GetRemoteJSON("refresh.php");
        JSONObject jsonUsers;
        JSONObject jsonPosts;
        JSONObject jsonImages;
        try {
            jsonUsers = jsonObject.getJSONObject("users");
            jsonPosts = jsonObject.getJSONObject("posts");
            jsonImages = jsonObject.getJSONObject("images");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        // Users
        ArrayList<Integer> updateUserIds = null;
        if (jsonUsers != null ) {
            updateUserIds = Compare(jsonUsers, true, "user_ids", "user_update_dates");
        }
        if (updateUserIds != null && updateUserIds.size() > 0) {
            UpdateUsers(updateUserIds);
        }
        // Posts
        ArrayList<Integer> updatePostIds = null;
        if (jsonPosts != null) {
            updatePostIds = Compare(jsonPosts, true, "post_ids", "post_update_dates");
        }
        if (updatePostIds != null && updatePostIds.size() > 0) {
            UpdatePosts(updatePostIds);
        }
        // Images
        ArrayList<Integer> updateImageIds = null;
        if (jsonPosts != null) {
            updateImageIds = Compare(jsonImages, true, "image_ids", "image_update_dates");
        }
        if (updateImageIds != null && updateImageIds.size() > 0) {
            UpdateImages(updateImageIds);
        }
        dbHelper.Dispose();
        return true;
    }

    private JSONObject GetRemoteJSON(String trailing) {
        JSONObject result = null;
        String webServiceResult = "";
        try {
            URL url = new URL(REMOTE_URL + trailing);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                webServiceResult += inputLine;
            }
            bufferedReader.close();
            result = new JSONObject(webServiceResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private ArrayList<Integer> Compare(JSONObject jsonObject, boolean updatingSelf, String id, String dates) {
        try {
            ArrayList<Integer> result = new ArrayList<>();
            JSONArray userJSONArray = jsonObject.getJSONArray(id);
            JSONArray updatesJSONArray = jsonObject.getJSONArray(dates);
            HashMap<Integer, String> remoteUpdates = new HashMap<>();
            HashMap<Integer, String> localUpdates = new HashMap<>();
            ArrayList<Integer> remoteUsers = new ArrayList<>();
            ArrayList<Integer> localUsers = new ArrayList<>();
            Cursor mCursor;
            switch(id) {
                case "user_ids":
                    mCursor = dbHelper.selectAllUsers();
                    break;
                case "post_ids":
                    mCursor = dbHelper.selectAllPosts();
                    break;
                case "image_ids":
                    mCursor = dbHelper.selectAllImages();
                    break;
                default:
                    return null;
            }
            if (mCursor != null && mCursor.getCount() > 0) {
                do {
                    localUsers.add(mCursor.getInt(0));
                    localUpdates.put(mCursor.getInt(0), mCursor.getString(4));
                } while (mCursor.moveToNext());
                mCursor.close();
            }
            for (int i = 0; i < userJSONArray.length(); ++i) {
                remoteUsers.add(Integer.parseInt(userJSONArray.getString(i)));
                remoteUpdates.put(Integer.parseInt(userJSONArray.getString(i)), updatesJSONArray.getString(i));
            }
            if (updatingSelf) {
                // Assume remote is true if updatingSelf (bigger list)
                remoteUsers.removeAll(localUsers);
                if (remoteUsers.size() > 0) {
                    result = remoteUsers;
                }
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
                for (Map.Entry<Integer, String> entry : localUpdates.entrySet())  {
                    if (remoteUpdates.containsKey(entry.getKey())) {
                        Date localDate = format.parse(localUpdates.get(entry.getKey()));
                        Date remoteDate = format.parse(remoteUpdates.get(entry.getKey()));
                        if (remoteDate.after(localDate)) {
                            result.add(entry.getKey());
                        }
                    }
                }
            } else {
                localUsers.removeAll(remoteUsers);
                if (localUsers.size() > 0) {
                    result = localUsers;
                }
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
                for (Map.Entry<Integer, String> entry : remoteUpdates.entrySet())  {
                    if (localUpdates.containsKey(entry.getKey())) {
                        Date localDate = format.parse(localUpdates.get(entry.getKey()));
                        Date remoteDate = format.parse(remoteUpdates.get(entry.getKey()));
                        if (localDate.after(remoteDate)) {
                            result.add(entry.getKey());
                        }
                    }
                }
            }
            Set temp = new LinkedHashSet(result);
            result.clear();
            result.addAll(temp);
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (ParseException e) {
            int a =1;
            e.printStackTrace();
            return null;
        }
    }

    private void UpdateUsers(ArrayList<Integer> UserIDs) {
        try {
            JSONObject tempJSON;
            for (int i = 0; i < UserIDs.size(); ++i) {
                dbHelper.deleteUser(UserIDs.get(i));
                tempJSON = GetRemoteJSON("read.php?&post_id=-1&image_id=-1&user_id=" + UserIDs.get(i));
                tempJSON = tempJSON.getJSONObject("users");
                dbHelper.createUser(tempJSON.getString("user_name"),
                        Base64ToByte(tempJSON.getString("user_image")),
                        tempJSON.getString("user_email"),
                        Integer.parseInt(tempJSON.getString("user_id")),
                        tempJSON.getString("user_update_date"),
                        tempJSON.getString("user_password"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void UpdatePosts(ArrayList<Integer> PostIDs) {
        try {
            JSONObject tempJSON;
            for (int i = 0; i < PostIDs.size(); ++i) {
                dbHelper.deletePost(PostIDs.get(i));
                tempJSON = GetRemoteJSON("read.php?&post_id="+PostIDs.get(i)+"&image_id=-1&user_id=-1");
                tempJSON = tempJSON.getJSONObject("posts");
                dbHelper.createPost(Integer.parseInt(tempJSON.getString("post_id")),
                        Integer.parseInt(tempJSON.getString("user_id")),
                        tempJSON.getString("header"),
                        tempJSON.getString("body"),
                        tempJSON.getString("post_update_date"),
                        Integer.parseInt(tempJSON.getString("voided")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void UpdateImages(ArrayList<Integer> ImageIDs) {
        try {
            JSONObject tempJSON;
            for (int i = 0; i < ImageIDs.size(); ++i) {
                dbHelper.deletePost(ImageIDs.get(i));
                tempJSON = GetRemoteJSON("read.php?&post_id=-1&image_id=" + ImageIDs.get(i) + "&user_id=-1");
                tempJSON = tempJSON.getJSONObject("images");
                dbHelper.createImage(Integer.parseInt(tempJSON.getString("image_id")),
                        Integer.parseInt(tempJSON.getString("post_id")),
                        Base64ToByte(tempJSON.getString("image_guid")),
                        tempJSON.getString("image_update_date"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void UpdateRemoteUsers(ArrayList<Integer> UserIDs) {
        Cursor mCursor;
        int user_id;
        String user_name;
        String user_image;
        String user_email;
        String user_update_date;
        String user_password;
        String message;
        try {
            for (int i = 0; i < UserIDs.size(); ++i) {
                mCursor = dbHelper.selectRecordsRaw("SELECT user_id, user_name, user_image, user_email, user_update_date, user_password FROM Users WHERE user_id = " + UserIDs.get(i));
                if (mCursor != null) {
                    user_id = mCursor.getInt(0);
                    user_name = mCursor.getString(1);
                    user_image = mCursor.getString(2);
                    user_email = mCursor.getString(3);
                    user_update_date = mCursor.getString(4);
                    user_password = mCursor.getString(5);
                    mCursor.close();
                    message = "?user_id=" + user_id + "&user_name=" + user_name +
                            "&user_image=" + user_image + "&user_email=" +user_email +
                            "&user_update_date=" + user_update_date + "&user_password=" +
                            user_password + "";
                    URL url = new URL(REMOTE_URL + CREATE_UPDATE_USER + message);
                    URLConnection urlConnection = url.openConnection();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    bufferedReader.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdateRemotePosts(ArrayList<Integer> PostIDs) {
        Cursor mCursor;
        int post_id;
        int user_id;
        String header;
        String body;
        String post_update_date;
        int voided;
        String message;
        try {
            for (int i = 0; i < PostIDs.size(); ++i) {
                mCursor = dbHelper.selectRecordsRaw("SELECT post_id, user_id, header, body, post_update_date, is_void FROM Posts WHERE post_id = " + PostIDs.get(i));
                if (mCursor != null) {
                    post_id = mCursor.getInt(0);
                    user_id = mCursor.getInt(1);
                    header = mCursor.getString(2);
                    body = mCursor.getString(3);
                    post_update_date = mCursor.getString(4);
                    voided = mCursor.getInt(5);
                    mCursor.close();
                    message = "?post_id=" + post_id + "&user_id=" + user_id +
                            "&header=" + header + "&body=" +body +
                            "&post_update_date=" + post_update_date + "&voided=" +
                            voided;
                    URL url = new URL(REMOTE_URL + CREATE_UPDATE_POST + message);
                    URLConnection urlConnection = url.openConnection();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    bufferedReader.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/*
    // Building Parameters
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    // getting JSON string from URL
    JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);

    // Check your log cat for JSON reponse
            Log.d("All Products: ", json.toString());

            try {
        // Checking for SUCCESS TAG
        int success = json.getInt(TAG_SUCCESS);

        if (success == 1) {
            // products found
            // Getting Array of Products
            products = json.getJSONArray(TAG_PRODUCTS);

            // looping through All Products
            for (int i = 0; i < products.length(); i++) {
                JSONObject c = products.getJSONObject(i);

                // Storing each json item in variable
                String id = c.getString(TAG_PID);
                String name = c.getString(TAG_NAME);

                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();

                // adding each child node to HashMap key => value
                map.put(TAG_PID, id);
                map.put(TAG_NAME, name);

                // adding HashList to ArrayList
                productsList.add(map);
            }
        } else {
            // no products found
            // Launch Add New product Activity
            Intent i = new Intent(getApplicationContext(),
                    NewProductActivity.class);
            // Closing all previous activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    } catch (JSONException e) {
        e.printStackTrace();
    }

            return null;
}
*/
    private void UpdateRemoteImages(ArrayList<Integer> ImageIDs) {
        Cursor mCursor;
        int image_id;
        int post_id;
        String image_guid;
        byte[] image;
        String image_update_date;
        String message;
        URL url;
        byte[] postData;
        int postDataLength;
        try {
            url = new URL(REMOTE_URL + CREATE_UPDATE_IMAGE);
            for (int i = 0; i < ImageIDs.size(); ++i) {
                mCursor = dbHelper.selectRecordsRaw("SELECT image_id, post_id, image, image_update_date FROM Images WHERE image_id = " + ImageIDs.get(i));
                if (mCursor != null) {
                    image_id = mCursor.getInt(0);
                    post_id = mCursor.getInt(1);
                    image = mCursor.getBlob(2);
                    image_guid = Base64.encodeToString(image, Base64.DEFAULT);
                    image_update_date = mCursor.getString(3);
                    mCursor.close();
                    message = "image_id=" + image_id + "&post_id=" + post_id +"&image_update_date=" +image_update_date + "&image_guid=" + image_guid ;

                    postData = message.getBytes(StandardCharsets.UTF_8);
                    postDataLength = message.length();
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setDoOutput(true);
                    conn.setInstanceFollowRedirects(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                    conn.setUseCaches(false);
                    conn.connect();
                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes(message);
                    wr.flush();
                    wr.close();
                    InputStreamReader dataInputStream = new InputStreamReader(conn.getInputStream());
                    BufferedReader br = new BufferedReader(dataInputStream);
                    String json_response = "";
                    String text = "";
                    while ((text = br.readLine()) != null) {
                        json_response += text;
                    }
                    System.out.println(json_response);
                    System.out.println(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] Base64ToByte(String base64) {
        if (base64 == null)
            return null;
        return Base64.decode(base64, Base64.DEFAULT);
    }
}
