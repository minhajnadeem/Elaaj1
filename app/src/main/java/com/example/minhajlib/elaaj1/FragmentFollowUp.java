package com.example.minhajlib.elaaj1;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentFollowUp extends Fragment implements FollowUpAdapter.FollowUpOnClick {

    /*
    declaring variables
     */

    ArrayList<DbModal> arrayList;
    ArrayList<String> arrListDonorId;

    //views
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView.Adapter adapter;
    TextView tvNoDonor;

    //firebase
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser firebaseUser;
    DatabaseReference followUpRef;
    ValueEventListener valueEventListener;

    /**
     * constructor
     */
    public FragmentFollowUp() {
        // Required empty public constructor
        arrayList = new ArrayList<>();
        arrListDonorId = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        /**
         * initializing components
         */

        //views
        View view = inflater.inflate(R.layout.fragment_follow_up, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        adapter = new FollowUpAdapter(getContext(), this, arrayList);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        tvNoDonor = (TextView) view.findViewById(R.id.tv_no_donor_found);

        //firebase
        auth = FirebaseAuth.getInstance();

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrListDonorId.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    String id = data.getValue(String.class);
                    //Log.d("xyz", "follow up :" + id);
                    arrListDonorId.add(id);
                }
                addFollowUp();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            //Log.d("xyz", "initiate follow up ref");
            followUpRef = FirebaseDatabase.getInstance().getReference(Constants.DB_FOLLOW_UP).child(firebaseUser.getUid());
            followUpRef.addValueEventListener(valueEventListener);
            //Log.d("xyz", "follow up ref = " + followUpRef.toString());
        } else {
            Toast.makeText(getContext(), getString(R.string.msg_sign_in_first), Toast.LENGTH_SHORT).show();
            followUpRef.removeEventListener(valueEventListener);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (followUpRef != null) {
            followUpRef.removeEventListener(valueEventListener);
        }
    }

    //adding into list
    public void addFollowUp() {
        arrayList.clear();
        if (arrListDonorId.size() == 0) {
            tvNoDonor.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        } else {
            tvNoDonor.setVisibility(View.GONE);
        }

        for (String id : arrListDonorId) {
            FirebaseDatabase.getInstance().getReference(Constants.DB_USERS).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                        DbModal dbModal = dataSnapshot.getValue(DbModal.class);
                        if (dbModal != null) {
                            arrayList.add(dbModal);
                        }
                    adapter.notifyDataSetChanged();
                    //Log.d("xyz", "adding follow up :" + dbModal.getName());
                    //Log.d("xyz","arr list size "+arrayList.size());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        //Log.d("xyz","arr list size "+arrayList.size());
    }

    //when click on view
    @Override
    public void followUpOnClick(int position) {

        final DbModal modal = arrayList.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("data", modal);
        FragmentManager fragmentManager = getFragmentManager();
        CustomCallDialog customCall = new CustomCallDialog();
        customCall.setArguments(bundle);
        customCall.show(fragmentManager, "callDialog");
    }
}