package com.plorial.exoroplayer.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.plorial.exoroplayer.R;

import java.util.List;

/**
 * Created by plorial on 4/16/16.
 */
public class FileArrayAdapter extends ArrayAdapter<Item> {

    private Context c;
    private int id;
    private List<Item> items;

    public FileArrayAdapter(Context context, int textViewResourceId, List<Item> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }
    public Item getItem(int i)
    {
        return items.get(i);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, null);
        }
        final Item o = items.get(position);
        if (o != null) {
            TextView t1 = (TextView) v.findViewById(R.id.TextView01);
            TextView t2 = (TextView) v.findViewById(R.id.TextView02);
            TextView t3 = (TextView) v.findViewById(R.id.TextViewDate);

            ImageView imageCity = (ImageView) v.findViewById(R.id.fd_Icon1);
            switch (o.getImage()) {
                case "directory_icon":
                    imageCity.setImageResource(R.drawable.folder_outline);
                    break;
                case "file_icon":
                    imageCity.setImageResource(R.drawable.file_outline);
                    break;
                case "directory_up":
                    imageCity.setImageResource(R.drawable.arrow_up);
                    break;
                case "video":
                    imageCity.setImageResource(R.drawable.filmstrip);
                    break;
                case "subtitles":
                    imageCity.setImageResource(R.drawable.ic_subtitles_black_24dp);
                    break;
            }
            if(t1!=null)
                t1.setText(o.getName());
            if(t2!=null)
                t2.setText(o.getData());
            if(t3!=null)
                t3.setText(o.getDate());
        }
        return v;
    }
}
