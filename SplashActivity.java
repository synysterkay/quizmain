package com.superquiz.easyquiz.triviastar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;




public class SplashActivity extends Activity {
    ImageView imageView, animatedBackground;
    TextView title;
    Runnable runnable;
    FirebaseAnalytics mFirebaseAnalytics;
    FirebaseUser user;
    private FirebaseRemoteConfig firebaseRemoteConfig;
    public static String timeInSeconds;
    static final String timeInSeconds_KEY = "timer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Firebase.setAndroidContext(this);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


        //============fetch timer=============//
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d("TAG", "Config params updated: " + updated);


                        } else {

                        }
                        timeInSeconds = firebaseRemoteConfig.getString(timeInSeconds_KEY);
                    }
                });

        //====================================//
        imageView = (ImageView) findViewById(R.id.imageView);
        animatedBackground = (ImageView) findViewById(R.id.animated_background);
        title = (TextView) findViewById(R.id.title);

        //animated imageView
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        imageView.startAnimation(animation);

        // animate the background
        int currentRotation = 0;
        Animation anim = new RotateAnimation(currentRotation, (360 * 4), Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setDuration(5000);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setRepeatMode(Animation.RESTART);
        anim.setFillEnabled(true);
        anim.setFillAfter(true);
        animatedBackground.startAnimation(anim);

        String fontPath = "fonts/NeoSans.ttf";
        Typeface font = Typeface.createFromAsset(getAssets(), fontPath);
        title.setTypeface(font);

        final Handler handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isOnline()) {
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        //To Remove categories activity
//                        Intent intent = new Intent(SplashActivity.this, LevelsActivity.class);
                        Intent intent = new Intent(SplashActivity.this, CategoriesActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    }
                } else {

                    Toast.makeText(SplashActivity.this, getString(R.string.checkInternet) + "", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        };
        handler.postDelayed(runnable, 3000);


    }


    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

}