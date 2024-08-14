package com.superquiz.easyquiz.triviastar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


 

public class CategoriesActivity extends BaseActivity {
    private static final String TAG = "AdMob ads consent";
    static SharedPreferences preferences;
    static Typeface typeface;
    InterstitialAd mInterstitialAd;
    private ConsentInformation consentInformation;
    private ConsentForm consentForm;

    AdView mAdView;
    FirebaseUser user;
    String userName;
    RecyclerView recyclerViewCategories;
    Query query;
    String fontPath = "fonts/NeoSans.ttf";
    private GridLayoutManager mLayoutManager;



    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(
                this,
                getString(R.string.admob_interstitial_unit_id),
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        CategoriesActivity.this.mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        // Called when fullscreen content is dismissed.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        CategoriesActivity.this.mInterstitialAd = null;
                                        Log.d("TAG", "The ad was dismissed.");
                                        requestNewInterstitial();

                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        // Called when fullscreen content failed to show.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        CategoriesActivity.this.mInterstitialAd = null;
                                        Log.d("TAG", "The ad failed to show.");
                                        requestNewInterstitial();

                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        // Called when fullscreen content is shown.
                                        Log.d("TAG", "The ad was shown.");
                                        requestNewInterstitial();
                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;

                        String error =
                                String.format(
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());

                    }
                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //To Remove categories activity
//        Intent i2 = new Intent(this, LevelsActivity.class);
//        startActivity(i2);
//        finish();
//        overridePendingTransition(R.anim.goup, R.anim.godown);

        setContentView(R.layout.activity_categories);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(CategoriesActivity.this);

        recyclerViewCategories = (RecyclerView) findViewById(R.id.gridview);

        mLayoutManager = new GridLayoutManager(this, 2);

        //Change the number of columns for tablets
        DisplayMetrics metrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float yInches = metrics.heightPixels / metrics.ydpi;
        float xInches = metrics.widthPixels / metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);
        if (diagonalInches >= 6.5) {
            mLayoutManager = new GridLayoutManager(this, 4);

        }

        recyclerViewCategories.setLayoutManager(mLayoutManager);


        //**Ads EU conset**//
        String purchasedItem = preferences.getString("purchased", null);
        if (purchasedItem == null) {
            checkForConsent();
        }

        //AdMob Ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        if (purchasedItem == null) {
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }

        requestNewInterstitial();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            userName = extras.getString("userName");
        }

        // insert user name to the user object
        if (isOnline()) {
            user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getDisplayName() == null) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(userName).build();
                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("Firebase", "User profile updated.");
                                }
                            }
                        });
            }
        } else {
            Toast.makeText(this, getString(R.string.checkInternet) + "", Toast.LENGTH_SHORT).show();
        }


        typeface = Typeface.createFromAsset(getAssets(), fontPath);
        /////////////////////////////////////////////////////
        query = FirebaseDatabase.getInstance().getReference("categories").orderByKey();

        FirebaseRecyclerOptions<CategoriesModelClass> options =
                new FirebaseRecyclerOptions.Builder<CategoriesModelClass>()
                        .setQuery(query, CategoriesModelClass.class)
                        .build();

        final FirebaseRecyclerAdapter<CategoriesModelClass, CategoriesViewHolder> firebaseRecyclerAdapter2 =
                new FirebaseRecyclerAdapter<CategoriesModelClass, CategoriesViewHolder>(options) {


                    @Override
                    public CategoriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.category_item, parent, false);
                        return new CategoriesViewHolder(view);
                    }

                    ///////For descending order////////
                    @Override
                    public CategoriesModelClass getItem(int position) {
                        return super.getItem(position);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull CategoriesActivity.CategoriesViewHolder holder, int position, @NonNull CategoriesModelClass model) {
                        holder.setIsRecyclable(false);
                        holder.setImage(getApplicationContext(), model.getImage());
                        holder.setName(model.getName());

                    }
                    /////////////////////////////////
                };
        firebaseRecyclerAdapter2.startListening();

        recyclerViewCategories.setAdapter(firebaseRecyclerAdapter2);
        firebaseRecyclerAdapter2.notifyDataSetChanged();
        ////////////////
