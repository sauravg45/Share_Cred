package com.example.egautofill;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.example.egautofill.DTO.UIDataDTO;
import com.example.egautofill.Entity.UserPassDataBase;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppDatabase appDatabase= Room.databaseBuilder(getApplicationContext(),AppDatabase.class,"user-db").allowMainThreadQueries().build();
        List<UIDataDTO> databases=appDatabase.userPassDataDao().getAll();
        String viewInf=databases.toString();

        TextView editText=(TextView) findViewById(R.id.passData);
        editText.setText(viewInf);
    }
}