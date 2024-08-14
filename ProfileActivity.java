package com.superquiz.easyquiz.triviastar;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

 


public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int SELECT_PICTURE = 100;
    TextView userName, userEmail;
    ImageView user_image;
    FloatingActionButton upload_Image;
    // creating an instance of Firebase Storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    //creating a storage reference. Replace the below URL with your Firebase storage URL.
    StorageReference storageRef;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String name, email, uid;
    Uri photoUrl;
    AdView mAdView;
    SharedPreferences preferences;
    TextView scoreTitle, scoreTotal;
    Button loginSignoutButton;
    TextView deleteAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this);

        setContentView(R.layout.activity_profile);
        getSupportActionBar().setTitle(getString(R.string.profile_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        storageRef = storage.getReferenceFromUrl(getString(R.string.usersProfileURL) + "/usersProfile");
        userName = (TextView) findViewById(R.id.profile_name);
        user_image = (ImageView) findViewById(R.id.profile_image);
        upload_Image = (FloatingActionButton) findViewById(R.id.upload_image);
        scoreTitle = (TextView) findViewById(R.id.scoreTitle);
        scoreTotal = (TextView) findViewById(R.id.scores);
        loginSignoutButton = (Button) findViewById(R.id.loginSignout);
        deleteAccount = (TextView) findViewById(R.id.deleteAccount);

        //Font
        String fontPath = "fonts/NeoSans.ttf";
        Typeface font = Typeface.createFromAsset(getAssets(), fontPath);
        userName.setTypeface(font);
        scoreTitle.setTypeface(font);
        scoreTotal.setTypeface(font);
        loginSignoutButton.setTypeface(font);


        userEmail = (TextView) findViewById(R.id.profile_email);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            name = user.getDisplayName();
            email = user.getEmail();
            photoUrl = user.getPhotoUrl();
            uid = user.getUid();
            loginSignoutButton.setText(getString(R.string.logout));
            deleteAccount.setVisibility(View.VISIBLE);
            deleteAccount.setTypeface(font,Typeface.BOLD);
            SpannableString content = new SpannableString(getString(R.string.deleteButton));
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            deleteAccount.setText(content);
        } else {
            loginSignoutButton.setText(getString(R.string.loginTitle));
            deleteAccount.setVisibility(View.GONE);

        }


        if (name != null) {
            userName.setText(name);
        } else {
            userName.setText("");
        }

        if (email != null) {
            userEmail.setText(email);
        }

        scoreTotal.setText(preferences.getInt("total", 0) + "");

        userName.setOnClickListener(this);
        user_image.setOnClickListener(this);
        loginSignoutButton.setOnClickListener(this);
        deleteAccount.setOnClickListener(this);


        if (photoUrl != null) {
            Glide.with(this).load(photoUrl).into(user_image);

        } else {
            Glide.with(this).load(R.drawable.profile).into(user_image);
        }
        onUploadButtonClick(); // for uploading the image to Firebase Storage.


        //AdMob Ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View v) {
        if (user != null) {

            switch (v.getId()) {

                case R.id.profile_name:
                    changeName();
                    break;


                case R.id.profile_image:
                    chooseImage();
                    break;

                case R.id.loginSignout:
                    if (user != null) {
                        //signout
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(this, getString(R.string.logout), Toast.LENGTH_SHORT).show();
                        Intent i6 = new Intent(ProfileActivity.this, LoginActivity.class);
                        i6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i6);
                    } else {

                    }
                    break;

                case R.id.deleteAccount:
                    deleteAccount();
                    break;

            }
        } else {
            if (v.getId() == R.id.loginSignout) {
                //login
                Intent i = new Intent(ProfileActivity.this, SplashActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            } else {
                Snackbar snackbar = Snackbar
                        .make(this.findViewById(android.R.id.content), getString(R.string.please_signin), Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.signin_action), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent i = new Intent(ProfileActivity.this, SplashActivity.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(i);
                                    }
                                }
                        );
                snackbar.show();
            }
        }

    }


    @SuppressLint("RestrictedApi")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    //Leenah: this line to avoid black imageView when load large camera photo
                    user_image.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    // Get the path from the Uri
                    String path = getPathFromURI(selectedImageUri);
                    Log.i("IMAGE PATH TAG", "Image Path : " + path);
                    // Set the image in ImageView

                    user_image.setImageURI(selectedImageUri);
                    upload_Image.setVisibility(View.VISIBLE);

                }
            }
        }
    }

    private String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    protected void onUploadButtonClick() {

        upload_Image.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                // Creating a reference to the full path of the file. myfileRef now points
                user_image.setClickable(false);
                upload_Image.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, getString(R.string.please_wait) + "", Toast.LENGTH_LONG).show();

                StorageReference myfileRef = storageRef.child("userPhoto.png" + user.getUid());
                user_image.setDrawingCacheEnabled(true);
                user_image.buildDrawingCache();
                Bitmap bitmap = user_image.getDrawingCache();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 20, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = myfileRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(ProfileActivity.this, getString(R.string.imageNotUploaded), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(ProfileActivity.this, getString(R.string.imageUploaded), Toast.LENGTH_SHORT).show();
                        Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @SuppressLint("RestrictedApi")
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setPhotoUri(Uri.parse(downloadUrl)).build();
                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    //Update user image link in the database
//
                                                    final DatabaseReference refDirect = FirebaseDatabase.getInstance().getReference();
                                                    refDirect.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            refDirect.child("Users").child(user.getUid() + "/image").setValue(user.getPhotoUrl().toString());
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {
                                                        }
                                                    });

                                                }
                                            }
                                        });
                                user_image.setClickable(true);


                            }
                        });

                    }
                });
            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void chooseImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, getString(R.string.chooseImage)), SELECT_PICTURE);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // change user name
    public void changeName() {
        if (isOnline()) {
            final Dialog dialogChangeName = new Dialog(ProfileActivity.this);
            dialogChangeName.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogChangeName.setContentView(R.layout.change_name_dialog_layout);
            dialogChangeName.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialogChangeName.setCancelable(false);

            final ImageView imageView = dialogChangeName.findViewById(R.id.imageView);
            final Button buttonChange = dialogChangeName.findViewById(R.id.change);
            Button buttonBack = dialogChangeName.findViewById(R.id.back);
            final EditText editText = dialogChangeName.findViewById(R.id.nameFiled);

            imageView.setImageResource(R.drawable.small_logo_tranparent);
            buttonChange.setText(getString(R.string.changename));

            //Font
            String fontPath = "fonts/NeoSans.ttf";
            Typeface font = Typeface.createFromAsset(getAssets(), fontPath);
            editText.setTypeface(font);
            buttonChange.setTypeface(font);
            buttonBack.setTypeface(font);

            buttonChange.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    buttonChange.setEnabled(false);

                    if ((isOnline())) {
                        if (!editText.getText().toString().trim().equals("")) {

                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(ProfileActivity.this.userName.getWindowToken(), 0);

                            final UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(editText.getText().toString()).build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ProfileActivity.this, getString(R.string.namechanged), Toast.LENGTH_SHORT).show();
                                                userName.setText(editText.getText().toString());

                                                //Update user name in the database
                                                final DatabaseReference refDirect = FirebaseDatabase.getInstance().getReference();

                                                refDirect.child("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            refDirect.child("name").setValue(user.getDisplayName());

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
                                                    }
                                                });

                                            }
                                        }
                                    });
                        } else if (editText.getText().toString().trim().equals("")) {
                            Toast.makeText(ProfileActivity.this, getString(R.string.newname), Toast.LENGTH_SHORT).show();
                        }
                        dialogChangeName.dismiss();

                    } else {
                        Snackbar.make(buttonChange, getString(R.string.checkInternet), Snackbar.LENGTH_SHORT).show();
                    }

                }
            });
            buttonBack.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialogChangeName.dismiss();
                }
            });
            dialogChangeName.show();


        } else {
            Toast.makeText(this, getString(R.string.checkInternet) + "", Toast.LENGTH_SHORT).show();

        }


    }

    // Delete the account
    public void deleteAccount() {
        if (isOnline()) {
            String userUID = user.getUid();
            final Dialog dialog = new Dialog(ProfileActivity.this);
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
            imageView.setImageResource(R.drawable.small_logo_tranparent);
            buttonLogin.setVisibility(View.GONE);
            line.setVisibility(View.GONE);

            textViewTitle.setText(getString(R.string.deleteText));
            textViewTitle.setTextColor(getResources().getColor(R.color.textColor));
            textViewTitle.setTextSize(20.0F);
            textViewTitle.setPadding(50,50,50,0);
            buttonReplay.setText(getString(R.string.continue_button));
            buttonReplay.setTextSize(16.0F);
            buttonBack.setTextSize(16.0F);
            //Font
            String fontPath = "fonts/NeoSans.ttf";
            Typeface font = Typeface.createFromAsset(getAssets(), fontPath);
            textViewTitle.setTypeface(font);
            buttonReplay.setTypeface(font);
            buttonBack.setTypeface(font);

            buttonReplay.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //
                    //delete user content in the database
                    final DatabaseReference refDirect = FirebaseDatabase.getInstance().getReference();

                    refDirect.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("Users").child(userUID).exists()) {
                                refDirect.child("Users").child(userUID).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        user.delete()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
                                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(i);
                                                            overridePendingTransition(R.anim.goup, R.anim.godown);
                                                            finish();
                                                            Toast.makeText(ProfileActivity.this,getString(R.string.accountDeleted),Toast.LENGTH_LONG).show();

                                                        }else {
                                                            FirebaseAuth.getInstance().signOut();
                                                            Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
                                                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(i);
                                                            overridePendingTransition(R.anim.goup, R.anim.godown);
                                                            finish();
                                                            Toast.makeText(ProfileActivity.this,getString(R.string.loginAgain),Toast.LENGTH_LONG).show();
                                                        }

                                                    }
                                                });
                                    }
                                });

                            }else{
                                user.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
                                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(i);
                                                    overridePendingTransition(R.anim.goup, R.anim.godown);
                                                    finish();
                                                    Toast.makeText(ProfileActivity.this,getString(R.string.accountDeleted),Toast.LENGTH_LONG).show();

                                                }else {
                                                    FirebaseAuth.getInstance().signOut();
                                                    Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
                                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(i);
                                                    overridePendingTransition(R.anim.goup, R.anim.godown);
                                                    finish();
                                                    Toast.makeText(ProfileActivity.this,getString(R.string.loginAgain),Toast.LENGTH_LONG).show();
                                                }

                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    //


                }
            });
            buttonBack.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();


        } else {
            Toast.makeText(this, getString(R.string.checkInternet) + "", Toast.LENGTH_SHORT).show();

        }


    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }


}
