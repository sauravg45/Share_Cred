package com.example.credpass.Entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "USERPASSDATABASE")
public class UserPassDataBase {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "PACKAGE")
    public String packageName;

    @ColumnInfo(name = "IDENTIFIER")
    public String identifier;

    @ColumnInfo(name="DATA")
    public String data;

    @ColumnInfo(name="PASSWORD")
    public  String password;

    @ColumnInfo(name="TAG")
    public String tag;

    @ColumnInfo(name = "APP_NAME")
    public String appName;

    @ColumnInfo(name = "ICON")
    public String icon;

    @ColumnInfo(name="TIMESTAMP")
    public String timestamp;

    @ColumnInfo(name="ISIDPASS")
    public String isIdPass;

    public UserPassDataBase() {

    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getdata() {
        return data;
    }

    public void setdata(String data) {
        this.data = data;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getIsIdPass() {
        return isIdPass;
    }

    public void setIsIdPass(String isIdPass) {
        this.isIdPass = isIdPass;
    }
}
