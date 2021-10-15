package com.majedalmoqbeli.appssender.helper;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.majedalmoqbeli.appssender.constants.AdmobKey;

import java.util.Arrays;
import java.util.List;

public class AdmobHelper {


    private Context context;


    public AdmobHelper(Context context) {
        this.context = context;
    }

    /**
     ******* just call it one time ******
     */
    public void initializeAds() {
        List<String> testDeviceIds = Arrays.asList(AdmobKey.TEST_DEVICES);
        RequestConfiguration configuration =
                new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
        MobileAds.setRequestConfiguration(configuration);
        MobileAds.initialize(context);
    }


    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    public void setupBanner(AdView adView) {

        adView.loadAd(getAdRequest());
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.

                Log.i("AdsBanner Loaded =>", "DONE");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                Log.i("AdsBanner ERROR =>", adError.toString());
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });
    }


    private InterstitialAd mInterstitialAd;

    public void setupInterstitialAd(String ID) {

        InterstitialAd.load(context, ID, getAdRequest(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i("AdsInterstitialAd=>", "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("AdsInterstitialAd", loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }

    public void showInterstitialAd() {
        if (mInterstitialAd != null)
            mInterstitialAd.show((Activity) context);
    }
}
