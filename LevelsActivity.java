package com.superquiz.easyquiz.triviastar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.superquiz.easyquiz.triviastar.MainActivity.readyForReview;


 


public class LevelsActivity extends BaseActivity {
    static int categoriesLevels;
    static int NumberOfQuestions;
    public List<String> Levels;
    //    InterstitialAd mInterstitialAd;
    SharedPreferences preferences;
    LevelAdapter levelAdapter;
    AdView mAdView;
    int code;
    GridView gridview;
    Float percentage;
    ProgressBar progressBar;
    TextView scoreTextView;
    TextView titleTextView;
    SharedPreferences.Editor editor;
    ReviewInfo reviewInfo;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_levels);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            code = extras.getInt("code");
        }
        //To Remove categories activity
//        code = 1;

        preferences = PreferenceManager.getDefaultSharedPreferences(LevelsActivity.this);
        editor = preferences.edit();


        Toolbar toolbar = (Toolbar) findViewById(R.id.custom_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
            upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);


        }

        //Retrieve the user data
        //==============================================//
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //Upload user data
        if (isOnline() && user != null && user.getDisplayName() != null) {
            final Query query = FirebaseDatabase.getInstance().getReference();
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("Users" + "/" + user.getUid() + "/data/" + code).child("total").exists()) {
                        editor.putInt("total" + code, Integer.valueOf(String.valueOf(dataSnapshot.child("Users" + "/" + user.getUid() + "/data/" + code).child("total").getValue())));
                        editor.apply();
                    }

                    if (dataSnapshot.child("Users" + "/" + user.getUid() + "/data/" + code).child("CurrentQuestion").exists()) {
                        editor.putInt("CurrentQuestion" + code, Integer.valueOf(String.valueOf(dataSnapshot.child("Users" + "/" + user.getUid() + "/data/" + code).child("total").getValue())));
                        editor.apply();
                    }

                    if (dataSnapshot.child("Users" + "/" + user.getUid() + "/score").exists()) {
                        editor.putInt("total", Integer.valueOf(String.valueOf(dataSnapshot.child("Users" + "/" + user.getUid() + "/score").getValue())));
                        editor.apply();
                    }

                    Query query2 = FirebaseDatabase.getInstance().getReference("data").child(String.valueOf(code)).orderByKey();
                    query2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot2) {
                            NumberOfQuestions = 0;
                            for (int i = 0; i < dataSnapshot2.getChildrenCount(); i++) {
                                int branch = i + 1;
                                NumberOfQuestions = Integer.parseInt(String.valueOf(NumberOfQuestions)) + Integer.parseInt(String.valueOf(dataSnapshot.child(String.valueOf(i + 1)).getChildrenCount()));

                                if (dataSnapshot.child("Users" + "/" + user.getUid() + "/data/" + code + "/" + branch + "/currentLevelQuestion").exists()) {
                                    editor.putInt("currentLevelQuestion" + code + "_" + branch, Integer.valueOf(String.valueOf(dataSnapshot.child("Users" + "/" + user.getUid() + "/data/" + code + "/" + branch + "/currentLevelQuestion").getValue())));
                                    editor.apply();
                                }

                                if (dataSnapshot.child("Users" + "/" + user.getUid() + "/data/" + code + "/" + branch + "/total").exists()) {
                                    editor.putInt("total" + code + "_" + branch, Integer.valueOf(String.valueOf(dataSnapshot.child("Users" + "/" + user.getUid() + "/data/" + code + "/" + branch + "/total").getValue())));
                                    editor.apply();
                                }

                                if (dataSnapshot.child("Users" + "/" + user.getUid() + "/data/" + code + "/" + branch + "/done").exists()) {
                                    editor.putString("done" + "_" + code + "_" + branch, String.valueOf(dataSnapshot.child("Users" + "/" + user.getUid() + "/data/" + code + "/" + branch + "/done").getValue()));
                                    editor.apply();
                                }
                                editor.putInt("question_total" + code, NumberOfQuestions);
                                editor.apply();
                                retrieveQuestionTile_LevelsNumber();
                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }

                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }else{
            retrieveQuestionTile_LevelsNumber();
        }

        //==============================================//

        progressBar = (ProgressBar) toolbar.findViewById(R.id.progress);
        scoreTextView = (TextView) toolbar.findViewById(R.id.score);
        titleTextView = (TextView) toolbar.findViewById(R.id.title);
        int progress = preferences.getInt("CurrentQuestion" + code, 0);
        if (preferences.getInt("question_total" + code, 0) > 0) {
            percentage = ((preferences.getInt("total" + code, 0) / Float.parseFloat(String.valueOf(preferences.getInt("question_total" + code, 0)))) * Float.parseFloat(String.valueOf(100)));
            Log.d("testtesttest", preferences.getInt("total" + code, 0) + "-->" + Float.parseFloat(String.valueOf(preferences.getInt("question_total" + code, 0))));
        } else {
            percentage = Float.valueOf(0);
        }
        if (preferences.getInt("question_total" + code, 0) > 0) {
            progressBar.setVisibility(View.VISIBLE);
            scoreTextView.setVisibility(View.VISIBLE);
            progressBar.setMax(preferences.getInt("question_total" + code, 0));
            progressBar.setProgress(progress);
            scoreTextView.setText(String.format(Locale.ENGLISH, "%.2f", percentage) + "%");
        } else {
            progressBar.setProgress(0);
            scoreTextView.setText("0.00%");
        }

