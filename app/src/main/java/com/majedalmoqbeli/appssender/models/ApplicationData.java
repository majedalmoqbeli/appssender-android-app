package com.majedalmoqbeli.appssender.models;

import android.graphics.drawable.Drawable;

public class ApplicationData {

    private String appName;
    private String appPackage;
    private String appSize;
    private String appActivityName;
    private Drawable appIcon;

    public ApplicationData(String appName, Drawable appIcon,
                            String appPackage, String appActivityName,
                           String appSize) {
        this.appName = appName;
        this.appIcon = appIcon;
        this.appPackage = appPackage;
        this.appActivityName = appActivityName;
        this.appSize=appSize;
    }


    public String getAppActivityName() {
        return appActivityName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public String getAppSize() {
        return appSize;
    }
}
