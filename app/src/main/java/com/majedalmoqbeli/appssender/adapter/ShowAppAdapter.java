package com.majedalmoqbeli.appssender.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.TooltipCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.majedalmoqbeli.appssender.databinding.ItemAppBinding;
import com.majedalmoqbeli.appssender.helper.AdmobHelper;
import com.majedalmoqbeli.appssender.constants.AdmobKey;
import com.majedalmoqbeli.appssender.models.ApplicationData;
import com.majedalmoqbeli.appssender.R;
import com.majedalmoqbeli.appssender.ui.application.ApplicationViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class ShowAppAdapter extends RecyclerView.Adapter<ShowAppAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<ApplicationData> appData;
    private final TextView number;
    private AdmobHelper admobHelper;
    private int p;
    private  ApplicationViewModel model;

    public ShowAppAdapter(Context context, ArrayList<ApplicationData> appData, TextView number,
                          ApplicationViewModel model) {
        this.context = context;
        this.appData = appData;
        this.number = number;
        this.model = model;
        setUpAds();
    }

    private void setUpAds() {
        admobHelper = new AdmobHelper(context);
        admobHelper.setupInterstitialAd(AdmobKey.INTERSTITIAL_LIST_ID);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(context),
                        R.layout.item_app, parent, false));


    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(appData.get(position));
    }

    public int getPosition() {
        return p;
    }

    public void removeItem(int p) {

        if (!isAppInstalled(appData.get(p).getAppPackage())) {
            model.deleteItem(p);
            notifyItemRemoved(p);
            updateCount();
        }
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    private void updateCount() {
        number.setText(context.getResources().getString(R.string.thereIs, String.valueOf(appData.size())));
    }

    @Override
    public int getItemCount() {
        return appData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAppBinding binding;

        public ViewHolder(@NonNull ItemAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


            TooltipCompat.setTooltipText(binding.deleteApp, context.getString(R.string.deleteApp));
            TooltipCompat.setTooltipText(binding.shareApp, context.getString(R.string.shareApp));


        }


        void bind(ApplicationData data) {

            binding.setAppData(data);
            binding.executePendingBindings();

            setOnClicked();

            updateCount();
        }

        private void setOnClicked() {
            binding.deleteApp.setOnClickListener(view -> {
                Uri packageUri = Uri.parse("package:" + appData.get(getBindingAdapterPosition()).getAppPackage());
                Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                ((Activity) context).startActivityForResult(intent, 1);
                p = getBindingAdapterPosition();
            });

            binding.shareApp.setOnClickListener(view -> {

                shareApplication();
            });

            binding.openApp.setOnClickListener(view -> {
                admobHelper.showInterstitialAd();
                Intent intent = new Intent(Intent.ACTION_MAIN)
                        .setClassName(appData.get(getBindingAdapterPosition()).getAppPackage(), appData.get(getBindingAdapterPosition()).getAppActivityName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });

        }


        private void shareApplication() {
            try {
                // Get application info
                String packageName = appData.get(getBindingAdapterPosition()).getAppPackage();
                ApplicationInfo packageInfo = context.getPackageManager().getApplicationInfo(packageName, 0);
                File originalApk = new File(packageInfo.publicSourceDir);

                // Use app-specific cache directory (no permissions needed)
                File cacheDir = new File(context.getCacheDir(), "ExtractedApk");
                if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                    Toast.makeText(context, "Failed to create directory", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Prepare destination file
                String appName = appData.get(getBindingAdapterPosition()).getAppName();
                File tempFile = new File(cacheDir, appName + ".apk");

                // Copy the APK file efficiently
                try (InputStream in = new FileInputStream(originalApk);
                     OutputStream out = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                }

                // Share the APK using FileProvider
                Uri fileUri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".provider",
                        tempFile
                );

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("application/vnd.android.package-archive");
                intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                context.startActivity(Intent.createChooser(
                        intent,
                        context.getResources().getString(R.string.selectToShare)
                ));

            } catch (Exception e) {
                Toast.makeText(context, context.getResources().getString(R.string.errorToShare), Toast.LENGTH_SHORT).show();
                Log.e("AppShare", "Error sharing app", e);
            }
        }

    }


}



