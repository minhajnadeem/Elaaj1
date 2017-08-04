package com.example.minhajlib.elaaj1;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Minhaj lib on 5/10/2017.
 */

public class RvAdapterOrgList extends RecyclerView.Adapter<RvAdapterOrgList.MyViewHolder> {

    Context context;
    ArrayList<DbModalOrg> arrayList;
    RvAdapterOrgList.OnClickOrgList onClickOrgList;

    public RvAdapterOrgList(Context context, RvAdapterOrgList.OnClickOrgList onClickOrgList, ArrayList<DbModalOrg> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.onClickOrgList = onClickOrgList;
    }

    @Override
    public RvAdapterOrgList.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_org_list, parent, false);
        MyViewHolder myViewHolder = new RvAdapterOrgList.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final RvAdapterOrgList.MyViewHolder holder, int position) {
        final DbModalOrg modal = arrayList.get(position);
        holder.tvOrgName.setText(modal.getOrgName());
        String logoUrl = modal.getOrgLogoUrl();
        if (!logoUrl.isEmpty()) {
            Picasso.with(context)
                    .load(modal.getOrgLogoUrl())
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    //.error(R.drawable.organization_48)
                    .into(holder.civOrgLogo, new Callback() {
                        @Override
                        public void onSuccess() {}
                        @Override
                        public void onError() {
                            Picasso.with(context).load(modal.getOrgLogoUrl())
                                    //.error(R.drawable.organization_48)
                                    .into(holder.civOrgLogo);
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tvOrgName;
        public ImageView civOrgLogo;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvOrgName = (TextView) itemView.findViewById(R.id.tv_org_name);
            civOrgLogo = (ImageView) itemView.findViewById(R.id.civ_org_logo);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickOrgList.clickOrgList(getAdapterPosition());
        }
    }

    interface OnClickOrgList {
        void clickOrgList(int position);
    }
}