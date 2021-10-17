package com.majedalmoqbeli.appssender.ui.application;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.format.Formatter;
import android.widget.Toast;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.majedalmoqbeli.appssender.R;
import com.majedalmoqbeli.appssender.models.ApplicationData;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ApplicationViewModel extends ViewModel {


    private MutableLiveData<ArrayList<ApplicationData>> applicationDataMutableLiveData;


    private PackageManager pm;


    public LiveData<ArrayList<ApplicationData>> getData(Context context) {
        if (applicationDataMutableLiveData == null) {
            applicationDataMutableLiveData = new MutableLiveData<>();
            getAppData(context);
        }
        return applicationDataMutableLiveData;
    }


    public ArrayList<ApplicationData> getListBySearch(String query) {
        ArrayList<ApplicationData> newAppData = new ArrayList<>();
        if (applicationDataMutableLiveData.getValue() != null) {
            for (ApplicationData item : applicationDataMutableLiveData.getValue()) {
                if ((item.getAppName().toLowerCase()).matches("(.*)" + query.toLowerCase() + "(.*)")
                )
                    newAppData.add(item);
            }
            if (newAppData.size() > 0) return newAppData;
        }
        return applicationDataMutableLiveData.getValue();


    }


    public void getListSortAtoZ() {

        if (applicationDataMutableLiveData.getValue() != null) {

            Comparator<ApplicationData> comparatorByName =
                    (ApplicationData o1, ApplicationData o2) ->
                            o1.getAppName().compareTo(o2.getAppName());

            Collections.sort(applicationDataMutableLiveData.getValue(), comparatorByName);

            applicationDataMutableLiveData.setValue(applicationDataMutableLiveData.getValue());

        }

    }


    public void deleteItem(int index) {

        if (applicationDataMutableLiveData.getValue() != null) {

            applicationDataMutableLiveData.getValue().remove(index);

        }

    }


    private void getAppData(Context context) {
        pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfo = pm.queryIntentActivities(intent, 0);
        if (resolveInfo.size() > 0) {
            setAppData(resolveInfo, context);
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.error), Toast.LENGTH_SHORT).show();
        }

    }

    private void setAppData(List<ResolveInfo> resolveInfo, Context context) {

        ArrayList<ApplicationData> appData = new ArrayList<>();
        for (int i = 0; i < resolveInfo.size(); i++) {
            ActivityInfo aInfo = resolveInfo.get(i).activityInfo;
            appData.add(new ApplicationData(resolveInfo.get(i).loadLabel(pm).toString()
                    , resolveInfo.get(i).loadIcon(pm),
                    aInfo.applicationInfo.packageName,
                    aInfo.name,
                    getApkSize(aInfo.applicationInfo.packageName, context)));
        }
        applicationDataMutableLiveData.setValue(appData);
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
