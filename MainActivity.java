package com.superquiz.easyquiz.triviastar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;


import static android.content.ContentValues.TAG;
import static com.superquiz.easyquiz.triviastar.SplashActivity.timeInSeconds;
import static com.google.android.gms.ads.RequestConfiguration.MAX_AD_CONTENT_RATING_G;
import static com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE;



public class MainActivity extends Activity implements View.OnClickListener {
    public static char[] user_submit_answer;
    public static boolean readyForReview = false;
    static int currentLetter = 0;
    static int showed_ad_question = 0;
    static int NumberOfQuestionsInLevel;
    public List<String> choicesLetters = new ArrayList<>();
    public char[] answer;
    public List<Integer> user_selected_choices_positions = new ArrayList<>();
    public List<char[]> modified_user_submit_answer;
    public List<Integer> spacePosition = new ArrayList<>();
    String QuestionText, AnswerText, OriginalAnswer, ImageURL, VoiceURL;
    SharedPreferences preferences;
    TextView questionNumberTV, scoreTV, questionTV, LevelNumberTV;
    Button revealButton, askButton, doneButton;
    ImageButton resetButton, skipButton, voiceImage;
    //    ImageView questionImage;
    ShapeableImageView questionImage;
    RecyclerView choicesGridview, answerGridview;
    String correct_answer;
    String[] alphabet_character;
    int code;
    int branch;
    int CurrentQuestion;
    SharedPreferences.Editor editor;
    MediaPlayer adPlayer, correctPlayer, wrongPlayer, clickPlayer, finishPlayer, tickPlayer, timeoutPlayer;
    Animation blinkAnimation, clockwiseAnimation;
    Boolean showAnswer, skipPressed = false, skipped = false;
    AdView mAdView;
    DisplayMetrics dm;
    float screenWidth, screenHeight;
    String fontPath = "fonts/NeoSans.ttf";
    Typeface font;
    RecyclerChoicesAdapter recyclerChoicesAdapter;
    RecyclerAnswersAdapter recyclerAnswersAdapter;
    int currentLevelQuestion;
    FirebaseUser user;
    ProgressBar progressBar;
    CountDownTimer countDownTimer;
    boolean isRunning = false;
    long startTime = 0;
    Dialog dialogWrong;
    MediaPlayer mediaPlayer;
    boolean isLoading;
    //    RewardedAd mRewardedVideoAd;
    private RewardedAd rewardedAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        dm = MainActivity.this.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels / dm.density;
        screenHeight = dm.heightPixels / dm.density;


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            code = extras.getInt("code");
            branch = extras.getInt("branch");

        }
        Firebase.setAndroidContext(this);
        retrieveNumberOfQuestions();

        //the alphabet from strings.xml
        alphabet_character = getResources().getStringArray(R.array.alphabet);

        preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        editor = preferences.edit();
        CurrentQuestion = preferences.getInt("CurrentQuestion" + code, 0);
        currentLevelQuestion = preferences.getInt("currentLevelQuestion" + code + "_" + branch, 1);

        //AdMob Ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        //=======Designed for children==========//
        RequestConfiguration requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder().setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setMaxAdContentRating(MAX_AD_CONTENT_RATING_G)
                .build();
        MobileAds.setRequestConfiguration(requestConfiguration);
//=======================================//

        mAdView = findViewById(R.id.adView);
        String purchasedItem = preferences.getString("purchased", null);
        if (purchasedItem == null) {
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }

        loadRewardedAd();
        //===============********************===================//
        progressBar = findViewById(R.id.progressBar);

        //=================********************======================//

        //Initialize views
        questionNumberTV = findViewById(R.id.questionNumber);
        scoreTV = findViewById(R.id.score);
        questionTV = findViewById(R.id.questionText);
        LevelNumberTV = findViewById(R.id.level_number);
        revealButton = findViewById(R.id.reveal);
        askButton = findViewById(R.id.ask);
        resetButton = findViewById(R.id.reset);
        skipButton = findViewById(R.id.skip);
        doneButton = findViewById(R.id.done);
        questionImage = findViewById(R.id.questionImage);
        voiceImage = findViewById(R.id.voiceImage);
        answerGridview = findViewById(R.id.answerGridview);
        choicesGridview = findViewById(R.id.choicesGridview);

        ///////////Showing items in Horizontal instead of listView//////////

        choicesGridview.setHasFixedSize(false);
        answerGridview.setHasFixedSize(false);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setAlignItems(AlignItems.CENTER);
        layoutManager.setJustifyContent(JustifyContent.CENTER);

        choicesGridview.setLayoutManager(layoutManager);

        FlexboxLayoutManager layoutManager2 = new FlexboxLayoutManager(this);
        layoutManager2.setFlexWrap(FlexWrap.WRAP);
        layoutManager2.setAlignItems(AlignItems.CENTER);
        layoutManager2.setJustifyContent(JustifyContent.CENTER);
        answerGridview.setLayoutManager(layoutManager2);


        //Font
        font = Typeface.createFromAsset(getAssets(), fontPath);
        questionNumberTV.setTypeface(font);
        scoreTV.setTypeface(font);
        questionTV.setTypeface(font);
        LevelNumberTV.setTypeface(font);
        revealButton.setTypeface(font);
        askButton.setTypeface(font);
        doneButton.setTypeface(font);

        //Scroll textView
        questionTV.setMovementMethod(new ScrollingMovementMethod());
        //Initialize the new question
