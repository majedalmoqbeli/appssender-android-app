package com.majedalmoqbeli.appssender.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.TooltipCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.interstitial.InterstitialAd;
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

    public ShowAppAdapter(Context context, ArrayList<ApplicationData> appData, TextView number) {
        this.context = context;
        this.appData = appData;
        this.number = number;
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
            appData.remove(p);
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

            ApplicationInfo packageinfo = null;
            try {
                packageinfo = context.getPackageManager().getApplicationInfo(appData.get(getBindingAdapterPosition()).getAppPackage(), 0);
            } catch (Exception e) {
                Log.i("ErrorException", e.toString());
            }
            File filePath = new File(Objects.requireNonNull(packageinfo).publicSourceDir);

            Intent intent = new Intent(Intent.ACTION_SEND);

            // MIME of .apk is "application/vnd.android.package-archive".
            // but Bluetooth does not accept this. Let's use "*/*" instead.
            intent.setType("application/vnd.android.package-archive");

            // Append file and send Intent
            File originalApk = new File(String.valueOf(filePath));


            try {
                //Make new directory in new location
                File tempFile = new File(context.getExternalCacheDir() + "/ExtractedApk");
                //If directory doesn't exists create new
                if (!tempFile.isDirectory())
                    if (!tempFile.mkdirs())
                        return;

                //Get application's name and convert to lowercase
                tempFile = new File(tempFile.getPath() + "/" + appData.get(getBindingAdapterPosition()).getAppName() + ".apk");
                //If file doesn't exists create new
                if (!tempFile.exists()) {
                    if (!tempFile.createNewFile()) {
                        return;
                    }
                }
                //Copy file to new location
                InputStream in = new FileInputStream(originalApk);
                OutputStream out = new FileOutputStream(tempFile);

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                System.out.println("File copied.");
                //Open share dialog

                Uri myUri;
                if (Build.VERSION.SDK_INT >= 24) {

                    myUri = FileProvider.getUriForFile(context.getApplicationContext(),
                            context.getPackageName() + ".provider", tempFile);


                } else
                    myUri = Uri.fromFile(tempFile);
                intent.putExtra(Intent.EXTRA_STREAM, myUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.selectToShare)));
            } catch (Exception e) {
                Toast.makeText(context, context.getResources().getString(R.string.errorToShare), Toast.LENGTH_SHORT).show();
                Log.i("ExceptionShare", e.toString());
            }

        }


    }


}



