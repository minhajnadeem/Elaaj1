package com.example.minhajlib.elaaj1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static com.example.minhajlib.elaaj1.R.drawable.user;

/**
 * Created by Minhaj lib on 4/3/2017.
 */

class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    Context context;
    private ArrayList<DbModal> arrayList;
    private RecyclerViewOnClick recyclerViewOnClick;
    private SharedPreferences mPreferences, mprefFavDonors;

    RecyclerAdapter(Context context, RecyclerViewOnClick recyclerViewOnClick, ArrayList<DbModal> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.recyclerViewOnClick = recyclerViewOnClick;

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            mPreferences = context.getSharedPreferences(firebaseUser.getUid()+"_call_history", Context.MODE_PRIVATE);
            mprefFavDonors = context.getSharedPreferences(firebaseUser.getUid(), Context.MODE_PRIVATE);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.custome_layout, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final int index = holder.getAdapterPosition();
        final DbModal modal = arrayList.get(index);
        holder.tvDonorName.setText(modal.getName());
        holder.tvDonorCity.setText(modal.getCity());
        holder.tvDonorAddress.setText(modal.getAddress());
        holder.tvLastDonated.setText(String.format("Donated : %s", modal.getLastDonated()));
        holder.tvOptionMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerViewOnClick.addToFollowUp(index,holder.tvOptionMenu);
            }
        });

        holder.tvOrgName.setText(modal.getOrgName());
            /*holder.tvOrgName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recyclerViewOnClick.rvOnClickOrgName(modal.getAssociation());
                }
            });*/

        if (modal.getPaid() != null && modal.getPaid()) {
            holder.tvFreePaid.setText(String.format("Price : %s Rs/Bottle", modal.getBloodPrice()));
        } else {
            holder.tvFreePaid.setText(R.string.price);
        }

        if (mPreferences != null) {
            Boolean isCallMade = mPreferences.getBoolean(modal.getId()+"_bool", false);
            if (isCallMade) {
                holder.tvLastCall.setVisibility(View.VISIBLE);
                holder.tvLastCall.setText(mPreferences.getString(modal.getId(), ""));
            } else {
                holder.tvLastCall.setVisibility(View.INVISIBLE);
            }
        }

        if (mprefFavDonors != null) {
            Boolean isFavorite = mprefFavDonors.getBoolean(modal.getId(), false);
            if (isFavorite) {
                holder.tvOptionMenu.setBackgroundResource(R.drawable.ic_star_filled);
            } else {
                holder.tvOptionMenu.setBackgroundResource(R.drawable.ic_star);
            }
        }

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
        tvCircle.setColorFilter(ResourcesCompat.getColor(context.getResources(),color,null), PorterDuff.Mode.ADD);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    private void showPopUp(int position) {
        /*//creating a popup menu
        PopupMenu popup = new PopupMenu(context, holder.tvOptionMenu);
        //inflating menu from xml resource
        popup.inflate(R.menu.text_option_menu);
        //adding click listener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_follow_up:
                        recyclerViewOnClick.addToFollowUp(position);
                        break;
                }
                return false;
            }
        });
        //displaying the popup
        popup.show();*/
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvDonorName, tvDonorCity, tvDonorAddress, tvOptionMenu, tvOrgName, tvLastDonated;
        TextView tvBloodIc, tvFreePaid, tvLastCall;

        MyViewHolder(View itemView) {
            super(itemView);
            tvDonorName = (TextView) itemView.findViewById(R.id.tv_donor_name);
            tvDonorCity = (TextView) itemView.findViewById(R.id.tv_donor_city);
            tvDonorAddress = (TextView) itemView.findViewById(R.id.tv_donor_address);
            tvBloodIc = (TextView) itemView.findViewById(R.id.tv_blood_ic);
            tvOptionMenu = (TextView) itemView.findViewById(R.id.textViewOptions);
            tvOrgName = (TextView) itemView.findViewById(R.id.tv_org_name);
            tvLastDonated = (TextView) itemView.findViewById(R.id.tv_last_donated);
            tvFreePaid = (TextView) itemView.findViewById(R.id.tv_free_paid);
            tvLastCall = (TextView) itemView.findViewById(R.id.tv_last_called);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            recyclerViewOnClick.rvOnclick(getAdapterPosition());
        }
    }

    interface RecyclerViewOnClick {

        void rvOnclick(int position);
        void addToFollowUp(int position,TextView textView);
        //void rvOnClickOrgName(String orgId);
    }
}