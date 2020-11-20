package com.example.credpass.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.credpass.DTO.UIDataDTO;
import com.example.credpass.Entity.UserPassDataBase;

import java.util.List;

@Dao
public interface UserPassDataDao {
   @Query("SELECT * FROM UserPassDataBase")
    List<UIDataDTO> getAll();

    @Query("SELECT * from UserPassDataBase where  PACKAGE Like :packag and  tag Like :tag")
    List<UIDataDTO> getByPackageAndTag(String packag, String tag);

    @Query("SELECT * from USERPASSDATABASE where tag Like :tag")
     List<UIDataDTO> getByTag(String tag);

    @Query("SELECT * from UserPassDataBase where  PACKAGE Like :packag and  tag Like :tag and isIdPass Like :isIdPass")
    List<UIDataDTO> getByPackageTagAndIsPassId(String packag, String tag,String isIdPass);

    @Query("SELECT * from UserPassDataBase where tag Like :tag and isIdPass Like :isIdPass")
    List<UIDataDTO> getByTagAndIsPassId(String tag,String isIdPass);


    @Query("SELECT * from USERPASSDATABASE where  PACKAGE Like :packag")
 List<UIDataDTO> getByPackageName(String packag);

    @Insert
    void saveUserPassData(UserPassDataBase... userPassDataBase);

    @Query("DELETE from UserPassDataBase where uid =:skey")
    void deleteBySkey(Long skey);

    @Query("UPDATE UserPassDataBase SET DATA=:data and PASSWORD=:password where UID=:uid")
    void updateBySkey(Long uid,String data,String password);

    @Query("SELECT * FROM UserPassDataBase")
    LiveData<List<UIDataDTO>> getLifeCycleAll();

}
