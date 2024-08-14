package com.superquiz.easyquiz.triviastar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

 

public class LeaderBoardActivity extends AppCompatActivity {
    static TextView noData;
    static int allNods;
    private static Firebase firebaseRef;
    RecyclerView mNamesList;
    ProgressBar listprogressBar;
    AdView mAdView;
    SharedPreferences preferences;
    Query query, query2;
    de.hdodenhof.circleimageview.CircleImageView first_image, second_image, third_image;
    TextView first_name, second_name, third_name;
    TextView first_score, second_score, third_score;
    List<String> top3List;
    RelativeLayout second_layout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        Firebase.setAndroidContext(this);
        noData = (TextView) findViewById(R.id.noData);
        listprogressBar = (ProgressBar) findViewById(R.id.listProgressBar);
        mNamesList = (RecyclerView) findViewById(R.id.list);

        first_image = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.first_image);
        second_image = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.second_image);
        third_image = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.third_image);
        first_name = (TextView) findViewById(R.id.first_name);
        second_name = (TextView) findViewById(R.id.second_name);
        third_name = (TextView) findViewById(R.id.third_name);
        first_score = (TextView) findViewById(R.id.first_score);
        second_score = (TextView) findViewById(R.id.second_score);
        third_score = (TextView) findViewById(R.id.third_score);

        second_layout = (RelativeLayout) findViewById(R.id.second_layout);

        second_layout.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        preferences = PreferenceManager.getDefaultSharedPreferences(LeaderBoardActivity.this);

        //AdMob Ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        String purchasedItem = preferences.getString("purchased", null);
        if (purchasedItem == null) {

            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.leaderboard));
        // remove the fix size to showing it in the fragment
        mNamesList.setHasFixedSize(false);
        ///////For descending order////////
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mNamesList.setLayoutManager(linearLayoutManager);

        ////////////////////////////////////


        //set 1st,2nd,3rd place
        //get key of the top 3 users
        top3List = new ArrayList<>();

        query2 = FirebaseDatabase.getInstance().getReference("Users").orderByChild("score").limitToLast(3);
        query2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    top3List.add(String.valueOf(snapshot.getKey()));

                }
                Collections.reverse(top3List);


                if (top3List.size() >= 3) {
                    Glide.with(getApplicationContext()).load(dataSnapshot.child(top3List.get(0)).child("image").getValue()).into(first_image);
                    Glide.with(getApplicationContext()).load(dataSnapshot.child(top3List.get(1)).child("image").getValue()).into(second_image);
                    Glide.with(getApplicationContext()).load(dataSnapshot.child(top3List.get(2)).child("image").getValue()).into(third_image);
                    first_name.setText(String.valueOf(dataSnapshot.child(top3List.get(0)).child("name").getValue()));
                    second_name.setText(String.valueOf(dataSnapshot.child(top3List.get(1)).child("name").getValue()));
                    third_name.setText(String.valueOf(dataSnapshot.child(top3List.get(2)).child("name").getValue()));
                    first_score.setText(String.valueOf(dataSnapshot.child(top3List.get(0)).child("score").getValue()));
                    second_score.setText(String.valueOf(dataSnapshot.child(top3List.get(1)).child("score").getValue()));
                    third_score.setText(String.valueOf(dataSnapshot.child(top3List.get(2)).child("score").getValue()));
                } else if (top3List.size() == 2) {
                    Glide.with(getApplicationContext()).load(dataSnapshot.child(top3List.get(0)).child("image").getValue()).into(first_image);
                    Glide.with(getApplicationContext()).load(dataSnapshot.child(top3List.get(1)).child("image").getValue()).into(second_image);
                    first_name.setText(String.valueOf(dataSnapshot.child(top3List.get(0)).child("name").getValue()));
                    second_name.setText(String.valueOf(dataSnapshot.child(top3List.get(1)).child("name").getValue()));
                    first_score.setText(String.valueOf(dataSnapshot.child(top3List.get(0)).child("score").getValue()));
                    second_score.setText(String.valueOf(dataSnapshot.child(top3List.get(1)).child("score").getValue()));
                } else if (top3List.size() == 1) {
                    Glide.with(getApplicationContext()).load(dataSnapshot.child(top3List.get(0)).child("image").getValue()).into(first_image);
                    first_name.setText(String.valueOf(dataSnapshot.child(top3List.get(0)).child("name").getValue()));
                    first_score.setText(String.valueOf(dataSnapshot.child(top3List.get(0)).child("score").getValue()));
                }


            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //list of the rest winner
        query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("score").limitToLast(50);
        FirebaseRecyclerOptions<LeaderBoardModelClass> options =
                new FirebaseRecyclerOptions.Builder<LeaderBoardModelClass>()
                        .setQuery(query, LeaderBoardModelClass.class)
                        .build();
        final FirebaseRecyclerAdapter<LeaderBoardModelClass, LeaderboardViewHolder> firebaseRecyclerAdapter5 =
                new FirebaseRecyclerAdapter<LeaderBoardModelClass, LeaderboardViewHolder>(options) {

                    @NonNull
                    @Override
                    public LeaderboardViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.user_row, parent, false);
                        return new LeaderboardViewHolder(view);
                    }

                    /////For descending order////////
                    @Override
                    public LeaderBoardModelClass getItem(int position) {
                        allNods = getItemCount();
                        return super.getItem(position);

                    }

                    @Override
                    protected void onBindViewHolder(@NonNull LeaderBoardActivity.LeaderboardViewHolder holder, int position, @NonNull LeaderBoardModelClass model) {
                        if (allNods - position > 3) {
                            holder.setIsRecyclable(true);
                            holder.setName(model.getName());
                            holder.setScore(model.getScore());
                            holder.setImage(model.getImage());
                            holder.setRank(position);
                            holder.setVisible();
                        } else {
                            holder.setGone();

                        }
                    }
                    ///////////////////////////////
                };
        firebaseRecyclerAdapter5.startListening();

        mNamesList.setAdapter(firebaseRecyclerAdapter5);

        firebaseRecyclerAdapter5.notifyDataSetChanged();
