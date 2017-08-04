package com.example.minhajlib.elaaj1;


import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentOrgsList extends Fragment implements RvAdapterOrgList.OnClickOrgList {

    /*** declaring variables ***/
    ArrayList<DbModalOrg> arrListOrg;
    //views
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    private TextView tvNoOrg;

    //firebase
    DatabaseReference refOrganization;
    ValueEventListener valueEventListener;

    //fragment
    FragmentTransaction fragmentTransaction;


    public FragmentOrgsList() {
        // Required empty public constructor
        arrListOrg = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_orgs_list, container, false);

        //views
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_org_list);
        tvNoOrg = (TextView) view.findViewById(R.id.tv_no_org);
        adapter = new RvAdapterOrgList(getContext(),this,arrListOrg);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        //firebase
        refOrganization = FirebaseDatabase.getInstance().getReference(Constants.DB_ORGANIZATION);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //looping through each child node
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    DbModalOrg dbModalOrg = snapshot.getValue(DbModalOrg.class);
                    arrListOrg.add(dbModalOrg);
                }
                //notify the adapter that data has changed
                adapter.notifyDataSetChanged();
                if (arrListOrg.isEmpty()){
                    tvNoOrg.setVisibility(View.VISIBLE);
                }else {
                    tvNoOrg.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        return view;
    }

    //this method will invoke when click on list of organization
    @Override
    public void clickOrgList(int position) {
        //from this method  we will display organization profile fragment
        String orgId = arrListOrg.get(position).getOrgId();
        Fragment fragment = new FragmentOrgDisplay();
        Bundle bundle = new Bundle();
        bundle.putString("orgId", orgId);
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).mReplaceFragment(fragment,Constants.FRAGMENT_ORGANIZATION_PROFILE,false);
    }

    @Override
    public void onStart() {
        super.onStart();
        arrListOrg.clear();
        refOrganization.addValueEventListener(valueEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        refOrganization.removeEventListener(valueEventListener);
    }
}