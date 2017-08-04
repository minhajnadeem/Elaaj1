package com.example.minhajlib.elaaj1;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Minhaj lib on 5/6/2017.
 */

class RecyclerAdapterCreatedDonors extends RecyclerView.Adapter<RecyclerAdapterCreatedDonors.MyViewHolder> {

    Context context;
    private ArrayList<DbModal> arrayList;
    private OnClickCreatedDonor onClickCreatedDonor;

    RecyclerAdapterCreatedDonors(Context context, OnClickCreatedDonor onClickCreatedDonor, ArrayList<DbModal> arrayList){
        this.context = context;
        this.arrayList = arrayList;
        this.onClickCreatedDonor =  onClickCreatedDonor;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custome_created_donor,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final DbModal modal = arrayList.get(position);
        holder.tvDonorName.setText(modal.getName());
        holder.tvDonorCity.setText(modal.getCity());
        holder.tvDonorAddress.setText(modal.getAddress());
        holder.tvLastDonation.setText(String.format("Donated : %s", modal.getLastDonated()));
        holder.tvOrgName.setText(modal.getOrgName());
        if (modal.getIsDonor().equals("true")){
            holder.tvActive.setText("showing in donors list");
        }else {
            holder.tvActive.setText("not showing in donors list");
        }
        if (modal.getPaid() != null && modal.getPaid()) {
            holder.tvFreePaid.setText(String.format("Price : %s Rs/Bottle", modal.getBloodPrice()));
        } else {
            holder.tvFreePaid.setText("Price : Free");
        }
        holder.ivPhoneReceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialerActivity(modal.getContact());
            }
        });

        String blood = modal.getBloodGroup();
        int color;
        switch (blood) {
            case "A+":
                color = R.color.a_plus;
                break;
            case "AB+":
                color = R.color.ab_plus;
                break;
            case "B+":
                color = R.color.b_plus;
                break;
            case "O+":
                color = R.color.o_plus;
                break;
            case "A-":
                color = R.color.a_minus;
                break;
            case "AB-":
                color = R.color.ab_minus;
                break;
            case "B-":
                color = R.color.b_minus;
                break;
            case "O-":
                color = R.color.o_minus;
                break;
            default:
                color = R.color.no_color;
        }

        holder.tvBloodIc.setText(modal.getBloodGroup());
        Drawable tvCircle = holder.tvBloodIc.getBackground();
        tvCircle.setColorFilter(context.getResources().getColor(color), PorterDuff.Mode.ADD);
    }

    private void openDialerActivity(String contact) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+contact));
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tvDonorName, tvDonorCity, tvDonorAddress,tvBloodIc,tvActive,tvLastDonation,tvFreePaid,tvOrgName;
        ImageView ivPhoneReceiver;

        MyViewHolder(View itemView) {
            super(itemView);
            tvDonorName = (TextView) itemView.findViewById(R.id.tv_donor_name);
            tvDonorCity = (TextView) itemView.findViewById(R.id.tv_donor_city);
            tvDonorAddress = (TextView) itemView.findViewById(R.id.tv_donor_address);
            tvBloodIc = (TextView) itemView.findViewById(R.id.tv_blood_ic);
            ivPhoneReceiver = (ImageView) itemView.findViewById(R.id.textViewOptions);
            tvLastDonation = (TextView) itemView.findViewById(R.id.tv_last_donated);
            tvFreePaid = (TextView) itemView.findViewById(R.id.tv_free_paid);
            tvOrgName = (TextView) itemView.findViewById(R.id.tv_org_name);
            tvActive = (TextView) itemView.findViewById(R.id.tv_active);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickCreatedDonor.clickCreatedDonor(getAdapterPosition());
        }
    }

    interface OnClickCreatedDonor{
        void clickCreatedDonor(int position);
    }
}