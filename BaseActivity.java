package com.superquiz.easyquiz.triviastar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.annotation.LayoutRes;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.UserMessagingPlatform;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static com.android.billingclient.api.BillingClient.SkuType.INAPP;




public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    int code;
    SharedPreferences preferences;
    String order;
    SharedPreferences.Editor editor;
    private NavigationView navigationView;
    private DrawerLayout fullLayout;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private int selectedNavItemId;
    private BillingClient billingClient;
    public static final String PRODUCT_ID= "quiznoads";
    static final String TAG = "InAppPurchaseTag";
    public static DrawerLayout drawer;
    private ProductDetails productDetails;
    private Purchase purchase;
    @Override
    public void setContentView(@LayoutRes int layoutResID) {

        /**
         * This is going to be our actual root layout.
         */
        fullLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        /**
         * {@link FrameLayout} to inflate the child's view. We could also use a {@link android.view.ViewStub}
         */
        FrameLayout activityContainer = (FrameLayout) fullLayout.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        /**
         * Note that we don'title pass the child's layoutId to the parent,
         * instead we pass it our inflated layout.
         */
        super.setContentView(fullLayout);
        drawer = (DrawerLayout) findViewById(R.id.activity_container);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        preferences = PreferenceManager.getDefaultSharedPreferences(BaseActivity.this);
        editor = preferences.edit();

        navigationView = (NavigationView) findViewById(R.id.navigationView);
        Menu m = navigationView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);

            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }

            applyFontToMenuItem(mi);
        }
        if (useToolbar()) {
            setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }
        //Enable / Disable Sounds Effects
        //=================================================================================//
        SwitchCompat drawerSwitch = (SwitchCompat) navigationView.getMenu().findItem(R.id.switch_item).getActionView();
        if (preferences.getBoolean("sounds", true) == true) {
            drawerSwitch.setChecked(true);
        } else if (preferences.getBoolean("sounds", true) == false) {
            drawerSwitch.setChecked(false);
        }
        drawerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean("sounds", true);
                    editor.apply();
                } else {
                    editor.putBoolean("sounds", false);
                    editor.apply();

                }
            }
        });
        //=================================================================================//
        setUpNavView();
    }

    /**
     * Premiumdialog
     */

    protected fun showUpgradeDialog() {
        if (preferences.getString("purchased", null) != null) {
            return  // User has already purchased, don't show dialog
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_upgrade, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

        dialogView.findViewById<Button>(R.id.btn_upgrade).setOnClickListener {
            billingSetup()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Helper method that can be used by child classes to
     * specify that they don'title want a {@link Toolbar}
     *
     * @return true
     */
    protected boolean useToolbar() {
        return true;
    }

    protected void setUpNavView() {
        navigationView.setNavigationItemSelectedListener(this);

        if (useDrawerToggle()) { // use the hamburger menu
            drawerToggle = new ActionBarDrawerToggle(this, fullLayout, toolbar,
                    R.string.nav_drawer_opened,
                    R.string.nav_drawer_closed);

            fullLayout.setDrawerListener(drawerToggle);
            drawerToggle.syncState();
        } else if (useToolbar() && getSupportActionBar() != null) {
            // Use home/back button instead
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(getResources()
                    .getDrawable(R.drawable.abc_ic_ab_back_material));
        }
    }

    /**
     * Helper method to allow child classes to opt-out of having the
     * hamburger menu.
     *
     * @return
     */
    protected boolean useDrawerToggle() {
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        fullLayout.closeDrawer(GravityCompat.START);
        selectedNavItemId = menuItem.getItemId();

        return onOptionsItemSelected(menuItem);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Context context = this;
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.levels:
                Intent i = new Intent(this, CategoriesActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                overridePendingTransition(R.anim.goup, R.anim.godown);
                return true;

            case R.id.switch_item:
                return false;

            case R.id.leaderboard_item:
                Intent i2 = new Intent(this, LeaderBoardActivity.class);
                i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i2);
                overridePendingTransition(R.anim.goup, R.anim.godown);
                return true;


            case R.id.profile_item:
                Intent i5 = new Intent(this, ProfileActivity.class);
                startActivity(i5);
                return true;


            case R.id.remove_ads:
                String purchasedItem = preferences.getString("purchased", null);
                if (purchasedItem == null) {
                    billingSetup();;

                } else {
                    Toast.makeText(this, getString(R.string.adsremovedone) + "", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.share:
                //share intent
                shareTextUrl();
                return true;

            case R.id.rate:
                String package_name = getPackageName();
                Intent r = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + package_name));
                startActivity(r);
                return true;


            case R.id.contact_us:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri data = Uri.parse("mailto:" + getString(R.string.email));
                intent.setData(data);
                startActivity(intent);
                return true;
            case R.id.privacypolicy:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.gdpr_privacypolicy))));
                return true;
            case R.id.resetAds:
                adsSettings();
                return true;


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void shareTextUrl() {
        String package_name = getPackageName();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_name) + ":  " + "https://play.google.com/store/apps/details?id=" + package_name);
        startActivity(Intent.createChooser(share, getString(R.string.share_title)));
    }

    private void applyFontToMenuItem(MenuItem mi) {
        String fontPath = "fonts/NeoSans.ttf";


        Typeface font = Typeface.createFromAsset(getAssets(), fontPath);
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    /*******************************************/
    public void adsSettings() {
        ConsentInformation consentInformation = UserMessagingPlatform.getConsentInformation(this);
        consentInformation.reset();

        Intent i4 = new Intent(this, SplashActivity.class);
        i4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i4);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }

    //=================================================//
    private void billingSetup() {

        billingClient = BillingClient.newBuilder(this).setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
                //

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                    for (Purchase purchase : list) {
                        completePurchase(purchase);
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    Log.i(TAG, "onPurchasesUpdated: Purchase Canceled");
                } else {
                    Log.i(TAG, "onPurchasesUpdated: Error");
                }
                //
            }
        }).enablePendingPurchases().build();

        billingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "OnBillingSetupFinish connected");
                    queryProduct();
                } else {
                    Log.i(TAG, "OnBillingSetupFinish failed");
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.i(TAG, "OnBillingSetupFinish connection lost");
            }
        });
    }

    private void queryProduct() {

        QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(ImmutableList.of(QueryProductDetailsParams.Product.newBuilder().setProductId(PRODUCT_ID).setProductType(BillingClient.ProductType.INAPP)

                .build())).build();

        billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> productDetailsList) {

                if (!productDetailsList.isEmpty()) {
                    productDetails = productDetailsList.get(0);
                    makePurchase();
                } else {
                    Log.i(TAG, "onProductDetailsResponse: No products");
                }
            }
        });
        billingClient.queryProductDetailsAsync(queryProductDetailsParams, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {

            }
        });
    }


    public void makePurchase() {

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder().setProductDetailsParamsList(ImmutableList.of(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(productDetails).build())).build();

        billingClient.launchBillingFlow(this, billingFlowParams);
    }

    private void completePurchase(Purchase item) {

        purchase = item;

        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

            if (preferences.getString("purchased", null) == null) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("purchased", "yes");
                editor.apply();
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.adsremovedone), Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, SplashActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
        }

    }

    public void consumePurchase() {
        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, @NonNull String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    {
                        //TODO
                    }
                }
            }
        };
        billingClient.consumeAsync(consumeParams, listener);
    }
    //============================================================//
}