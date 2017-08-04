package com.example.minhajlib.elaaj1;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Minhaj lib on 4/11/2017.
 */

class FollowUpAdapter extends RecyclerView.Adapter<FollowUpAdapter.MyViewHolder> {

    private ArrayList<DbModal> arrayList;
    private Context context;
    private FollowUpOnClick followUpOnClick;

    private DatabaseReference followUpRef ;
    private FirebaseUser firebaseUser;

    FollowUpAdapter(Context context, FollowUpOnClick followUpOnClick, ArrayList<DbModal> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.followUpOnClick = followUpOnClick;
        followUpRef = FirebaseDatabase.getInstance().getReference(Constants.DB_FOLLOW_UP);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        view = LayoutInflater.from(context).inflate(R.layout.custome_favorites, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        DbModal modal = arrayList.get(position);
        holder.tvDonorName.setText(modal.getName());
        holder.tvDonorCity.setText(modal.getCity());
        holder.tvDonorAddress.setText(modal.getAddress());
        holder.tvLastDonated.setText(String.format("Blood Donated : %s", modal.getLastDonated()));
        holder.tvOptionMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFromFollowUp(position);
            }
        });
        holder.tvOrgName.setText(modal.getOrgName());
        if (modal.getPaid() != null && modal.getPaid()) {
            holder.tvFreePaid.setText(String.format("Price : %s Rs/Bottle", modal.getBloodPrice()));
        } else {
            holder.tvFreePaid.setText("Price : Free");
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
        tvCircle.setColorFilter(context.getResources().getColor(color), PorterDuff.Mode.ADD);

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    //to remove from my donor list
    public void removeFromFollowUp(int position) {
        if (firebaseUser != null) {

            DbModal modal = arrayList.get(position);
            //Log.d("xyz", "modal :" + modal.getId());
            followUpRef.child(firebaseUser.getUid()).child(modal.getId()).setValue(null);
            removePrefFavDonors(modal.getId());
        }else {
            Constants.getInstance().displayToast(context, context.getString(R.string.msg_sign_in_first), Toast.LENGTH_SHORT);
        }
    }

    private void removePrefFavDonors(String id) {
        SharedPreferences preferences = context.getSharedPreferences(firebaseUser.getUid(),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(id);
        editor.apply();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tvDonorName, tvDonorCity, tvDonorAddress, tvOptionMenu, tvOrgName, tvLastDonated ,tvBloodIc, tvFreePaid, tvLastCall;

        public MyViewHolder(View itemView) {
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
            followUpOnClick.followUpOnClick(getAdapterPosition());
        }
    }

    interface FollowUpOnClick {
        void followUpOnClick(int position);
    }
}