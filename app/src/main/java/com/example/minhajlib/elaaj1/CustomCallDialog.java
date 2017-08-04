package com.example.minhajlib.elaaj1;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

import static okhttp3.Protocol.get;

/**
 * Created by Minhaj lib on 6/23/2017.
 */

public class CustomCallDialog extends DialogFragment {

    FirebaseUser mFirebaseUser;
    CustomCallDialog mCallDialog;

    public CustomCallDialog() {
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mCallDialog = this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        final DbModal modal = (DbModal) bundle.get("data");
        View customCallDialog = inflater.inflate(R.layout.custome_call_dialog, container, false);
        TextView tvName = (TextView) customCallDialog.findViewById(R.id.tv_dialog_name);
        TextView tvShare = (TextView) customCallDialog.findViewById(R.id.btn_share);
        final TextView tvCall = (TextView) customCallDialog.findViewById(R.id.btn_call);
        final ImageView imageView = (ImageView) customCallDialog.findViewById(R.id.iv_dialog_dp);

        tvName.setText(modal.getName());
        Constants.getInstance().downloadWithPicassoNoCache(getContext(), modal.getPhotoUrl(), imageView);
        tvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareThisDonor(modal);
            }
        });
        tvCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + modal.getContact()));
                startActivity(intent);
                saveCallInPref(modal.getId());
                mCallDialog.dismiss();
            }
        });
        return customCallDialog;
    }

    private void saveCallInPref(String id) {

        if (mFirebaseUser != null) {
            SharedPreferences preferences = getContext().getSharedPreferences(mFirebaseUser.getUid() + "_call_history", Context.MODE_PRIVATE);
            Calendar calendar = Calendar.getInstance();
            int dom = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            String dateCalled = dom + "/" + month;
            //int year = calendar.get(Calendar.YEAR);
            SharedPreferences.Editor editor = preferences.edit();
            if (preferences.getAll().size() > 10) {
                editor.clear();
            }
            //editor.putString(id,dom+"/"+month+"/"+year);
            editor.putBoolean(id+"_bool", true);
            editor.putString(id, dateCalled);
            editor.apply();
        }
    }

    private void shareThisDonor(DbModal modal) {

        StringBuilder shareStringBuilder = new StringBuilder();
        shareStringBuilder.append("please contact to:")
                .append("\nName : " + modal.getName())
                .append("\nNumber : " + modal.getContact())
                .append("\nBlood Group : ")
                .append(modal.getBloodGroup())
                .append("\nCity : " + modal.getCity());
        String textToShare = shareStringBuilder.toString();

        /*String textToShare = "please contact to:"
                + "\nName : " + modal.getName()
                + "\nNumber : " + modal.getContact()
                + "\nBlood Group : "
                + modal.getBloodGroup()
                + "\nCity : " + modal.getCity();*/

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, textToShare);
        intent.setType("text/plain");
        startActivity(intent);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}