//Check for empty data
        query.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    firebaseRecyclerAdapter2.notifyDataSetChanged();
                } else {//is empty
                    Toast.makeText(CategoriesActivity.this, getString(R.string.nodata) + "", Toast.LENGTH_SHORT).show();
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
                firebaseRecyclerAdapter2.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @Nullable String s) {
                firebaseRecyclerAdapter2.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                firebaseRecyclerAdapter2.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });

        /////////////////////////////////////////////////////
    }


    /*******************************************/
    private void checkForConsent() {
        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(this)
                .setDebugGeography(ConsentDebugSettings
                        .DebugGeography
                        .DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId("")

                .build();



        // Set tag for underage of consent. false means users are not underage.
        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .setTagForUnderAgeOfConsent(false)
                //TODO Remove this line before production
//                .setConsentDebugSettings(debugSettings)
                .build();

        consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.requestConsentInfoUpdate(
                this,
                params,
                new ConsentInformation.OnConsentInfoUpdateSuccessListener() {
                    @Override
                    public void onConsentInfoUpdateSuccess() {
                        // The consent information state was updated.
                        // You are now ready to check if a form is available.
                        if (consentInformation.isConsentFormAvailable()) {
                            loadForm();
                        }
                    }
                },
                new ConsentInformation.OnConsentInfoUpdateFailureListener() {
                    @Override
                    public void onConsentInfoUpdateFailure(FormError formError) {
                        // Handle the error.
                    }
                });
    }

    public void loadForm(){
        UserMessagingPlatform.loadConsentForm(
                this,
                new UserMessagingPlatform.OnConsentFormLoadSuccessListener() {
                    @Override
                    public void onConsentFormLoadSuccess(ConsentForm consentForm) {
                        CategoriesActivity.this.consentForm = consentForm;
                        if(consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED) {
                            consentForm.show(
                                    CategoriesActivity.this,
                                    new ConsentForm.OnConsentFormDismissedListener() {
                                        @Override
                                        public void onConsentFormDismissed(@Nullable FormError formError) {
                                            // Handle dismissal by reloading form.
                                            loadForm();
                                        }
                                    });

                        }

                    }
                },
                new UserMessagingPlatform.OnConsentFormLoadFailureListener() {
                    @Override
                    public void onConsentFormLoadFailure(FormError formError) {
                        /// Handle Error.
                    }
                }
        );
    }

    /*******************************************/

//    @Override
//    protected void onResume() {
//        super.onResume();
//        levelAdapter.notifyDataSetChanged();
//    }
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public void showInterstitial() {
        Log.d("testets", "--");
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (mInterstitialAd != null) {
            mInterstitialAd.show(CategoriesActivity.this);
        }
    }

    //View Holder For Recycler View
    public class CategoriesViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public CategoriesViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    ConnectivityManager cm = (ConnectivityManager) v.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
                        Intent i = new Intent(v.getContext().getApplicationContext(), LevelsActivity.class);
                        i.putExtra("code", getAdapterPosition() + 1);
                        v.getContext().startActivity(i);
//                            overridePendingTransition(R.anim.goup, R.anim.godown);
                        String purchasedItem = preferences.getString("purchased", null);
                        if (purchasedItem == null) {
                            showInterstitial();
                        }

//                        }
                    } else {
                        Toast.makeText(v.getContext(), v.getContext().getString(R.string.checkInternet) + "", Toast.LENGTH_SHORT).show();
                    }

                }

            });
        }

        public void setImage(Context ctx, final String i) {
            final ImageView post_image = (ImageView) mView.findViewById(R.id.levelItem);
            Glide.with(ctx).asBitmap().load(i).into(post_image);

        }

        public void setName(final String n) {
            final TextView textView = (TextView) mView.findViewById(R.id.levelName);
            textView.setText(n);
            textView.setTypeface(typeface);


        }


    }
}