//        LevelNumberTV.setText(getString(R.string.level) + " " + code);
        retrieveQuestionTile();
        retrieveData();
        if (isFirstRun()) {
            super.showUpgradeDialog();
        }
    }

    @Override
    public void onClick(View view) {
        String purchasedItem = preferences.getString("purchased", null);
        int total = preferences.getInt("total" + code + "_" + branch, 0);
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.reveal:
//                NextQuestion();
                if (isRunning == true) {
                    countDownTimer.cancel();
                    if (tickPlayer != null && tickPlayer.isPlaying()) {
                        tickPlayer.stop();
                    }
                }
                if (purchasedItem == null) {
                    Reveal_Answer_Dialog();
                } else {
                    currentLetter = 0;
                    for (int i = 0; i < answer.length; i++) {
                        String Letter = String.valueOf(answer[i]);
                        char compare = Letter.charAt(0);
                        user_submit_answer[currentLetter] = compare;
                        if (currentLetter + 1 < answer.length) {
                            currentLetter = currentLetter + 1;
                        }

                        //Update UI
                        recyclerAnswersAdapter = new RecyclerAnswersAdapter(user_submit_answer, this);
                        answerGridview.setAdapter(recyclerAnswersAdapter);
                        recyclerAnswersAdapter.notifyDataSetChanged();
                    }

                }
                break;
            case R.id.ask:

                ShareScreenshot();
                break;

            case R.id.reset:
                NextQuestion();
                break;

            case R.id.skip:
                if (isRunning == true) {
                    countDownTimer.cancel();
                    if (tickPlayer != null && tickPlayer.isPlaying()) {
                        tickPlayer.stop();
                    }
                    isRunning = false;
                }
                if (total >= Integer.parseInt(getString(R.string.skip_score))) {
                    if (purchasedItem == null) {
                        Skip_Dialog();
                    } else {
                        CurrentQuestion = CurrentQuestion + 1;
                        currentLevelQuestion++;

                        editor.putInt("CurrentQuestion" + code, CurrentQuestion);
                        editor.putInt("currentLevelQuestion" + code + "_" + branch, currentLevelQuestion);

                        editor.putInt("total" + code, preferences.getInt("total" + code, 0) - Integer.parseInt(getString(R.string.skip_score)));
                        editor.putInt("total" + code + "_" + branch, preferences.getInt("total" + code + "_" + branch, 0) - Integer.parseInt(getString(R.string.skip_score)));
                        editor.putInt("total", preferences.getInt("total", 0) - Integer.parseInt(getString(R.string.skip_score)));
                        editor.apply();
                        retrieveData();
                        scoreTV.setText(preferences.getInt("total" + code + "_" + branch, 0) + "");
                    }
                } else {
                    Toast.makeText(this, getString(R.string.skip_score_warning) + " " + getString(R.string.skip_score) + " " + getString(R.string.score), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.done:
                String result = "";
                for (int i = 0; i < user_submit_answer.length; i++)
                    result += String.valueOf(user_submit_answer[i]);
                if (result.equalsIgnoreCase(correct_answer)) {
                    if (isRunning == true) {
                        countDownTimer.cancel();
                    }
                    if (tickPlayer != null && tickPlayer.isPlaying()) {
                        tickPlayer.stop();
                    }
                    isRunning = false;
                    user_submit_answer = new char[correct_answer.length()];
                    CurrentQuestion = CurrentQuestion + 1;
                    currentLevelQuestion++;

                    editor.putInt("CurrentQuestion" + code, CurrentQuestion);
                    editor.putInt("currentLevelQuestion" + code + "_" + branch, currentLevelQuestion);

                    editor.putInt("total" + code, preferences.getInt("total" + code, 0) + 1);
                    editor.putInt("total" + code + "_" + branch, preferences.getInt("total" + code + "_" + branch, 0) + 1);
                    editor.putInt("total", preferences.getInt("total", 0) + 1);
                    editor.apply();
                    Correct_Dialog();

                } else {
                    if (total > 0) {
                        editor.putInt("total" + code, preferences.getInt("total" + code, 0) - 1);
                        editor.putInt("total" + code + "_" + branch, preferences.getInt("total" + code + "_" + branch, 0) - 1);
                        editor.putInt("total", preferences.getInt("total", 0) - 1);

                        editor.apply();
                        scoreTV.setText(preferences.getInt("total" + code + "_" + branch, 0) + "");
                    }
                    Wrong_Dialog();

                }

                user = FirebaseAuth.getInstance().getCurrentUser();
                //Upload user data
                if (isOnline() && user != null && user.getDisplayName() != null) {
                    final DatabaseReference refDirect = FirebaseDatabase.getInstance().getReference();
                    refDirect.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            refDirect.child("Users" + "/" + user.getUid() + "/score").setValue(preferences.getInt("total", 0));
                            refDirect.child("Users" + "/" + user.getUid() + "/data/" + code + "/total").setValue(preferences.getInt("total" + code, 0));
                            refDirect.child("Users" + "/" + user.getUid() + "/data/" + code + "/CurrentQuestion").setValue(CurrentQuestion);
                            refDirect.child("Users" + "/" + user.getUid() + "/data/" + code + "/" + branch + "/currentLevelQuestion").setValue(currentLevelQuestion);
                            refDirect.child("Users" + "/" + user.getUid() + "/data/" + code + "/" + branch + "/total").setValue(preferences.getInt("total" + code + "_" + branch, 0));
                            refDirect.child("Users" + "/" + user.getUid() + "/data/" + code + "/" + branch + "/done").setValue(preferences.getString("done" + "_" + code + "_" + branch, null));

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }

                    });
                }
                break;

        }
    }

    private boolean isFirstRun() {
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("isFirstRun", true);
        if (isFirstRun) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isFirstRun", false);
            editor.apply();
        }
        return isFirstRun;
    }
    private void NextQuestion() {
        doneButton.setPressed(false);
        revealButton.setEnabled(true);
        showAnswer = false;
        skipPressed = false;
        if ((currentLevelQuestion) % Integer.parseInt(getString(R.string.adsCounter)) == 0 && (currentLevelQuestion) > Integer.parseInt(getString(R.string.adsCounter)) - 1) {
            if (!((MainActivity.this).isFinishing())) {
                if (currentLevelQuestion < NumberOfQuestionsInLevel) {
                    if (showed_ad_question < currentLevelQuestion) {
                        Ads_Dialog();
                    }
                }
            }
        }
        scoreTV.setText(preferences.getInt("total" + code + "_" + branch, 0) + "");
        if (currentLevelQuestion <= NumberOfQuestionsInLevel) {
            if (isRunning == false) {
                if (skipPressed == false) {
                    NewCountDown();
                }
            }
            Log.d("rrrrrr", currentLevelQuestion + ">>" + NumberOfQuestionsInLevel);
            if (AnswerText != null && !AnswerText.equals("")) {
                if (currentLevelQuestion <= Integer.parseInt(String.valueOf(NumberOfQuestionsInLevel))) {
                    questionNumberTV.setText(currentLevelQuestion + "/" + Integer.parseInt(String.valueOf(NumberOfQuestionsInLevel)));
                }
                currentLetter = 0;
                CurrentQuestion = preferences.getInt("CurrentQuestion" + code, 0);
                questionTV.setText(QuestionText);


                if (ImageURL != null && QuestionText != null) {
                    Glide.with(getApplicationContext()).load(ImageURL).into(questionImage);

                    questionImage.setVisibility(View.VISIBLE);
//                    questionImage.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    questionTV.setVisibility(View.VISIBLE);
                    questionTV.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    if(dm.density == 3 && dm.heightPixels < 2000){
                        questionImage.getLayoutParams().height = (int) (questionImage.getMeasuredWidth() / 2);
                        //Avoid wrong image hight if image getMeasuredWidth() == 0
                        if (questionImage.getMeasuredWidth() == 0) {
                            questionImage.getLayoutParams().height = (int) (dm.widthPixels / 2) ;
                        }
                    }else{
                        questionImage.getLayoutParams().height = (int) (questionImage.getMeasuredWidth() / 2) + 100;
                        //Avoid wrong image hight if image getMeasuredWidth() == 0
                        if (questionImage.getMeasuredWidth() == 0) {
                            questionImage.getLayoutParams().height = (int) (dm.widthPixels / 2) + 100;
                        }
                    }


                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) questionTV.getLayoutParams();
                    params.topMargin = 0;
                    questionTV.setLayoutParams(params);

                } else if (ImageURL == null && QuestionText != null) {
                    questionTV.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                    questionTV.setVisibility(View.VISIBLE);
                    questionImage.setVisibility(View.GONE);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) questionTV.getLayoutParams();
                    params.topMargin = 50;
                    questionTV.setLayoutParams(params);

                } else if (QuestionText == null && ImageURL != null) {
                    questionImage.setVisibility(View.VISIBLE);
                    Glide.with(getApplicationContext()).load(ImageURL).into(questionImage);
                    if(dm.density == 3 && dm.heightPixels < 2000) {
                        questionImage.getLayoutParams().height = (int) (questionImage.getMeasuredWidth() / 2);
                        //Avoid wrong image hight if image getMeasuredWidth() == 0
                        if (questionImage.getMeasuredWidth() == 0) {
                            questionImage.getLayoutParams().height = (int) (dm.widthPixels / 2);
                        }
                    }else{
                        questionImage.getLayoutParams().height = (int) (questionImage.getMeasuredWidth() / 2) + 100;
                        //Avoid wrong image hight if image getMeasuredWidth() == 0
                        if (questionImage.getMeasuredWidth() == 0) {
                            questionImage.getLayoutParams().height = (int) (dm.widthPixels / 2) + 100;
                        }
                    }


                    questionTV.setVisibility(View.GONE);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) questionTV.getLayoutParams();
                    params.topMargin = 0;
                    questionTV.setLayoutParams(params);
                }


                ////=======Save spaces positions and remove spaces from the answer=============////
                for (int i = 0; i < AnswerText.length(); i++) {
                    char space = AnswerText.charAt(i);
                    if (String.valueOf(space).equals(" ")) {

                        //Get the correct position after deleting the empty spaces

                        int j = i - spacePosition.size() - 1;

                        spacePosition.add(j);
                    }

                }
                //==============================================================================//
                AnswerText = AnswerText.replace(" ", "");
                correct_answer = AnswerText;
                correct_answer = correct_answer.substring(correct_answer.lastIndexOf("/") + 1);

                answer = correct_answer.toCharArray();

                //===========31/1/2021============//
                user_selected_choices_positions = new ArrayList<>(answer.length);


                user_submit_answer = new char[answer.length];

                //Add Answer character to List
                choicesLetters.clear();
                user_selected_choices_positions.clear();
                for (char item : answer) {
                    //Add logo name to list
                    choicesLetters.add(String.valueOf(item));

                }
                Random random = new Random();

                //Random add some character to list
                for (int i = answer.length; i < answer.length * 2; i++)
                    choicesLetters.add(alphabet_character[random.nextInt(alphabet_character.length)]);

                //Sort random
                Collections.shuffle(choicesLetters);

                //Set for GridView
                recyclerAnswersAdapter = new RecyclerAnswersAdapter(answerFields(), this);
                recyclerChoicesAdapter = new RecyclerChoicesAdapter(choicesLetters, this, MainActivity.this);

                recyclerAnswersAdapter.notifyDataSetChanged();
                recyclerChoicesAdapter.notifyDataSetChanged();

                choicesGridview.setAdapter(recyclerChoicesAdapter);
                answerGridview.setAdapter(recyclerAnswersAdapter);
            } else {
                retrieveData();
            }
        } else {
            if (currentLevelQuestion <= Integer.parseInt(String.valueOf(NumberOfQuestionsInLevel))) {

                questionNumberTV.setText(currentLevelQuestion + "/" + Integer.parseInt(String.valueOf(NumberOfQuestionsInLevel)));
            }
//            if (preferences.getInt("CurrentQuestion" + code, 0) > 0) {
            if (preferences.getInt("currentLevelQuestion" + code + "_" + branch, 1) > 1) {
//                Float percentage = ((preferences.getInt("total" + code, 0) / Float.parseFloat(String.valueOf(NumberOfQuestions))) * Float.parseFloat(String.valueOf(100)));
                Float percentage = ((preferences.getInt("total" + code + "_" + branch, 0) / Float.parseFloat(String.valueOf(NumberOfQuestionsInLevel))) * Float.parseFloat(String.valueOf(100)));
                if (percentage >= Float.parseFloat(getString(R.string.pass_average))) {
                    Finish_Level_Dialog();
                    UploadData();

                } else {
                    Failed_Level_Dialog();
                }
            }
        }

    }

    private char[] answerFields() {
        char[] result = new char[answer.length];
        for (int i = 0; i < answer.length; i++)
            result[i] = ' ';
        return result;
    }


    //Share the Image
    public void ShareScreenshot() {
        String package_name = getPackageName();

        // Get access to the URI for the bitmap
        Uri bmpUri = getLocalBitmapUri();
        if (bmpUri != null) {
            // Construct a ShareIntent with link to image
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_name) + "\n" + "https://play.google.com/store/apps/details?id=" + package_name);

            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.askfriends)));
        } else {
        }
    }

    // Returns the URI path to the Bitmap displayed in specified ImageView
    public Uri getLocalBitmapUri() {
        LinearLayout linearLayout = findViewById(R.id.parent); /*Your root view to be part of screenshot*/
        linearLayout.buildDrawingCache();
        Bitmap bmp = linearLayout.getDrawingCache();
        // Store image to default external storage directory
        Uri bmpUri = null;
        File file;
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/" + getString(R.string.app_name) + "/");
            dir.mkdirs();
            String fileName = "/" + System.currentTimeMillis() + "share_image.png";

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                file = new File(dir, fileName);
            } else {
                file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            }

            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    public void Ads_Dialog() {
        if (isRunning == true) {
            countDownTimer.cancel();
        }

        showed_ad_question = currentLevelQuestion;
        if (preferences.getBoolean("sounds", true) == true) {
            adPlayer = MediaPlayer.create(MainActivity.this, R.raw.ads);
            adPlayer.start();
        }
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.ads_dialog_layout);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        // set the custom dialog components - text, image and button
        final Button buttonClose = dialog.findViewById(R.id.continue_button);
        final Button buttonBack = dialog.findViewById(R.id.back_button);
        TextView textViewTitle = dialog.findViewById(R.id.rest_title);
        ImageView imageView = dialog.findViewById(R.id.imageView);

        blinkAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        imageView.setAnimation(blinkAnimation);

        int progress = (int) ((preferences.getInt("currentLevelQuestion" + code + "_" + branch, 0) / Float.parseFloat(String.valueOf(NumberOfQuestionsInLevel))) * Float.parseFloat(String.valueOf(100)));
        textViewTitle.setText(getString(R.string.adsText1) + " " + progress + "% " + getString(R.string.adsText2));

        //Font
        String fontPath = "fonts/NeoSans.ttf";
        Typeface font = Typeface.createFromAsset(getAssets(), fontPath);
        textViewTitle.setTypeface(font);
        buttonClose.setTypeface(font);
        buttonBack.setTypeface(font);

        buttonClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (preferences.getBoolean("sounds", true) == true) {
                    if (adPlayer != null && adPlayer.isPlaying()) {
                        adPlayer.stop();
                    }
                }
                String purchasedItem = preferences.getString("purchased", null);
                if (purchasedItem == null) {
                    showRewardedVideo();

                }

            }
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (preferences.getBoolean("sounds", true) == true) {
                    if (adPlayer != null && adPlayer.isPlaying()) {
                        adPlayer.stop();
                    }
                }
            }
        });
        dialog.show();

    }


    public void UploadData() {
        //upload updated user scores
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (isOnline() && user != null && user.getDisplayName() != null) {

            final DatabaseReference refDirect = FirebaseDatabase.getInstance().getReference();
            refDirect.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    refDirect.child("Users").child(user.getUid() + "/score").setValue(preferences.getInt("total", 0));
                    refDirect.child("Users").child(user.getUid() + "/name").setValue(user.getDisplayName());
                    refDirect.child("Users").child(user.getUid() + "/image").setValue(user.getPhotoUrl().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    public void Finish_Level_Dialog() {
        editor.putString("done" + "_" + code + "_" + branch, "yes");
        editor.apply();

        if (preferences.getBoolean("sounds", true) == true) {
            finishPlayer = MediaPlayer.create(MainActivity.this, R.raw.finish);
            finishPlayer.start();
        }
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.finish_level_dialog_layout);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        // set the custom dialog components - text, image and button
        final Button buttonReplay = dialog.findViewById(R.id.replay_button);
        final Button buttonBack = dialog.findViewById(R.id.back_button);
        final TextView buttonLogin = dialog.findViewById(R.id.loginTitle);
        final TextView line = dialog.findViewById(R.id.line);
        TextView textViewTitle = dialog.findViewById(R.id.level_title);
        ImageView imageView = dialog.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.endlevel);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            buttonLogin.setVisibility(View.GONE);
            line.setVisibility(View.GONE);
        }


        blinkAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        imageView.setAnimation(blinkAnimation);
        Float percentage = ((preferences.getInt("total" + code + "_" + branch, 0) / Float.parseFloat(String.valueOf(NumberOfQuestionsInLevel))) * Float.parseFloat(String.valueOf(100)));

        textViewTitle.setText(getString(R.string.endLevel) + " " + String.format(Locale.ENGLISH, "%.2f", percentage) + "%" + "");
        //Font
        String fontPath = "fonts/NeoSans.ttf";
        Typeface font = Typeface.createFromAsset(getAssets(), fontPath);
        textViewTitle.setTypeface(font);
        buttonReplay.setTypeface(font);
        buttonBack.setTypeface(font);

        buttonReplay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                currentLevelQuestion = 1;
                CurrentQuestion = 0;
                if (preferences.getBoolean("sounds", true) == true) {
                    if (finishPlayer != null && finishPlayer.isPlaying()) {
                        finishPlayer.stop();
                    }
                }

                //*****************Reset Current level insted of all levels in the category**************************//
                if (preferences.getInt("CurrentQuestion" + code, 0) - NumberOfQuestionsInLevel >= 0) {
                    editor.putInt("CurrentQuestion" + code, preferences.getInt("CurrentQuestion" + code, 0) - NumberOfQuestionsInLevel);
                }
                if (preferences.getInt("total" + code, 0) - NumberOfQuestionsInLevel >= 0) {
                    editor.putInt("total" + code, preferences.getInt("total" + code, 0) - NumberOfQuestionsInLevel);
                    editor.putInt("total", preferences.getInt("total", 0) - NumberOfQuestionsInLevel);
                    editor.apply();
                }

                editor.putInt("total" + code + "_" + branch, 0);
                editor.putInt("currentLevelQuestion" + code + "_" + branch, 1);


                editor.apply();
//                finish();

                if (isOnline() && user != null && user.getDisplayName() != null) {
                    final DatabaseReference refDirect = FirebaseDatabase.getInstance().getReference();
                    refDirect.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            refDirect.child("Users" + "/" + user.getUid() + "/score").setValue(preferences.getInt("total", 0));
                            refDirect.child("Users" + "/" + user.getUid() + "/data/" + code + "/total").setValue(preferences.getInt("total" + code, 0));
                            refDirect.child("Users" + "/" + user.getUid() + "/data/" + code + "/CurrentQuestion").setValue(CurrentQuestion);
                            refDirect.child("Users" + "/" + user.getUid() + "/data/" + code + "/" + branch + "/currentLevelQuestion").setValue(currentLevelQuestion);
                            refDirect.child("Users" + "/" + user.getUid() + "/data/" + code + "/" + branch + "/total").setValue(preferences.getInt("total" + code + "_" + branch, 0));
                            refDirect.child("Users" + "/" + user.getUid() + "/data/" + code + "/" + branch + "/done").setValue(preferences.getString("done" + "_" + code + "_" + branch, null));

                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            intent.putExtra("code", code);
                            intent.putExtra("branch", branch);
                            startActivity(intent);
                            overridePendingTransition(R.anim.goup, R.anim.godown);

                            dialog.dismiss();
                            buttonReplay.setClickable(false);
                            buttonBack.setClickable(false);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }

                    });
                } else {
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.putExtra("code", code);
                    intent.putExtra("branch", branch);
                    startActivity(intent);
                    overridePendingTransition(R.anim.goup, R.anim.godown);

                    dialog.dismiss();
                    buttonReplay.setClickable(false);
                    buttonBack.setClickable(false);

                }


            }
        });
        buttonBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                currentLevelQuestion = 1;
                CurrentQuestion = 0;
                dialog.dismiss();
                if (preferences.getBoolean("sounds", true) == true) {
                    if (finishPlayer != null && finishPlayer.isPlaying()) {
                        finishPlayer.stop();
                    }
                }
                Intent intent = new Intent(MainActivity.this, LevelsActivity.class);
                intent.putExtra("code", code);

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.goup, R.anim.godown);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                overridePendingTransition(R.anim.goup, R.anim.godown);
            }
        });
        dialog.show();

    }

    public void Failed_Level_Dialog() {
        wrongPlayer = MediaPlayer.create(MainActivity.this, R.raw.wrong);

        if (preferences.getBoolean("sounds", true) == true) {
            wrongPlayer.start();
        }
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.failed_level_dialog_layout);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        // set the custom dialog components - text, image and button
        final Button buttonReplay = dialog.findViewById(R.id.replay_button);
        final Button buttonBack = dialog.findViewById(R.id.back_button);
        TextView textViewTitle = dialog.findViewById(R.id.level_title);
        TextView textViewFailedAverage = dialog.findViewById(R.id.failed_average);
        ImageView imageView = dialog.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.wrong);


        blinkAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        imageView.setAnimation(blinkAnimation);
        Float percentage = ((preferences.getInt("total" + code + "_" + branch, 0) / Float.parseFloat(String.valueOf(NumberOfQuestionsInLevel))) * Float.parseFloat(String.valueOf(100)));

        textViewTitle.setText(getString(R.string.failedLevel) + " ");
        textViewFailedAverage.setText(getString(R.string.failedAverage) + " " + String.format(Locale.ENGLISH, "%.2f", percentage) + "%" + "");
        //Font
        String fontPath = "fonts/NeoSans.ttf";
        Typeface font = Typeface.createFromAsset(getAssets(), fontPath);
        textViewTitle.setTypeface(font);
        textViewFailedAverage.setTypeface(font);
        buttonReplay.setTypeface(font);
        buttonBack.setTypeface(font);

        buttonReplay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                currentLevelQuestion = 1;
                CurrentQuestion = 0;
                wrongPlayer = MediaPlayer.create(MainActivity.this, R.raw.wrong);

                if (preferences.getBoolean("sounds", true) == true) {
                    if (wrongPlayer != null && wrongPlayer.isPlaying()) {
                        wrongPlayer.stop();
                    }
                }

                //*****************Reset Current level insted of all levels in the category**************************//
                if (preferences.getInt("CurrentQuestion" + code, 0) - NumberOfQuestionsInLevel >= 0) {
                    editor.putInt("CurrentQuestion" + code, preferences.getInt("CurrentQuestion" + code, 0) - NumberOfQuestionsInLevel);
                }
                if (preferences.getInt("total" + code, 0) - NumberOfQuestionsInLevel >= 0) {
                    editor.putInt("total" + code, preferences.getInt("total" + code, 0) - NumberOfQuestionsInLevel);
                    editor.putInt("total", preferences.getInt("total", 0) - NumberOfQuestionsInLevel);
                    editor.apply();
                }
                editor.putInt("total" + code + "_" + branch, 0);
                editor.putInt("currentLevelQuestion" + code + "_" + branch, 1);


                editor.apply();

                ///

                //Upload user data
                if (isOnline() && user != null && user.getDisplayName() != null) {
                    final DatabaseReference refDirect = FirebaseDatabase.getInstance().getReference();
                    refDirect.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            refDirect.child("Users" + "/" + user.getUid() + "/score").setValue(preferences.getInt("total", 0));
                            refDirect.child("Users" + "/" + user.getUid() + "/data/" + code + "/total").setValue(preferences.getInt("total" + code, 0));
                            refDirect.child("Users" + "/" + user.getUid() + "/data/" + code + "/CurrentQuestion").setValue(CurrentQuestion);
                            refDirect.child("Users" + "/" + user.getUid() + "/data/" + code + "/" + branch + "/currentLevelQuestion").setValue(currentLevelQuestion);
                            refDirect.child("Users" + "/" + user.getUid() + "/data/" + code + "/" + branch + "/total").setValue(preferences.getInt("total" + code + "_" + branch, 0));
                            refDirect.child("Users" + "/" + user.getUid() + "/data/" + code + "/" + branch + "/done").setValue(preferences.getString("done" + "_" + code + "_" + branch, null));
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            intent.putExtra("code", code);
                            intent.putExtra("branch", branch);
                            startActivity(intent);
                            overridePendingTransition(R.anim.goup, R.anim.godown);

                            dialog.dismiss();

                            buttonReplay.setClickable(false);
                            buttonBack.setClickable(false);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }

                    });
                } else {
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.putExtra("code", code);
                    intent.putExtra("branch", branch);
                    startActivity(intent);
                    overridePendingTransition(R.anim.goup, R.anim.godown);

                    dialog.dismiss();
                    buttonReplay.setClickable(false);
                    buttonBack.setClickable(false);

                }
                ///


            }
        });
        buttonBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                currentLevelQuestion = 1;
                CurrentQuestion = 0;
                dialog.dismiss();
                if (preferences.getBoolean("sounds", true) == true) {
                    if (finishPlayer != null && finishPlayer.isPlaying()) {
                        finishPlayer.stop();
                    }
                }
                Intent intent = new Intent(MainActivity.this, LevelsActivity.class);
                intent.putExtra("code", code);

                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.goup, R.anim.godown);
            }
        });
        dialog.show();

    }

    public void Reveal_Answer_Dialog() {

        final Dialog dialogReveal = new Dialog(MainActivity.this);
        dialogReveal.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogReveal.setContentView(R.layout.reveal_letter_dialog_layout);
        dialogReveal.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialogReveal.setCancelable(false);

        final ImageView imageView = dialogReveal.findViewById(R.id.imageView);
        final Button buttonShow = dialogReveal.findViewById(R.id.show);
        Button buttonBack = dialogReveal.findViewById(R.id.back);
        TextView textView = dialogReveal.findViewById(R.id.text);
        final ProgressBar loadingAdProgress = dialogReveal.findViewById(R.id.loading_ad_progress);

        blinkAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        imageView.setAnimation(blinkAnimation);
        //Font
        String fontPath = "fonts/NeoSans.ttf";
        Typeface font = Typeface.createFromAsset(getAssets(), fontPath);
        textView.setTypeface(font);
        buttonShow.setTypeface(font);
        buttonBack.setTypeface(font);

        buttonShow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                buttonShow.setEnabled(false);
                showAnswer = true;
                skipPressed = false;
                if ((isOnline())) {
                    if (rewardedAd != null) {
                        loadingAdProgress.setVisibility(View.GONE);
                        showRewardedVideo();
                        dialogReveal.dismiss();


                    } else {
                        loadingAdProgress.setVisibility(View.VISIBLE);
                        loadRewardedAd();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
//                                buttonShow.performClick();
                            }
                        }, 6000); // 3000 milliseconds delay
                    }
                } else {
                    Snackbar.make(buttonShow, getString(R.string.checkInternet), Snackbar.LENGTH_SHORT).show();
                }

            }
        });
        buttonBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogReveal.dismiss();
                if (isRunning == true) {
                    countDownTimer.start();
                }
            }
        });
        dialogReveal.show();


    }

    public void Skip_Dialog() {

        final Dialog dialogReveal = new Dialog(MainActivity.this);
        dialogReveal.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogReveal.setContentView(R.layout.skip_dialog_layout);
        dialogReveal.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialogReveal.setCancelable(false);

        final ImageView imageView = dialogReveal.findViewById(R.id.imageView);
        final Button buttonShow = dialogReveal.findViewById(R.id.show);
        Button buttonBack = dialogReveal.findViewById(R.id.back);
        TextView textView = dialogReveal.findViewById(R.id.text);
        final ProgressBar loadingAdProgress = dialogReveal.findViewById(R.id.loading_ad_progress);

        blinkAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        imageView.setAnimation(blinkAnimation);
        //Font
        String fontPath = "fonts/NeoSans.ttf";
        Typeface font = Typeface.createFromAsset(getAssets(), fontPath);
        textView.setTypeface(font);
        buttonShow.setTypeface(font);
        buttonBack.setTypeface(font);

        buttonShow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                buttonShow.setEnabled(false);
                skipPressed = true;
                showAnswer = false;
                if ((isOnline())) {
                    if (rewardedAd != null) {
                        loadingAdProgress.setVisibility(View.GONE);
                        showRewardedVideo();
                        dialogReveal.dismiss();


                    } else {
                        loadingAdProgress.setVisibility(View.VISIBLE);
                        loadRewardedAd();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
//                                buttonShow.performClick();
                            }
                        }, 6000); // 3000 milliseconds delay
                    }
                } else {
                    Snackbar.make(buttonShow, getString(R.string.checkInternet), Snackbar.LENGTH_SHORT).show();
                }

            }
        });
        buttonBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogReveal.dismiss();

            }
        });
        dialogReveal.show();

    }

    public void Correct_Dialog() {

        if (preferences.getBoolean("sounds", true) == true) {
            correctPlayer = MediaPlayer.create(MainActivity.this, R.raw.correct);
            correctPlayer.start();
        }
        final Dialog dialogCorrect = new Dialog(MainActivity.this);
        dialogCorrect.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogCorrect.setContentView(R.layout.correct_dialog_layout);
        dialogCorrect.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialogCorrect.setCancelable(false);

        final ImageView imageView = dialogCorrect.findViewById(R.id.imageView);
        final Button buttonContinue = dialogCorrect.findViewById(R.id.continue_button);
        TextView textView_perfect = dialogCorrect.findViewById(R.id.perfect);
        TextView textView = dialogCorrect.findViewById(R.id.answer);

        textView.setText(OriginalAnswer);
        //Font
        String fontPath = "fonts/NeoSans.ttf";
        Typeface font = Typeface.createFromAsset(getAssets(), fontPath);
        textView.setTypeface(font);
        buttonContinue.setTypeface(font);
        textView_perfect.setTypeface(font);
        textView.setTypeface(font);

        blinkAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        imageView.setAnimation(blinkAnimation);
        buttonContinue.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                retrieveData();
                if (correctPlayer != null && correctPlayer.isPlaying()) {
                    correctPlayer.stop();
                }
                dialogCorrect.dismiss();

            }
        });

        dialogCorrect.show();

    }

    public void Wrong_Dialog() {
        wrongPlayer = MediaPlayer.create(MainActivity.this, R.raw.wrong);

        if (preferences.getBoolean("sounds", true) == true) {
            wrongPlayer.start();
        }
        dialogWrong = new Dialog(MainActivity.this);
        dialogWrong.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogWrong.setContentView(R.layout.wrong_dialog_layout);
        dialogWrong.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialogWrong.setCancelable(false);

        final ImageView imageView = dialogWrong.findViewById(R.id.imageView);
        final Button buttonTryAgain = dialogWrong.findViewById(R.id.try_again_button);
        //Font
        String fontPath = "fonts/NeoSans.ttf";
        Typeface font = Typeface.createFromAsset(getAssets(), fontPath);
        buttonTryAgain.setTypeface(font);

        blinkAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        imageView.setAnimation(blinkAnimation);
        buttonTryAgain.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                retrieveData();
                if (wrongPlayer != null && wrongPlayer.isPlaying()) {
                    wrongPlayer.stop();
                }
                dialogWrong.dismiss();

            }
        });

        dialogWrong.show();

    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }


    public void retrieveNumberOfQuestions() {
        if (isOnline()) {
            Query query = FirebaseDatabase.getInstance().getReference("data").child(String.valueOf(code)).child(String.valueOf(branch)).orderByKey();
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    NumberOfQuestionsInLevel = (int) dataSnapshot.getChildrenCount();
                    android.util.Log.d("NumberOfQLevel", NumberOfQuestionsInLevel + "-");
                    enableViews();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            Toast.makeText(this, getString(R.string.checkInternet) + "", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void enableViews() {
        if (NumberOfQuestionsInLevel > 0) {
            revealButton.setOnClickListener(this);
            askButton.setOnClickListener(this);
            resetButton.setOnClickListener(this);
            skipButton.setOnClickListener(this);
            doneButton.setOnClickListener(this);


        } else {
            Toast.makeText(this, getString(R.string.nodata) + "", Toast.LENGTH_LONG).show();
        }
    }

    public void retrieveData() {
//        if (isRunning == false) {
//            if (skipPressed == false) {
//                NewCountDown();
//            }
//        }
        QuestionText = "";
        AnswerText = "";
        OriginalAnswer = "";
        ImageURL = "";
        VoiceURL = "";
        spacePosition.clear();
        if (isOnline()) {


            Query query = FirebaseDatabase.getInstance().getReference("data").child(String.valueOf(code)).child(String.valueOf(branch)).orderByKey();
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    QuestionText = dataSnapshot.child(currentLevelQuestion + "/" + "q").getValue(String.class);
                    AnswerText = dataSnapshot.child(currentLevelQuestion + "/" + "a").getValue(String.class);
                    ImageURL = dataSnapshot.child(currentLevelQuestion + "/" + "i").getValue(String.class);
                    VoiceURL = dataSnapshot.child(currentLevelQuestion + "/" + "s").getValue(String.class);
                    OriginalAnswer = AnswerText;
                    //==========Voice question==========//
                    if (VoiceURL != null) {
                        // Detect  sound volume
                        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) < 3) {
                            Toast.makeText(MainActivity.this, getString(R.string.volume) + "", Toast.LENGTH_LONG).show();
                        }

                        try {
                            voiceImage.setVisibility(View.VISIBLE);
                            voiceImage.setEnabled(false);

                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(VoiceURL);
                            mediaPlayer.prepare(); // might take long! (for buffering, etc)
                            mediaPlayer.start();
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    voiceImage.setBackgroundResource(R.drawable.speaker_background);
                                    voiceImage.setEnabled(true);
                                }
                            });
                            voiceImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (mediaPlayer.isPlaying() == false) {
                                        voiceImage.setBackgroundResource(R.drawable.border_background);
                                        voiceImage.setEnabled(false);
                                        mediaPlayer.start();
                                    }
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else if (VoiceURL == null) {
                        voiceImage.setVisibility(View.GONE);
                    }
                    //==========================================//
                    NextQuestion();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
        } else {
            Snackbar.make(doneButton, getString(R.string.checkInternet), Snackbar.LENGTH_SHORT).show();
            finish();
        }
    }

    public void retrieveQuestionTile() {

        if (isOnline()) {
            Query query = FirebaseDatabase.getInstance().getReference("categories").orderByKey();
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(String.valueOf(code)).child("n").getValue() != null) {
                        LevelNumberTV.setText(dataSnapshot.child(String.valueOf(code)).child("n").getValue() + " - " + branch);
                    } else {
                        LevelNumberTV.setText(getString(R.string.level) + " " + code + " - " + branch);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });
        } else {
            Snackbar.make(doneButton, getString(R.string.checkInternet), Snackbar.LENGTH_SHORT).show();
            finish();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        readyForReview = true;
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
        if (isRunning == true) {
            countDownTimer.cancel();
        }
        if (tickPlayer != null && tickPlayer.isPlaying()) {
            tickPlayer.stop();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();


        Intent i2 = new Intent(MainActivity.this, LevelsActivity.class);
        i2.putExtra("code", code);
        i2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i2);
        finish();
        overridePendingTransition(R.anim.goup, R.anim.godown);
    }

    public void NewCountDown() {

//TODO retrieve the time from the database
        if (timeInSeconds != null) {
            if (Integer.parseInt(timeInSeconds) > 0) {
                progressBar.setMax(Integer.parseInt(timeInSeconds));
                progressBar.setProgress(Integer.parseInt(timeInSeconds));

                countDownTimer = new CountDownTimer(Integer.parseInt(timeInSeconds) * 1000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        isRunning = true;
                        progressBar.setProgress(progressBar.getProgress() - 1);
                        if (progressBar.getProgress() == 5) {
                            tickPlayer = MediaPlayer.create(MainActivity.this, R.raw.tick);
                            if (preferences.getBoolean("sounds", true) == true) {
                                tickPlayer.start();
                            }

                        }
                    }

                    public void onFinish() {
                        //prepear next question save current answer
                        isRunning = false;
                        if (!((MainActivity.this).isFinishing())) {
                            Timeout_Dialog();
                        }


                    }

                }.start();
            } else {
                progressBar.setVisibility(View.GONE);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) LevelNumberTV.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                params.addRule(RelativeLayout.CENTER_VERTICAL, 1);
                LevelNumberTV.setLayoutParams(params);
            }
        } else {
            progressBar.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) LevelNumberTV.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            params.addRule(RelativeLayout.CENTER_VERTICAL, 1);
            LevelNumberTV.setLayoutParams(params);
        }
    }

    public void Timeout_Dialog() {
        if (isRunning == true) {
            countDownTimer.cancel();
        }
        isRunning = false;
        timeoutPlayer = MediaPlayer.create(MainActivity.this, R.raw.timeout);
        if (preferences.getBoolean("sounds", true) == true) {
            timeoutPlayer.start();
        }
        if (dialogWrong != null) {
            if (dialogWrong.isShowing()) {
                dialogWrong.dismiss();
            }
        }
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.timeout_dialog_layout);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        // set the custom dialog components - text, image and button
        final Button buttonClose = dialog.findViewById(R.id.continue_button);
        TextView textViewTimeoutTitle = dialog.findViewById(R.id.timeout_title);
        TextView textViewCorrect = dialog.findViewById(R.id.correct_answer);
        ImageView imageView = dialog.findViewById(R.id.imageView);

        clockwiseAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.clockwise);
        imageView.setAnimation(clockwiseAnimation);

        textViewCorrect.setText(OriginalAnswer);

        //Font
        String fontPath = "fonts/NeoSans.ttf";
        Typeface font = Typeface.createFromAsset(getAssets(), fontPath);
        textViewTimeoutTitle.setTypeface(font);
        textViewCorrect.setTypeface(font);
        buttonClose.setTypeface(font);

        buttonClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                CurrentQuestion = CurrentQuestion + 1;
                currentLevelQuestion = currentLevelQuestion + 1;
                int total = preferences.getInt("total" + code + "_" + branch, 0);
                if (total > 0) {
                    editor.putInt("total" + code, preferences.getInt("total" + code, 0) - 1);
                    editor.putInt("total" + code + "_" + branch, preferences.getInt("total" + code + "_" + branch, 0) - 1);
                    editor.putInt("total", preferences.getInt("total", 0) - 1);

                    editor.apply();
                    scoreTV.setText(preferences.getInt("total" + code + "_" + branch, 0) + "");
                }
                retrieveData();
                dialog.dismiss();


            }
        });
        dialog.show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRunning == true) {
            countDownTimer.cancel();
            if (tickPlayer != null && tickPlayer.isPlaying()) {
                tickPlayer.stop();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRunning == true) {
            countDownTimer.start();
        }
        if (isRunning == false) {
            if (skipped == true) {
                NewCountDown();
                skipped = false;
            }
        }

    }

    private void loadRewardedAd() {
        if (rewardedAd == null) {
            isLoading = true;
            AdRequest adRequest = new AdRequest.Builder().build();

            RewardedAd.load(
                    this,
                    getString(R.string.rewardedVideo_ad_unit_id),
                    adRequest,
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d(TAG, loadAdError.getMessage());
                            rewardedAd = null;
                            MainActivity.this.isLoading = false;
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            MainActivity.this.rewardedAd = rewardedAd;
                            Log.d(TAG, "onAdLoaded");
                            MainActivity.this.isLoading = false;
                        }
                    });
        }
    }

    private void showRewardedVideo() {

        if (rewardedAd == null) {
            Log.d("TAG", "The rewarded ad wasn't ready yet.");
            return;
        }

        rewardedAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Called when ad is shown.
                        Log.d(TAG, "onAdShowedFullScreenContent");

                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        Log.d(TAG, "onAdFailedToShowFullScreenContent");
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null;

                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null;
                        Log.d(TAG, "onAdDismissedFullScreenContent");

                        // Preload the next rewarded ad.
                        MainActivity.this.loadRewardedAd();
                    }
                });
        Activity activityContext = MainActivity.this;
        rewardedAd.show(
                activityContext,
                new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        Log.d("TAG", "The user earned the reward.");
                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();
                        if (showAnswer == true) {
                            currentLetter = 0;

                            for (int i = 0; i < answer.length; i++) {
                                String answerLetter = String.valueOf(answer[i]);
                                char compare = answerLetter.charAt(0);
                                user_submit_answer[currentLetter] = compare;
                                if (currentLetter + 1 < answer.length) {
                                    currentLetter = currentLetter + 1;
                                }

                                //Update UI
                                recyclerAnswersAdapter = new RecyclerAnswersAdapter(user_submit_answer, MainActivity.this);
                                answerGridview.setAdapter(recyclerAnswersAdapter);
                                recyclerAnswersAdapter.notifyDataSetChanged();
                            }

                            Toast.makeText(MainActivity.this, getString(R.string.reveald), Toast.LENGTH_SHORT).show();
                        } else if (skipPressed == true) {
                            skipped = true;
                            CurrentQuestion = CurrentQuestion + 1;
                            currentLevelQuestion++;
                            editor.putInt("CurrentQuestion" + code, CurrentQuestion);
                            editor.putInt("currentLevelQuestion" + code + "_" + branch, currentLevelQuestion);

                            editor.putInt("total" + code, preferences.getInt("total" + code, 0) - Integer.parseInt(getString(R.string.skip_score)));
                            editor.putInt("total" + code + "_" + branch, preferences.getInt("total" + code + "_" + branch, 0) - Integer.parseInt(getString(R.string.skip_score)));
                            editor.putInt("total", preferences.getInt("total", 0) - Integer.parseInt(getString(R.string.skip_score)));

                            editor.apply();

                            retrieveData();
                            scoreTV.setText(preferences.getInt("total" + code + "_" + branch, 0) + "");

                        }

                    }
                });
    }

    /********************************************************************************************/

    //Choices Adapter
    public class RecyclerChoicesAdapter extends RecyclerView.Adapter<ChoicesAdapterHolder> {
        private final List<String> suggestSource;
        private final Context context;
        private final MainActivity mainActivity;
        private final LayoutInflater mInflater;


        public RecyclerChoicesAdapter(List<String> suggestSource, Context context, MainActivity mainActivity) {
            this.suggestSource = suggestSource;
            this.context = context;
            this.mainActivity = mainActivity;
            this.mInflater = LayoutInflater.from(context);

        }

        //INITIALIZE VH
        @Override
        public ChoicesAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.choice_answer_item, parent, false);
            return new ChoicesAdapterHolder(view);

        }

        //BIND DATA
        @SuppressLint("WrongConstant")
        @Override
        public void onBindViewHolder(final ChoicesAdapterHolder holder, @SuppressLint("RecyclerView") final int position) {
            if (suggestSource.get(position).equals("null") || suggestSource.get(position).equals(" ")) {
                holder.button.setPadding(0, 0, 0, 0);
                holder.button.setBackgroundColor(Color.TRANSPARENT);
                ///=================///

                Log.d("dddddddd", dm.widthPixels + ">>" + dm.heightPixels);
                if (dm.widthPixels >= 720 && dm.widthPixels < 1080 && MainActivity.this.getResources().getConfiguration().orientation == 1) {
                    //dm.widthPixels >= 600 ||
                    holder.button.setMinimumHeight(50);
                    holder.button.setMinimumWidth(50);
                    holder.button.setTextSize(15);
                    holder.button.setHeight(50);
                    holder.button.setWidth(50);
                } else {
                    if (dm.widthPixels == 1440 && dm.heightPixels >= 2000) {
                        holder.button.setTextSize(25);
                        holder.button.setHeight(130);
                        holder.button.setWidth(130);
                    } else {

//                        if (dm.widthPixels > 400 && dm.widthPixels < 600) {
                        if (dm.widthPixels > 400 && dm.widthPixels <= 600) {
                            holder.button.setMinimumHeight(30);
                            holder.button.setMinimumWidth(30);
                            holder.button.setTextSize(14);
                            holder.button.setHeight(30);
                            holder.button.setWidth(30);
                        } else if (dm.heightPixels >= 1794) {
                            holder.button.setMinimumHeight(100);
                            holder.button.setMinimumWidth(100);
                            holder.button.setTextSize(25);
                            holder.button.setHeight(100);
                            holder.button.setWidth(100);

                        } else if (dm.widthPixels >= 1080 && dm.heightPixels < 1794 && MainActivity.this.getResources().getConfiguration().orientation == 1) {
                            holder.button.setMinimumHeight(60);
                            holder.button.setMinimumWidth(60);
                            holder.button.setTextSize(15);
                            holder.button.setHeight(60);
                            holder.button.setWidth(60);

                        } else if (dm.widthPixels >= 1080 && dm.heightPixels < 1794 && MainActivity.this.getResources().getConfiguration().orientation == 2) {
                            holder.button.setMinimumHeight(80);
                            holder.button.setMinimumWidth(80);
                            holder.button.setTextSize(20);
                            holder.button.setHeight(80);
                            holder.button.setWidth(80);
                        } else {
                            holder.button.setTextSize(14);
                            holder.button.setMinimumHeight(50);
                            holder.button.setMinimumWidth(50);
                            holder.button.setHeight(50);
                            holder.button.setWidth(50);

                        }
                    }
                }
                ///=================///
            } else {
                holder.button.setPadding(0, 0, 0, 0);
                holder.button.setBackgroundResource(R.drawable.choice_item);
                holder.button.setTextColor(getResources().getColor(R.color.white));
                ///=================///
                holder.button.setAllCaps(true);
                holder.button.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
                if (dm.widthPixels >= 720 && dm.widthPixels < 1080 && MainActivity.this.getResources().getConfiguration().orientation == 1) {
                    holder.button.setMinimumHeight(50);
                    holder.button.setMinimumWidth(50);
                    holder.button.setTextSize(15);
                    holder.button.setHeight(50);
                    holder.button.setWidth(50);
                } else {
                    if (dm.widthPixels == 1440 && dm.heightPixels >= 2000) {
                        holder.button.setTextSize(25);
                        holder.button.setHeight(130);
                        holder.button.setWidth(130);
                    } else {

//                        if (dm.widthPixels > 400 && dm.widthPixels < 600) {
                        if (dm.widthPixels > 400 && dm.widthPixels <= 600) {
                            holder.button.setMinimumHeight(30);
                            holder.button.setMinimumWidth(30);
                            holder.button.setTextSize(14);
                            holder.button.setHeight(30);
                            holder.button.setWidth(30);
                        }

                        //  else if (dm.heightPixels >= 1080) {
//                            holder.button.setMinimumHeight(100);
//                            holder.button.setMinimumWidth(100);
//                            holder.button.setTextSize(25);
//                            holder.button.setHeight(100);
//                            holder.button.setWidth(100);
                        else if (dm.heightPixels >= 1794) {
                            holder.button.setMinimumHeight(100);
                            holder.button.setMinimumWidth(100);
                            holder.button.setTextSize(25);
                            holder.button.setHeight(100);
                            holder.button.setWidth(100);
                        } else if (dm.widthPixels >= 1080 && dm.heightPixels < 1794 && MainActivity.this.getResources().getConfiguration().orientation == 1) {
                            holder.button.setMinimumHeight(60);
                            holder.button.setMinimumWidth(60);
                            holder.button.setTextSize(15);
                            holder.button.setHeight(60);
                            holder.button.setWidth(60);
                        } else if (dm.widthPixels >= 1080 && dm.heightPixels < 1794 && MainActivity.this.getResources().getConfiguration().orientation == 2) {
                            holder.button.setMinimumHeight(80);
                            holder.button.setMinimumWidth(80);
                            holder.button.setTextSize(20);
                            holder.button.setHeight(80);
                            holder.button.setWidth(80);

                        } else {
                            holder.button.setTextSize(14);
                            holder.button.setMinimumHeight(50);
                            holder.button.setMinimumWidth(50);
                            holder.button.setHeight(50);
                            holder.button.setWidth(50);
                        }
                    }
                }
                ///=================///
                holder.button.setText(suggestSource.get(position));
                holder.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickPlayer = MediaPlayer.create(MainActivity.this, R.raw.click);
                        if (String.valueOf(user_submit_answer[currentLetter]).trim().length() == 0 && currentLetter < user_selected_choices_positions.size() + 1) {

                            ///////////////////////////////////////////////
                            if (clickPlayer != null && clickPlayer.isPlaying()) {
                                clickPlayer.stop();
                            }
                            if (preferences.getBoolean("sounds", true) == true) {
                                clickPlayer.start();
                            }
                            //If correct answer contains character user selected
                            char compare = suggestSource.get(position).charAt(0);
                            //////////////////////////////////////////////////////////////////////////////////
//                            if (user_selected_choices_positions.size() == answer.length && !user_selected_choices_positions.get(currentLetter).equals("")) {
                            //============ Fixing the overwriting issue 31/1/2021 =========//
                            if (currentLetter < user_selected_choices_positions.size()) {
                                user_selected_choices_positions.set(currentLetter, position);
                            } else {
                                user_selected_choices_positions.add(position);
                            }
//                            Log.d("testtest"+user_selected_choices_positions.size()+">>"+answer.length);
                            // }
                            /////////////////////////////////////////////////////////////////////////////////////

                            user_submit_answer[currentLetter] = compare;
                            if (currentLetter + 1 < answer.length) {
                                currentLetter = currentLetter + 1;
                            }


                            //Update UI
                            recyclerAnswersAdapter = new RecyclerAnswersAdapter(user_submit_answer, context);
                            answerGridview.setAdapter(recyclerAnswersAdapter);
                            recyclerAnswersAdapter.notifyDataSetChanged();

                            //Remove from suggest source
                            choicesLetters.set(position, "null");
                            recyclerChoicesAdapter = new RecyclerChoicesAdapter(choicesLetters, context, mainActivity);
                            choicesGridview.setAdapter(recyclerChoicesAdapter);
                            recyclerChoicesAdapter.notifyDataSetChanged();
                        } else {
//
                            if (currentLetter >= user_selected_choices_positions.size() + 1) {
                                Toast.makeText(context, getString(R.string.empty_fields) + "", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(context, getString(R.string.select_any_letter) + "", Toast.LENGTH_SHORT).show();
                            }
//                        }
                        }
                    }

                });
            }
        }