//        Toast.makeText(this, progress+"->"+percentage+"->>"+preferences.getInt("question_total" + code, 0), Toast.LENGTH_SHORT).show();

        String purchasedItem = preferences.getString("purchased", null);
        //AdMob Ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });        mAdView = findViewById(R.id.adView);
        if (purchasedItem == null) {
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }

        //============In-App Review API==================//
        if (readyForReview == true) {
            final ReviewManager manager = ReviewManagerFactory.create(LevelsActivity.this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
                @Override
                public void onComplete(@NonNull Task<ReviewInfo> task) {
                    if (task.isSuccessful()) {
                        reviewInfo = task.getResult();
                        Task<Void> flow = manager.launchReviewFlow(LevelsActivity.this, reviewInfo);
                        flow.addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task2) {
                                // do nothing
                            }
                        });


                    }
                }
            });
        }
        //============================================//
    }

    public void retrieveQuestionTile_LevelsNumber() {

        if (isOnline()) {
            //get total questions number in the category
            if (preferences.getInt("question_total" + code, 0) == 0) {
                Query query2 = FirebaseDatabase.getInstance().getReference("data").child(String.valueOf(code)).orderByKey();
                query2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        NumberOfQuestions = 0;
                        for (int i = 0; i < dataSnapshot.getChildrenCount(); i++) {
                            NumberOfQuestions = Integer.parseInt(String.valueOf(NumberOfQuestions)) + Integer.parseInt(String.valueOf(dataSnapshot.child(String.valueOf(i + 1)).getChildrenCount()));
                        }

                        editor.putInt("question_total" + code, NumberOfQuestions);
                        editor.apply();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
            }

            //retrieve categories data

            Query query = FirebaseDatabase.getInstance().getReference("categories").orderByKey();
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(String.valueOf(code)).child("n").getValue() != null) {
                        titleTextView.setText(dataSnapshot.child(String.valueOf(code)).child("n").getValue() + "");
                    } else {
                        titleTextView.setText(getString(R.string.level) + " " + code);
                    }

                    ///get number of levels in the category//
                    Query query2 = FirebaseDatabase.getInstance().getReference("data").child(String.valueOf(code)).orderByKey();
                    query2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            categoriesLevels = Integer.parseInt(String.valueOf(dataSnapshot.getChildrenCount()));
                            //Prepare levels
                            levelAdapter = new LevelAdapter();
                            Levels = new ArrayList<>();
                            for (int i = 1; i <= categoriesLevels; i++) {
                                Levels.add(String.valueOf(i));
                            }
                            gridview = (GridView) findViewById(R.id.gridview);
                            if (levelAdapter.getCount() > 0) {
                                gridview.setAdapter(levelAdapter);


                            }

                            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                    int Branches_num = position;
                                    if (Branches_num == 0) {
                                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                        i.putExtra("branch", position + 1);
                                        i.putExtra("code", code);
                                        startActivity(i);
                                        overridePendingTransition(R.anim.goup, R.anim.godown);

//                                        String purchasedItem = preferences.getString("purchased", null);
//                                        if (purchasedItem == null) {
//                                            if (mInterstitialAd.isLoaded()) {
//                                                mInterstitialAd.show();
//                                                requestNewInterstitial();
//                                            }
//                                        }
                                    } else {
                                        if (preferences.getString("done" + "_" + code + "_" + Branches_num, null) != null && preferences.getString("done" + "_" + code + "_" + Branches_num, null).equals("yes")) {
                                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                            i.putExtra("branch", position + 1);
                                            i.putExtra("code", code);
                                            startActivity(i);
                                            overridePendingTransition(R.anim.goup, R.anim.godown);
//                                            String purchasedItem = preferences.getString("purchased", null);
//                                            if (purchasedItem == null) {
//                                                if (mInterstitialAd.isLoaded()) {
//                                                    mInterstitialAd.show();
//                                                    requestNewInterstitial();
//                                                }
//                                            }
                                        }
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
        } else {
            Snackbar.make(gridview, getString(R.string.checkInternet), Snackbar.LENGTH_SHORT).show();
            finish();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        retrieveQuestionTile_LevelsNumber();


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(LevelsActivity.this, CategoriesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.goup, R.anim.godown);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                Intent i = new Intent(getApplicationContext(), CategoriesActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.goup, R.anim.godown);


                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*******************************************************************************************/


    public class LevelAdapter extends BaseAdapter {
        public List<Integer> mProgressIds;
        private LayoutInflater mInflater;

        public LevelAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return Levels.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }


        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(
                        R.layout.level_item, null);
                holder.levelNumberTextView = (TextView) convertView.findViewById(R.id.levelItem);
                holder.lockImageView = (ImageView) convertView.findViewById(R.id.lock_imageView);
                holder.isDoneImageView = (ImageView) convertView.findViewById(R.id.isDone);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            //detect if the level is completed
            if (preferences.getString("done" + "_" + code + "_" + Integer.parseInt(String.valueOf(position + 1)), null) != null && preferences.getString("done" + "_" + code + "_" + Integer.parseInt(String.valueOf(position + 1)), null).equals("yes")) {
                holder.isDoneImageView.setVisibility(View.VISIBLE);
            } else {
                holder.isDoneImageView.setVisibility(View.GONE);

            }
            if (position > 0) {
                //detect if level opened
                //if (preferences.getInt("CurrentQuestion" + Integer.parseInt(String.valueOf(position)), 0) == preferences.getInt("question_total" + Integer.parseInt(String.valueOf(position)), 0) && preferences.getInt("CurrentQuestion" + Integer.parseInt(String.valueOf(position)), 0) > 0) {
                if (preferences.getString("done" + "_" + code + "_" + position, null) != null && preferences.getString("done" + "_" + code + "_" + position, null).equals("yes")) {
                    holder.levelNumberTextView.setText(position + 1 + "");
//                    holder.levelNumberTextView.setBackgroundResource(R.drawable.level_number_background);
                    holder.lockImageView.setVisibility(View.GONE);
                    holder.levelNumberTextView.setVisibility(View.VISIBLE);

                } else {
//                    holder.levelNumberTextView.setBackgroundResource(R.drawable.lock);
                    holder.levelNumberTextView.setText("");
                    holder.lockImageView.setVisibility(View.VISIBLE);
//                    holder.levelNumberTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    holder.levelNumberTextView.setVisibility(View.GONE);


                }
            } else {
                holder.levelNumberTextView.setText(position + 1 + "");
                holder.lockImageView.setVisibility(View.GONE);
                holder.levelNumberTextView.setVisibility(View.VISIBLE);


            }
            return convertView;
        }
    }

    class ViewHolder {
        TextView levelNumberTextView;
        ImageView lockImageView, isDoneImageView;
    }


}