//Check for empty data
        query.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    firebaseRecyclerAdapter5.notifyDataSetChanged();
                } else {//is empty
                    Toast.makeText(LeaderBoardActivity.this, getString(R.string.nodata) + "", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //Check for updating data
        query.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @Nullable String s) {
                firebaseRecyclerAdapter5.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @Nullable String s) {
                firebaseRecyclerAdapter5.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                firebaseRecyclerAdapter5.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });

    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent i2 = new Intent(this, CategoriesActivity.class);
                startActivity(i2);
                finish();
                overridePendingTransition(R.anim.goup, R.anim.godown);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i2 = new Intent(this, CategoriesActivity.class);
        startActivity(i2);
        finish();
        overridePendingTransition(R.anim.goup, R.anim.godown);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Thread.currentThread().interrupt();
    }



    //ViewHolder for leaderboard list
    public static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ViewGroup.LayoutParams params;

        //Font
        String fontPath = "fonts/NeoSans.ttf";
        Typeface tf = Typeface.createFromAsset(itemView.getContext().getAssets(), fontPath);

        public LeaderboardViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;


        }

        public void setName(final String name) {
            TextView nameTv = (TextView) itemView.findViewById(R.id.user_name);
            nameTv.setText(name);
            nameTv.setTypeface(tf);
        }

        public void setScore(final int score) {
            TextView scoreTv = (TextView) itemView.findViewById(R.id.user_score);
            scoreTv.setText(score + "");

        }


        public void setRank(final int i) {
            TextView rankTV = (TextView) itemView.findViewById(R.id.user_rank);
            rankTV.setText((allNods - i) + ".");
        }

        public void setImage(final String image) {
            ImageView userImage = (ImageView) itemView.findViewById(R.id.user_image);
            Glide.with(mView.getContext()).load(image).into(userImage);
        }

        public void setGone() {
            final CardView cardView = (CardView) mView.findViewById(R.id.toplayout);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, 0);
            layoutParams.setMargins(0, 0, 0, 0);
            cardView.setLayoutParams(layoutParams);
            cardView.setFocusable(true);
            cardView.setMaxCardElevation(0);

        }

        public void setVisible() {
            final CardView cardView = (CardView) mView.findViewById(R.id.toplayout);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = (int) (Resources.getSystem().getDisplayMetrics().density * 5);
            layoutParams.setMargins(margin, margin, margin, margin);
            cardView.setLayoutParams(layoutParams);
            cardView.setFocusable(true);
            cardView.setMaxCardElevation(5);

        }

    }
    /*******************************************************************************************/

}