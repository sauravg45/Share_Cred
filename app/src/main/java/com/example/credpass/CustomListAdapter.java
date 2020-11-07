package com.example.credpass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.credpass.DTO.UIDataDTO;


import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomListAdapter extends ArrayAdapter {
    private Context context;
    private List data;
    customButtonListener customListener;

    public interface customButtonListener {
        public void onButtonClickListener(int position, UIDataDTO data, ViewHolder viewHolder);
    }

    public void setCustomButtonListener(customButtonListener listener) {
        this.customListener = listener;
    }

    public class ViewHolder{
        TextView pass;
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
            viewHolder.pass = (TextView) convertView.findViewById(R.id.cred_pass);
            viewHolder.button = (ImageButton) convertView.findViewById(R.id.shwHideBtn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final UIDataDTO temp = (UIDataDTO) getItem(position);
        viewHolder.text.setText(temp.getData());
        viewHolder.pass.setText(temp.getPassword());
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

    public CustomListAdapter(Context context, List listItems){
        super(context, R.layout.activity_listview, listItems);
        this.data = listItems;
        this.context = context;
    }

}
