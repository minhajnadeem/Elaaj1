package com.example.minhajlib.elaaj1;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by Minhaj lib on 4/10/2017.
 */

public class Elaaj extends Application {

    FirebaseDatabase firebaseDatabase;
    DatabaseReference dbRefUsers,dbOrgRef,dbFollowUpRef;

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
        dbRefUsers = firebaseDatabase.getReference(Constants.DB_USERS);
        dbOrgRef = firebaseDatabase.getReference(Constants.DB_ORGANIZATION);
        dbFollowUpRef = firebaseDatabase.getReference(Constants.DB_FOLLOW_UP);
        dbRefUsers.keepSynced(true);
        dbOrgRef.keepSynced(true);
        dbFollowUpRef.keepSynced(true);

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }
}
