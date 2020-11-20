package com.example.credpass.database;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.credpass.DTO.UIDataDTO;
import com.example.credpass.Dao.UserPassDataDao;
import com.example.credpass.database.AppDatabase;
import com.example.credpass.Entity.UserPassDataBase;

import java.util.List;

public class UserPassRepo {
    private UserPassDataDao userDataDao;
    private LiveData<List<UIDataDTO>> userPassData;

    public UserPassRepo(Application application){
        AppDatabase database=AppDatabase.getAppDatabase(application);
        userDataDao=database.userPassDataDao();
        userPassData=userDataDao.getLifeCycleAll();
        Log.d("Lets see Repo",userPassData.toString());
    }

    public LiveData<List<UIDataDTO>>  getAllData(){
        return userPassData;
    }

    public void insert (UserPassDataBase userdata) {
        new insertAsyncTask(userDataDao).execute(userdata);
    }
    private static class insertAsyncTask extends AsyncTask<UserPassDataBase, Void, Void> {

        private UserPassDataDao asyncDao;

        insertAsyncTask(UserPassDataDao userDao){
            asyncDao=userDao;
        }

        @Override
        protected Void doInBackground(UserPassDataBase... userPassDataBases) {
           asyncDao.saveUserPassData(userPassDataBases[0]);
            return null;
        }
    }

}
