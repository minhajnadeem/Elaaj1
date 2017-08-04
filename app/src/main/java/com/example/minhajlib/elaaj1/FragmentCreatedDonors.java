package com.example.minhajlib.elaaj1;


import android.content.Context;
import android.icu.text.LocaleDisplayNames;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCreatedDonors extends Fragment implements RecyclerAdapterCreatedDonors.OnClickCreatedDonor {

    /*** declaring variables ***/
    private ArrayList<DbModal> arrListMain;
    private ArrayList<DbModal> arrayListFilter;
    private Boolean isSignIn;
    String assocId;

    //views
    private RecyclerView.Adapter adapter;
    private EditText etSearch;
    private TextWatcher mTextWatcher;
    TextView tvMatchNotFound;
    Context context;

    FirebaseAuth auth;

    //constructor
    public FragmentCreatedDonors() {
        // Required empty public constructor
        arrListMain = new ArrayList<>();
        arrayListFilter = new ArrayList<>();
        assocId = "";
        isSignIn = false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = getContext();
        /*** initializing components ***/

        //views
        View view = inflater.inflate(R.layout.fragment_created_donors, container, false);
        etSearch = (EditText) view.findViewById(R.id.et_search);
        Button btnAddNewDonor = (Button) view.findViewById(R.id.btn_add_new_donor);
        tvMatchNotFound = (TextView) view.findViewById(R.id.tv_match_not_found);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rv_created_donors);
        adapter = new RecyclerAdapterCreatedDonors(getContext(), this, arrayListFilter);
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Log.d("xyz", "search " + s);
                filterList(s);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        btnAddNewDonor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignIn) {
                    //navigate to create donor profile
                    ((MainActivity) getActivity()).mReplaceFragment(new FragmentCreatedDonorsProfile(), Constants.FRAGMENT_CREATED_DONORS_PROFILE, true);
                }else {
                    Toast.makeText(context, getString(R.string.msg_sign_in_first), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //firebase
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            isSignIn = true;
            assocId = firebaseUser.getUid();
            doWork();   //display created donor
        } else {
            isSignIn = false;
            assocId = "";
        }
        return view;
    }

    //display created donors;called when user sign in;
    private void doWork() {
        arrayListFilter.clear();
        arrListMain.clear();
        if (isSignIn) {
            FirebaseDatabase.getInstance().getReference(Constants.DB_USERS)
                    .orderByChild(Constants.ASSOCIATION).equalTo(assocId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                DbModal dbModal = snapshot.getValue(DbModal.class);
                                arrListMain.add(dbModal);
                                arrayListFilter.add(dbModal);
                            }
                            adapter.notifyDataSetChanged();
                            etSearch.addTextChangedListener(mTextWatcher);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
        }

    }

    //filter data by name
    private void filterList(CharSequence s) {
        arrayListFilter.clear();
        String nameSearch = s.toString().toLowerCase().trim();
        for (DbModal dbModal : arrListMain) {
            if (dbModal.getName().toLowerCase().startsWith(nameSearch) || nameSearch.equalsIgnoreCase(dbModal.getBloodGroup())) {
                arrayListFilter.add(dbModal);
            }
        }
        if(arrayListFilter.isEmpty()){
            tvMatchNotFound.setVisibility(View.VISIBLE);
        }else {
            tvMatchNotFound.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    //this method will trigger when click on view
    @Override
    public void clickCreatedDonor(int position) {

        DbModal dbModal = arrListMain.get(position);
        String id = dbModal.getId();
        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        Fragment fragment = new FragmentCreatedDonorsProfile();
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).mReplaceFragment(fragment, Constants.FRAGMENT_CREATED_DONORS_PROFILE, true);
        etSearch.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        etSearch.removeTextChangedListener(mTextWatcher);
    }
}