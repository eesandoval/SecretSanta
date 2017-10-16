package tech.anri.secretsanta;

/**
 * Created by Rayziken on 10/15/2017.
 */

public class MainListViewDataModel {
    private String header;
    private String body;
    private String image;

    public MainListViewDataModel(String header, String body, String image) {
        this.header = header;
        this.body = body;
        this.image = image;
    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    public String getImage() {
        return image;
    }
}
