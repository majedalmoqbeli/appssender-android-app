package com.majedalmoqbeli.appssender.ui.application;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.majedalmoqbeli.appssender.R;
import com.majedalmoqbeli.appssender.adapter.ShowAppAdapter;
import com.majedalmoqbeli.appssender.helper.AdmobHelper;
import com.majedalmoqbeli.appssender.constants.AdmobKey;
import com.majedalmoqbeli.appssender.databinding.ActivityMainBinding;
import com.majedalmoqbeli.appssender.models.ApplicationData;

import java.util.ArrayList;
import java.util.Objects;


public class ApplicationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private ShowAppAdapter adapter;


    private ActivityMainBinding binding;

    private AdmobHelper admobHelper;
    private ApplicationViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setupAds();
        initToolBar();
        initRecyclerView();
        initDrawer();
        initNavigationView();


        initViewModel();

    }

    private void initRecyclerView() {
        binding.btn.recyclerView.removeAllViews();
        binding.btn.recyclerView.setHasFixedSize(true);
        binding.btn.recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initViewModel() {
        model = new ViewModelProvider(this).get(ApplicationViewModel.class);
        model.getData(this).observe(this, data -> {
            if (adapter == null) {
                setRecyclerView(data);
            } else {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void setupAds() {
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


    private void setRecyclerView(ArrayList<ApplicationData> data) {

        adapter = new ShowAppAdapter(this, data, binding.btn.numberOf);
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

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null) {
                    if (newText.length() >= 1) setRecyclerView(model.getListBySearch(newText));
                    ;
                    if (newText.isEmpty()) setRecyclerView(model.getListBySearch("null"));
                }
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            setRecyclerView(model.getListBySearch("%"));
            return false;
        });


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sortAtoZ) {
            model.getListSortAtoZ();
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
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ApplicationActivity.this);
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
            Uri uri = Uri.parse("https://fb.com/majedalmoqbeli0"); // missing 'http://' will cause crashed
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        adapter.removeItem(adapter.getPosition());

    }


}
