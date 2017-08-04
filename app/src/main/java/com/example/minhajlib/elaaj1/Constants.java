package com.example.minhajlib.elaaj1;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import static com.facebook.FacebookSdk.getApplicationContext;
import static okhttp3.internal.Internal.instance;

/**
 * Created by Minhaj lib on 5/10/2017.
 */

public class Constants {

    private static Constants instance;

    public static String BLOOD_GROUP = "All";
    public static String CITY = "All";
    public static String IS_DONOR = "isDonor";
    public static String ORG_NAME = "orgName";
    public static String ASSOCIATION = "association";
    public static String ORG_ID = "orgId";
    public static String USER_CONTACT = "contact";
    public static String PRIVATE = "Private";
    public static String GOVT = "Govt";

    //for firebase; nodes(table) names
    public static String DB_USERS = "users";
    public static String DB_FOLLOW_UP = "follow_up";
    public static String DB_ORGANIZATION = "organization";

    //for fragments
    public static final String FRAGMENT_DONORS_LIST = "Donors List";
    public static final String FRAGMENT_PROFILE = "Profile";
    public static final String FRAGMENT_MY_DONORS = "My Donors";
    public static final String FRAGMENT_CREATE_YOUR_DONORS = "Create Your Donors";
    public static final String FRAGMENT_CREATED_DONORS_LIST = "Created Donors List";
    public static final String FRAGMENT_CREATED_DONORS_PROFILE = "Your's Donor Profile";
    public static final String FRAGMENT_CREATE_ORGANIZATION = "Create Organization";
    public static final String FRAGMENT_ORGANIZATION_LIST = "Organizations List";
    public static final String FRAGMENT_ORGANIZATION_PROFILE = "Organizations Profile";

    //progress dialog
    ProgressDialog progressDialog;

    public static Constants getInstance() {
        if (instance == null) {
            return instance = new Constants();
        } else {
            return instance;
        }
    }

    public Boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public String[] getBloodGroups() {
        return new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
    }

    //cities list
    public String[] getCities() {
        return new String[]{"Bahawalpur", "Multan"};
    }

    //downloading image into imageView
    public void downloadWithPicasso(final Context context, final String photoUrl, final ImageView ivInto) {
        if (!photoUrl.isEmpty()) {
            Picasso.with(context)
                    .load(photoUrl)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    //.placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .into(ivInto, new Callback() {
                        @Override
                        public void onSuccess() {}

                        @Override
                        public void onError() {
                            Picasso.with(context)
                                    .load(photoUrl)
                                    //.placeholder(R.drawable.spinner_32)
                                    .error(R.drawable.user)
                                    .into(ivInto);
                        }
                    });
        }
    }

    //downloading image from internet only; don't use cache
    public void downloadWithPicassoNoCache(final Context context, final String photoUrl, final ImageView ivInto) {
        if (!photoUrl.isEmpty()) {
            Picasso.with(context)
                    .load(photoUrl)
                    .placeholder(R.drawable.spinner_32)
                    .error(R.drawable.user)
                    .into(ivInto);
        }
    }

    //downloading image from internet only; don't use cache
    public void downloadWithPicassoNoPlaceholder(final Context context, final String photoUrl, final ImageView ivInto) {
        if (!photoUrl.isEmpty()) {
            Picasso.with(context)
                    .load(photoUrl)
                    .error(R.drawable.user)
                    .into(ivInto);
        }
    }

    //downloading organization logo
    public void downloadWithPicassoOrgLogo(final Context context, final String photoUrl, final ImageView ivInto) {
        if (!photoUrl.isEmpty()) {
            Picasso.with(context)
                    .load(photoUrl)
                    .error(R.drawable.placeholder_1024x512)
                    .into(ivInto);
        }
    }

    public ProgressDialog showProgressDialog(Context context, String message) {

        if (!isInternetAvailable()) {
            //if no internet don't show progress dialod; display toast only
            displayToast(context, context.getString(R.string.msg_save_offline), Toast.LENGTH_SHORT);
            return null;
        }
        try {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(message);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.show();
        } catch (NullPointerException e) {
            Log.d("xyz", "exception progress dialog");
        }
        return progressDialog;
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void displayToast(Context context, String message, int length) {
        try {
            Toast toast = Toast.makeText(context, message, length);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } catch (NullPointerException e) {
            Log.d("xyz", "toast exception");
        }
    }
}