/////////////


        //TOTAL NUM TVSHOWS
        @Override
        public int getItemCount() {
            return suggestSource.size();
        }


    }

    public class ChoicesAdapterHolder extends RecyclerView.ViewHolder {
        Button button;

        public ChoicesAdapterHolder(View itemView) {
            super(itemView);

            button = itemView.findViewById(R.id.choiceItem);


        }
    }

    //Answer Adapter
    public class RecyclerAnswersAdapter extends RecyclerView.Adapter<AnswersAdapterHolder> {
        private final char[] answerCharacter;
        private final Context context;
        private final LayoutInflater mInflater;


        public RecyclerAnswersAdapter(char[] answerCharacter, Context context) {
            this.answerCharacter = answerCharacter;
            this.context = context;
            this.mInflater = LayoutInflater.from(context);


        }

        //INITIALIZE VH
        @Override
        public AnswersAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.choice_answer_item, parent, false);
            return new AnswersAdapterHolder(view);


        }

        //BIND DATA
        @SuppressLint("WrongConstant")
        @Override
        public void onBindViewHolder(final AnswersAdapterHolder holder, @SuppressLint("RecyclerView") final int position) {
            //=====================Adding space between words=======================//
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 100, 0);
            for (int i = 0; i < spacePosition.size(); i++) {
                if (spacePosition.get(i) == position) {
                    holder.button.setLayoutParams(params);
                    Log.d("fffffffff", spacePosition.get(i) + "");
                }
            }

            //======================================================================//
            holder.button.setPadding(0, 0, 0, 0);
            holder.button.setBackgroundResource(R.drawable.answer_item);
            holder.button.setTextColor(getResources().getColor(R.color.white));

            ///=================///
            holder.button.setAllCaps(true);
            holder.button.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
