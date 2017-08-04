package com.example.minhajlib.elaaj1;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by Minhaj lib on 5/30/2017.
 */

public class DialogBloodGroupAdapter extends ArrayAdapter<String> {

    String[] bloodGroups;
    Context mContext;

    public DialogBloodGroupAdapter(@NonNull Context context, @LayoutRes int resource,String[] bg) {
        super(context, resource,bg);
        mContext = context;
        bloodGroups = bg;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_blood_group,null,false);
        TextView textView = (TextView) view.findViewById(R.id.textView);
        String data = bloodGroups[position];
        textView.setText(data);
        return view;
    }
}
