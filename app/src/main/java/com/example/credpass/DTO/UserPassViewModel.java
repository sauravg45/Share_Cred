package com.example.credpass.DTO;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.credpass.database.UserPassRepo;
import com.example.credpass.Entity.UserPassDataBase;

import java.util.List;

public class UserPassViewModel extends AndroidViewModel {
    private UserPassRepo dataRepo;

    private LiveData<List<UIDataDTO>> dataDto;

    public UserPassViewModel(Application application){
        super(application);
        dataRepo=new UserPassRepo(application);
        dataDto=dataRepo.getAllData();
    }

   public LiveData<List<UIDataDTO>> getAllUIdata(){
        return dataDto;
    }
}
