package com.majedalmoqbeli.appssender.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.majedalmoqbeli.appssender.R;
import com.majedalmoqbeli.appssender.adapter.ShowAppAdapter;
import com.majedalmoqbeli.appssender.models.ApplicationData;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView recyclerView;
    private List<ResolveInfo> resolveInfo;
    private ArrayList<ApplicationData> appData;
    private final ArrayList<ApplicationData> newAppData = new ArrayList<>();
    private ShowAppAdapter adapter;
    private InterstitialAd mInterstitialAd;
    private TextView numberOf;
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        MobileAds.initialize(this, "ca-app-pub-1576857231249604~4606015804");

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-1576857231249604/3398065286");

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1576857231249604/3588447605");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        recyclerView = findViewById(R.id.recyclerView);
        numberOf = findViewById(R.id.numberOf);

        if (getAppData())
            setRecyclerView(appData);


        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.i("Loaded", "Loaded");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                Log.i("Failed", "" + errorCode);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
    }

    private void setRecyclerView(ArrayList<ApplicationData> data) {
        numberOf.setText(getString(R.string.thereIs, String.valueOf(data.size())));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.removeAllViews();
        adapter = new ShowAppAdapter(this, data, mInterstitialAd);
        recyclerView.setAdapter(adapter);
        recyclerView.setSelected(true);


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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.sortAtoZ) {
            getListSortAtoZ();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.aboutApp:
                if (mInterstitialAd.isLoaded())
                    mInterstitialAd.show();
                getAlertAboutApp();
                break;
            case R.id.aboutDev:
                if (mInterstitialAd.isLoaded())
                    mInterstitialAd.show();
                getAlertAboutDeveloper();
                break;
            case R.id.shareApp:
                shareApp();
                break;
            case R.id.moreApp:
                moreApp();
                break;
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
        @SuppressLint("InflateParams") View myView = LayoutInflater.from(this).inflate(R.layout.about_developer, null);
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
        @SuppressLint("InflateParams") View myView = LayoutInflater.from(this).inflate(R.layout.ticket_info_app, null);
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
            long appSize = new File(getPackageManager().getApplicationInfo(packageName, 0).publicSourceDir).length();
            return String.valueOf(Formatter.formatShortFileSize(this, appSize));
        } catch (PackageManager.NameNotFoundException e) {
            return "0";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resolveInfo.clear();
        if (getAppData())
            setRecyclerView(appData);

    }


}
