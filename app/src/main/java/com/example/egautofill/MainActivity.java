package com.example.egautofill;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.room.Room;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.egautofill.DTO.UIDataDTO;
import com.example.egautofill.Entity.UserPassDataBase;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Toast;
import android.widget.Toolbar;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CustomListAdapter.customButtonListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
//        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.)
        getSupportActionBar().setElevation(0);
        LinearLayout contentLayout = coordinatorLayout.findViewById(R.id.contentLayout);
        BottomSheetBehavior<LinearLayout> sheetBehaviour = BottomSheetBehavior.from(contentLayout);
        sheetBehaviour.setFitToContents(false);
        sheetBehaviour.setHideable(false);
        sheetBehaviour.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        //Create the an adapter & populate the list view
        String[] credArray = {"sourabhmeena381@gmail.com", "err0w1", "9968380690", "saurabh.meena@zs.com"};
        AppDatabase appDatabase= Room.databaseBuilder(getApplicationContext(),AppDatabase.class,"user-db").allowMainThreadQueries().build();
        List<UIDataDTO> databases=appDatabase.userPassDataDao().getAll();
        ArrayList<String> dataArray = prepareDataForListView(databases);
        CustomListAdapter adapter = new CustomListAdapter(MainActivity.this, dataArray);
        ListView listView = (ListView) findViewById(R.id.cred_list);
        adapter.setCustomButtonListener(MainActivity.this);
        listView.setAdapter(adapter);
    }

//    @Override
    @Override
    public void onButtonClickListener(int position, String value, CustomListAdapter.ViewHolder viewHolder){
        Object buttonTag = viewHolder.button.getTag();
        if(buttonTag == "hidden"){
            viewHolder.button.setImageResource(R.drawable.ic_visibility_off_black_24dp);
            viewHolder.text.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            viewHolder.button.setTag("shown");
        } else if(buttonTag == "shown"){
            viewHolder.button.setImageResource(R.drawable.ic_remove_red_eye_24px);
            viewHolder.text.setInputType( InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
            viewHolder.button.setTag("hidden");
        }
//        Toast.makeText(MainActivity.this, "Value:" + value + "pos: " + position, Toast.LENGTH_SHORT).show();
    }

    protected ArrayList<String> prepareDataForListView(List<UIDataDTO> list){
        ArrayList<String> result = new ArrayList<String>();
        for(UIDataDTO Cred: list){
            result.add(Cred.getData());
        }
        return result;
    }

}