package tech.anri.secretsanta;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Rayziken on 10/15/2017.
 */

public class MainListViewAdapter extends ArrayAdapter {
    public MainListViewAdapter(Context context, int resource) {
        super(context, resource);
    }

    public MainListViewAdapter(Context context, int resource, List<MainListViewDataModel> items) {
        super(context, resource, items);
    }

    @Override
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.layout_list_view_main, null);
        }

        MainListViewDataModel p = (MainListViewDataModel) getItem(position);

        if (p != null) {
            TextView header = (TextView) v.findViewById(R.id.layout_list_view_header_text_view);
            TextView body = (TextView) v.findViewById(R.id.layout_list_view_body_text_view);
            ImageView image = (ImageView) v.findViewById(R.id.layout_list_view_image_view);
            if (header != null) {
                header.setText(p.getHeader());
            }
            if (body != null) {
                body.setText(p.getBody());
            }
            if (image != null) {
                String uri = "@drawable/" + p.getImage();
                int imageResource = getContext().getResources().getIdentifier(uri, null, this.getContext().getPackageName());
                Drawable res = getContext().getResources().getDrawable(imageResource, null);
                image.setImageDrawable(res);
            }
        }
        return v;
    }
}
