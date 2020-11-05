package com.example.egautofill;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class CustomListAdapter extends ArrayAdapter {
    private Context context;
    private ArrayList<String> data = new ArrayList<String>();
    customButtonListener customListener;

    public interface customButtonListener {
        public void onButtonClickListener(int position, String value, ViewHolder viewHolder);
    }

    public void setCustomButtonListener(customButtonListener listener) {
        this.customListener = listener;
    }

    public class ViewHolder{
        TextView text;
        ImageButton button;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.activity_listview, null);
            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(R.id.cred_label);
            viewHolder.button = (ImageButton) convertView.findViewById(R.id.shwHideBtn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final String temp = (String) getItem(position);
        viewHolder.text.setText(temp);
        viewHolder.button.setTag("hidden");
        viewHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(customListener != null){
                    customListener.onButtonClickListener(position,temp, viewHolder);
                }
            }
        });
        return convertView;
    }

    public CustomListAdapter(Context context, ArrayList<String> listItems){
        super(context, R.layout.activity_listview, listItems);
        this.data = listItems;
        this.context = context;
    }

}
