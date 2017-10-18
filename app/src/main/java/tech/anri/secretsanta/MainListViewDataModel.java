package tech.anri.secretsanta;

import android.graphics.Bitmap;

/**
 * Created by Rayziken on 10/15/2017.
 */

public class MainListViewDataModel {
    private String header;
    private String body;
    private Bitmap image;
    private String username;
    private Bitmap userimage;

    public MainListViewDataModel(String header, String body, Bitmap image, String username, Bitmap userimage) {
        this.header = header;
        this.body = body;
        this.image = image;
        this.username = username;
        this.userimage = userimage;
    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getUsername() {
        return username;
    }

    public Bitmap getUserimage() {
        return userimage;
    }
}
