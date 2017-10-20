package tech.anri.secretsanta;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static android.R.id.list;

/**
 * Created by Rayziken on 10/15/2017.
 */

public class MainListViewAdapter extends ArrayAdapter {
    private List<MainListViewDataModel> list;
    public MainListViewAdapter(Context context, int resource) {
        super(context, resource);
    }

    public MainListViewAdapter(Context context, int resource, List<MainListViewDataModel> items) {
        super(context, resource, items);
        list = items;
    }

    @Override
    public @NonNull View getView(final int position, View convertView, @NonNull ViewGroup parent) {
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
            ImageView userimage = (ImageView) v.findViewById(R.id.layout_list_view_user_image);
            TextView username = (TextView) v.findViewById(R.id.layout_list_view_username);
            if (header != null) {
                header.setText(p.getHeader());
            }
            if (body != null) {
                body.setText(p.getBody());
            }
            if (image != null) {
                image.setImageBitmap(p.getImage());
            }
            if (userimage != null) {
                userimage.setImageBitmap(p.getUserimage());
            }
            if (username != null) {
                username.setText(p.getUsername());
            }
        }

        Button deleteButton = (Button)v.findViewById(R.id.layout_list_view_delete);
        Button editButton = (Button)v.findViewById(R.id.layout_list_view_edit);

        deleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //do something
                remove(getItem(position));
                notifyDataSetChanged();
            }
        });
        editButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //do something
                notifyDataSetChanged();
            }
        });

        return v;
    }
}