//            if (dm.widthPixels >= 800 && MainActivity.this.getResources().getConfiguration().orientation == 1) {
            if (dm.widthPixels >= 720 && dm.widthPixels < 1080 && MainActivity.this.getResources().getConfiguration().orientation == 1) {
                holder.button.setMinimumHeight(50);
                holder.button.setMinimumWidth(50);
                holder.button.setTextSize(15);
                holder.button.setHeight(50);
                holder.button.setWidth(50);
            } else {
                if (dm.widthPixels == 1440 && dm.heightPixels >= 2000) {
                    holder.button.setTextSize(25);
                    holder.button.setHeight(130);
                    holder.button.setWidth(130);
                } else {

                    if (dm.widthPixels > 400 && dm.widthPixels <= 600) {
                        holder.button.setMinimumHeight(30);
                        holder.button.setMinimumWidth(30);
                        holder.button.setTextSize(14);
                        holder.button.setHeight(30);
                        holder.button.setWidth(30);
                    } else if (dm.heightPixels >= 1794) {
                        holder.button.setMinimumHeight(100);
                        holder.button.setMinimumWidth(100);
                        holder.button.setTextSize(25);
                        holder.button.setHeight(100);
                        holder.button.setWidth(100);
                    } else if (dm.widthPixels >= 1080 && dm.heightPixels < 1794 && MainActivity.this.getResources().getConfiguration().orientation == 1) {
                        holder.button.setMinimumHeight(60);
                        holder.button.setMinimumWidth(60);
                        holder.button.setTextSize(15);
                        holder.button.setHeight(60);
                        holder.button.setWidth(60);
                    } else if (dm.widthPixels >= 1080 && dm.heightPixels < 1794 && MainActivity.this.getResources().getConfiguration().orientation == 2) {
                        holder.button.setMinimumHeight(80);
                        holder.button.setMinimumWidth(80);
                        holder.button.setTextSize(20);
                        holder.button.setHeight(80);
                        holder.button.setWidth(80);
                    } else {
                        holder.button.setTextSize(14);
                        holder.button.setMinimumHeight(50);
                        holder.button.setMinimumWidth(50);
                        holder.button.setHeight(50);
                        holder.button.setWidth(50);
                    }
                }
            }


            holder.button.setText(String.valueOf(answerCharacter[position]));
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentLetter = position;
                    //position<user_selected_choices_positions.size()&&

                    //avoid crashing if the duplicated letter gone
                    if (user_selected_choices_positions.size() > position) {
                        if (choicesLetters.size() > user_selected_choices_positions.get(position)) {

                            if (showAnswer == false && holder.button.getText().toString().trim().length() > 0 && !String.valueOf(answerCharacter[position]).equals(" ") && !String.valueOf(answerCharacter[position]).equals(null)) {


                                choicesLetters.set(user_selected_choices_positions.get(position), String.valueOf(answerCharacter[position]));
                                choicesGridview.setAdapter(recyclerChoicesAdapter);
                                recyclerChoicesAdapter.notifyDataSetChanged();

////////////////////////////////////////////////////////////////////////////
                                holder.button.setText("");
                                currentLetter = position;

                                String test = " ";
                                user_submit_answer[currentLetter] = test.charAt(0);

                                recyclerAnswersAdapter = new RecyclerAnswersAdapter(user_submit_answer, context);
                                answerGridview.setAdapter(recyclerAnswersAdapter);
                                recyclerAnswersAdapter.notifyDataSetChanged();


                            }
                        } else {
                            android.util.Log.d("Error", "Error****");
                        }
                    }
                }

            });
        }

        //TOTAL NUM TVSHOWS
        @Override
        public int getItemCount() {
            return answerCharacter.length;
        }


    }

    public class AnswersAdapterHolder extends RecyclerView.ViewHolder {
        Button button;

        public AnswersAdapterHolder(View itemView) {
            super(itemView);

            button = itemView.findViewById(R.id.choiceItem);


        }
    }


}
