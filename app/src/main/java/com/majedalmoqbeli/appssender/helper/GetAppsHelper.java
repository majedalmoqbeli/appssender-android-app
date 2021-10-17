package com.majedalmoqbeli.appssender.helper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.format.Formatter;
import com.majedalmoqbeli.appssender.models.ApplicationData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GetAppsHelper {

    private final Context context;
    private PackageManager pm;

    public GetAppsHelper(Context context) {
        this.context = context;
    }

    public ArrayList<ApplicationData> getAppData() {
        pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfo = pm.queryIntentActivities(intent, 0);
        if (resolveInfo.size() > 0) {
            return setAppData(resolveInfo);
        } else {
            return null;
        }

    }

    private ArrayList<ApplicationData> setAppData(List<ResolveInfo> resolveInfo) {

        ArrayList<ApplicationData> appData = new ArrayList<>();
        for (int i = 0; i < resolveInfo.size(); i++) {
            ActivityInfo aInfo = resolveInfo.get(i).activityInfo;
            appData.add(new ApplicationData(resolveInfo.get(i).loadLabel(pm).toString()
                    , resolveInfo.get(i).loadIcon(pm),
                    aInfo.applicationInfo.packageName,
                    aInfo.name,
                    getApkSize(aInfo.applicationInfo.packageName, context)));
        }
        return appData;
    }

    private String getApkSize(String packageName, Context context) {
        try {
            long appSize = new
                    File(pm.getApplicationInfo(packageName, 0).publicSourceDir).length();
            return String.valueOf(Formatter.formatShortFileSize(context, appSize));
        } catch (PackageManager.NameNotFoundException e) {
            return "0";
        }
    }


}
