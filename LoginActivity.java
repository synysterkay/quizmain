package com.superquiz.easyquiz.triviastar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;

public class LoginActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    public View mProgressView;
    SharedPreferences sharedPreferences;
    SharedPreferences preferences;
    Typeface font;
    TextView title;
    ImageButton skipButton;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_login);

        if (!isOnline()) {
            Toast.makeText(LoginActivity.this, getString(R.string.checkInternet), Toast.LENGTH_SHORT).show();
        }

        mProgressView = findViewById(R.id.login_progress);
        title = findViewById(R.id.title);
        skipButton = findViewById(R.id.skip);

        skipButton.setOnClickListener(this);

        String fontPath = "fonts/NeoSans.ttf";
        font = Typeface.createFromAsset(getAssets(), fontPath);
        title.setTypeface(font);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.skip) {
            Intent i = new Intent(LoginActivity.this, CategoriesActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
