package com.majedalmoqbeli.appssender.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.majedalmoqbeli.appssender.R;
import com.majedalmoqbeli.appssender.adapter.ShowAppAdapter;
import com.majedalmoqbeli.appssender.helper.AdmobHelper;
import com.majedalmoqbeli.appssender.constants.AdmobKey;
import com.majedalmoqbeli.appssender.databinding.ActivityMainBinding;
import com.majedalmoqbeli.appssender.models.ApplicationData;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<ResolveInfo> resolveInfo;
    private ArrayList<ApplicationData> appData;
    private ArrayList<ApplicationData> newAppData = new ArrayList<>();
    private ShowAppAdapter adapter;


    private ActivityMainBinding binding;

    private AdmobHelper admobHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setUpAds();
        initToolBar();
        initDrawer();
        initNavigationView();


        getDate();


    }

    private void setUpAds() {
        admobHelper = new AdmobHelper(this);
        admobHelper.initializeAds();
        admobHelper.setupBanner(binding.btn.adView);
        admobHelper.setupBanner(binding.btn.adViewTop);
        admobHelper.setupInterstitialAd(AdmobKey.INTERSTITIAL_ID);
    }


    private void initDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, binding.toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void initToolBar() {

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.app_name);
    }

    private void initNavigationView() {


        binding.navigationView.setNavigationItemSelectedListener(this);

        binding.navigationView.inflateMenu(R.menu.activity_main_drawer);


    }


    private void getDate() {

        if (getAppData())
            setRecyclerView(appData);
    }


    private void setRecyclerView(ArrayList<ApplicationData> data) {
        binding.btn.numberOf.setText(getString(R.string.thereIs, String.valueOf(data.size())));
        binding.btn.recyclerView.removeAllViews();
        binding.btn.recyclerView.setHasFixedSize(true);
        binding.btn.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ShowAppAdapter(this, data);
        binding.btn.recyclerView.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null) {
                    if (!query.isEmpty()) getListBySearch(query);
                    if (query.isEmpty()) getListBySearch("null");
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null) {
                    if (newText.length() >= 1) getListBySearch(newText);
                    if (newText.isEmpty()) getListBySearch("null");
                }
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            getListBySearch("%");
            return false;
        });


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sortAtoZ) {
            getListSortAtoZ();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.aboutApp) {
            admobHelper.showInterstitialAd();
            getAlertAboutApp();
        } else if (id == R.id.aboutDev) {
            admobHelper.showInterstitialAd();
            getAlertAboutDeveloper();
        } else if (id == R.id.shareApp) {
            admobHelper.showInterstitialAd();
            shareApp();
        } else if (id == R.id.moreApp) {
            admobHelper.showInterstitialAd();
            moreApp();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void getListBySearch(String query) {
        newAppData.clear();
        for (ApplicationData item : appData) {
            if ((item.getAppName().toLowerCase()).matches("(.*)" + query.toLowerCase() + "(.*)")
            )
                newAppData.add(item);

        }
        if (newAppData.size() > 0) setRecyclerView(newAppData);
        else setRecyclerView(appData);
    }

    private void getListSortAtoZ() {
        Comparator<ApplicationData> comparatorByName =
                (ApplicationData o1, ApplicationData o2) ->
                        o1.getAppName().compareTo(o2.getAppName());

        Collections.sort(appData, comparatorByName);

        setRecyclerView(appData);
    }

    private boolean getAppData() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveInfo = pm.queryIntentActivities(intent, 0);
        if (resolveInfo.size() > 0) {
            setAppData();
            return true;
        } else {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void setAppData() {
        PackageManager pm = getPackageManager();

        appData = new ArrayList<>();
        for (int i = 0; i < resolveInfo.size(); i++) {
            ActivityInfo aInfo = resolveInfo.get(i).activityInfo;
            appData.add(new ApplicationData(resolveInfo.get(i).loadLabel(pm).toString()
                    , resolveInfo.get(i).loadIcon(pm),
                    aInfo.applicationInfo.packageName,
                    aInfo.name,
                    getApkSize(aInfo.applicationInfo.packageName)));
        }
    }

    private void shareApp() {
        Intent waIntent = new Intent(Intent.ACTION_SEND);
        waIntent.setType("text/plain");

        String text = (getString(R.string.downloadApp, "https://play.google.com/store/apps/details?id=com.majedalmoqbeli.appssender"));

        waIntent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(waIntent, getString(R.string.selectToShare)));


    }

    private void moreApp() {
        Uri uri = Uri.parse("https://play.google.com/store/apps/developer?id=Majed+Al-Moqbeli"); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);

    }

    private void getAlertAboutDeveloper() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        @SuppressLint("InflateParams") View myView = LayoutInflater.from(this).inflate(R.layout.custom_developer, null);
        LinearLayout linearCall = myView.findViewById(R.id.linearCall);
        LinearLayout linearWhatsapp = myView.findViewById(R.id.linearWhatsapp);
        ImageView ImageFacebook = myView.findViewById(R.id.ImageFacebook);
        ImageView ImageIns = myView.findViewById(R.id.ImageIns);
        ImageView imageTwitter = myView.findViewById(R.id.imageTwitter);


        dialogBuilder.setTitle(getString(R.string.aboutDev));
        dialogBuilder.setIcon(R.drawable.ic_dev);
        dialogBuilder.setView(myView)
                .setCancelable(true);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        linearCall.setOnClickListener(view -> {
            Uri uri = Uri.parse("tel:+967774732246"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
        linearWhatsapp.setOnClickListener(view -> {
            Uri uri = Uri.parse("https://wa.me/967734262535"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
        ImageFacebook.setOnClickListener(view -> {
            Uri uri = Uri.parse("https://fb.com/majedalmoqbeli"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
        ImageIns.setOnClickListener(view -> {
            Uri uri = Uri.parse("https://instagram.com/majedalmoqbeli"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
        imageTwitter.setOnClickListener(view -> {
            Uri uri = Uri.parse("https://twitter.com/majedalmoqbeli"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
    }

    private void getAlertAboutApp() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        @SuppressLint("InflateParams") View myView = LayoutInflater.from(this).inflate(R.layout.custom_about, null);
        TextView version = myView.findViewById(R.id.version);

        PackageInfo pinfo;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            version.setText(getString(R.string.version, pinfo.versionName));

        } catch (Exception e) {

        }


        dialogBuilder.setTitle(getString(R.string.aboutapp));
        dialogBuilder.setIcon(R.drawable.ic_aboutapp);
        dialogBuilder.setView(myView)
                .setCancelable(true);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

    }

    public String getApkSize(String packageName) {
        try {
            long appSize = new
                    File(getPackageManager().getApplicationInfo(packageName, 0).publicSourceDir).length();
            return String.valueOf(Formatter.formatShortFileSize(this, appSize));
        } catch (PackageManager.NameNotFoundException e) {
            return "0";
        }
    }


}
