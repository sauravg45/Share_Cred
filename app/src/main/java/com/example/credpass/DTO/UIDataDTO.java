package com.example.credpass.DTO;

import androidx.room.ColumnInfo;

public class UIDataDTO {

    @ColumnInfo(name="UID")
    public Long skey;

    @ColumnInfo(name="PASSWORD")
    public  String password;

    @ColumnInfo(name = "APP_NAME")
    public String appName;

    @ColumnInfo(name = "ICON")
    public String icon;

    @ColumnInfo(name="DATA")
    public String data;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Long getSkey() {
        return skey;
    }

    public void setSkey(Long skey) {
        this.skey = skey;
    }

    @Override
    public String toString() {
        return "UIDataDTO{" +
                "password='" + password + '\'' +
                ", appName='" + appName + '\'' +
                ", icon='" + (icon==null) + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
