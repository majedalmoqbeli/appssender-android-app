package com.majedalmoqbeli.appssender.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.cardview.widget.CardView;
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
import com.majedalmoqbeli.appssender.helper.AdmobHelper;
import com.majedalmoqbeli.appssender.constants.AdmobKey;
import com.majedalmoqbeli.appssender.models.ApplicationData;
import com.majedalmoqbeli.appssender.R;

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
    private InterstitialAd mInterstitialAd;
    private AdmobHelper admobHelper;

    public ShowAppAdapter(Context context, ArrayList<ApplicationData> appData) {
        this.context = context;
        this.appData = appData;
        setUpAds();
    }

    private void setUpAds() {
        admobHelper = new AdmobHelper(context);
        admobHelper.setupInterstitialAd(AdmobKey.INTERSTITIAL_LIST_ID);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.template_app, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(appData.get(position));
    }

    @Override
    public int getItemCount() {
        return appData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView appIcon;
        final ImageView shareApp;
        final ImageView deleteApp;
        final TextView appName;
        final TextView appPackage, appSize;
        ApplicationData mItem;
        CardView cardView;

        ViewHolder(View itemView) {
            super(itemView);

            appIcon = itemView.findViewById(R.id.appIcon);
            shareApp = itemView.findViewById(R.id.shareApp);
            deleteApp = itemView.findViewById(R.id.deleteApp);
            appSize = itemView.findViewById(R.id.appSize);
            cardView = itemView.findViewById(R.id.cardView);

            TooltipCompat.setTooltipText(deleteApp, context.getString(R.string.deleteApp));
            TooltipCompat.setTooltipText(shareApp, context.getString(R.string.shareApp));
            appPackage = itemView.findViewById(R.id.appPackage);
            appName = itemView.findViewById(R.id.appName);


            itemView.setOnClickListener(this);


        }


        void bind(ApplicationData data) {
            mItem = data;
            appIcon.setImageDrawable(data.getAppIcon());
            appName.setText(data.getAppName());
            appSize.setText(data.getAppSize());
            appPackage.setText(data.getAppPackage());
            deleteApp.setOnClickListener(view -> {
                Uri packageUri = Uri.parse("package:" + data.getAppPackage());
                Intent intent = new Intent(Intent.ACTION_DELETE, packageUri);
                context.startActivity(intent);
                removeItem(getAdapterPosition());
            });

            shareApp.setOnClickListener(view -> {
                shareApplication();
            });
        }


        void removeItem(int p) {
            notifyItemRemoved(p);
        }


        private void shareApplication() {

            ApplicationInfo packageinfo = null;
            try {
                packageinfo = context.getPackageManager().getApplicationInfo(mItem.getAppPackage(), 0);
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
                tempFile = new File(tempFile.getPath() + "/" + mItem.getAppName() + ".apk");
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


        @Override
        public void onClick(View view) {

            admobHelper.showInterstitialAd();
            Intent intent = new Intent(Intent.ACTION_MAIN)
                    .setClassName(mItem.getAppPackage(), mItem.getAppActivityName());
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        }


    }


}



