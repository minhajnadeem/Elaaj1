package com.example.minhajlib.elaaj1;


import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.internal.CollectionMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Calendar;
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 */
public class DonorsList extends Fragment implements RecyclerAdapter.RecyclerViewOnClick {

    //declaring variables
    private String selectedBlood, selectedCity;
    private String[] strArrBloodGroups, strArrCity;
    private ArrayList<DbModal> arrListMain;
    private ArrayList<DbModal> arrListFilter;
    private Boolean isSignedIn;

    //defining views variables
    Spinner spinnerBloodGroup, spinnerCity;
    Button btnSelectBlood, btnSelectCity;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    DatePicker datePicker;
    TimePicker timePicker;
    Button btnSetAlarm;
    View customCallDialog;
    TextView tvNoDonorFound;

    //declaring firebase variables
    FirebaseDatabase firebaseDatabase;
    DatabaseReference rootRef;
    DatabaseReference usersRef;
    DatabaseReference followUpRef;
    Query donorsRef;
    ChildEventListener childEventListener;
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser firebaseUser;

    //fragment
    FragmentManager fragmentManager;

    public DonorsList() {
        // Required empty public constructor
        /***initializing variables ***/
        //initializing array list
        arrListMain = new ArrayList<>();
        arrListFilter = new ArrayList<>();
        strArrBloodGroups = new String[]{Constants.BLOOD_GROUP, "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "0-"};
        strArrCity = new String[]{Constants.CITY, "Bahawalpur", "Multan"};
//        strArrBloodGroups = getResources().getStringArray(R.array.blood_groups);
//        strArrCity = getResources().getStringArray(R.array.cities);
        selectedBlood = strArrBloodGroups[0];
        selectedCity = strArrCity[0];
        isSignedIn = false;
    }

    //attaching firebase database listener
    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_donors_list, container, false);
        //initializing views
        //spinnerBloodGroup = (Spinner) view.findViewById(R.id.spinner_blood_group);
        //spinnerCity = (Spinner) view.findViewById(R.id.spinner_city);
        btnSelectBlood = (Button) view.findViewById(R.id.btn_select_blood);
        btnSelectCity = (Button) view.findViewById(R.id.btn_select_city);
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_donors_list);
        adapter = new RecyclerAdapter(getContext(), this, arrListFilter);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        tvNoDonorFound = (TextView) view.findViewById(R.id.tv_no_donor_found);

        //initializing spinner adapters
        /*ArrayAdapter<String> spinnerBloodAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, strArrBloodGroups);
        spinnerBloodAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinnerBloodGroup.setAdapter(spinnerBloodAdapter);
        ArrayAdapter<String> spinnerCityAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, strArrCity);
        spinnerCityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(spinnerCityAdapter);
        *//***listeners for spinner***//*
        //blood group spinner
        spinnerBloodGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBlood = strArrBloodGroups[position];
                filterList();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        //city spinner
        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCity = strArrCity[position];
                filterList();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });*/

        //select buttons listeners
        btnSelectBlood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectBloodDialog();
            }
        });

        btnSelectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectCityDialog();
            }
        });

        //fragment components
        fragmentManager = getActivity().getSupportFragmentManager();

        /***firebase components***/
        //donorsRef is a firebase query
        firebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = firebaseDatabase.getReference();
        usersRef = rootRef.child(Constants.DB_USERS);
        followUpRef = rootRef.child(Constants.DB_FOLLOW_UP);
        donorsRef = usersRef.orderByChild(Constants.IS_DONOR);
        auth = FirebaseAuth.getInstance();
        //checking for user session
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = auth.getCurrentUser();
                if (firebaseUser == null) {
                    isSignedIn = false;
                } else {
                    isSignedIn = true;
                }
            }
        };
        auth.addAuthStateListener(authStateListener);
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DbModal dbModal = dataSnapshot.getValue(DbModal.class);
                //if value of isDonor is true
                if (dbModal.getIsDonor().equals("true")) {
                    arrListMain.add(dbModal);
                    Log.d("xyz", "add donor in list :" + arrListMain.size());
                    filterList();       //apply filter on this list
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                int index = -1;
                //getting changed donor
                DbModal dbModal = dataSnapshot.getValue(DbModal.class);
                Log.d("xyz", "dbModal isDonor = " + dbModal.getIsDonor());
                for (DbModal modal : arrListMain) {
                    if (modal.getId().equals(dbModal.getId())) {
                        index = arrListMain.indexOf(modal);
                        break;
                    }
                }
                Log.d("xyz", "index data changed =" + index);
                if (index >= 0) {
                    if (dbModal.getIsDonor().equals("false")) {
                        //remove from list because donor is no longer available
                        Log.d("xyz", "removing " + dbModal.getName());
                        arrListMain.remove(index);
                    } else {
                        //update the donor data
                        Log.d("xyz", "updating");
                        arrListMain.set(index, dbModal);
                    }
                    //request for refresh list of donors
                    Log.d("xyz", "request for refresh :" + selectedBlood + ":" + selectedCity);
                    filterList();
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        arrListMain.clear();      //clear list each time listener attach
        donorsRef.addChildEventListener(childEventListener);

        //returning view
        return view;
    }//end onCreateView;

    private void showSelectBloodDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View header = inflater.inflate(R.layout.custome_select_blood_title, null, false);
        builder.setCustomTitle(header);
        builder.setItems(strArrBloodGroups, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedBlood = strArrBloodGroups[which];
                if (which == 0) {
                    btnSelectBlood.setText("Select Blood Group");
                } else {
                    btnSelectBlood.setText(selectedBlood);
                }
                Log.d("xyz", "blood group :" + selectedBlood);
                filterList();
            }
        });
        builder.show();
    }

    private void showSelectCityDialog() {
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View header = inflater.inflate(R.layout.custome_select_city_title, null, false);
        builder.setCustomTitle(header);
        builder.setItems(strArrCity, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedCity = strArrCity[which];
                if (which == 0) {
                    btnSelectCity.setText("Select City");
                } else {
                    btnSelectCity.setText(selectedCity);
                }
                Log.d("xyz", "city :" + selectedCity);
                filterList();
            }
        });
        builder.show();
    }

    private void filterList() {
        arrListFilter.clear();
        Log.d("xyz", "blood to find :" + selectedBlood + ":" + selectedCity);

        if (selectedBlood.equals(Constants.BLOOD_GROUP) && selectedCity.equals(Constants.CITY)) {
            for (DbModal dbModal : arrListMain) {
                arrListFilter.add(dbModal);
            }
        } else if (selectedCity.equals(Constants.CITY)) {
            for (DbModal dbModal : arrListMain) {
                if (dbModal.getBloodGroup().equals(selectedBlood)) {
                    //then add this is filtered list
                    arrListFilter.add(dbModal);
                }
            }
        } else if (selectedBlood.equals(Constants.BLOOD_GROUP)) {
            for (DbModal dbModal : arrListMain) {
                if (dbModal.getCity().equals(selectedCity)) {
                    //then add this in filtered list
                    arrListFilter.add(dbModal);
                }
            }
        } else {
            for (DbModal dbModal : arrListMain) {
                if (dbModal.getBloodGroup().equals(selectedBlood) && dbModal.getCity().equals(selectedCity)) {
                    //then add this is filtered list
                    arrListFilter.add(dbModal);
                }
            }
        }

        Collections.shuffle(arrListFilter);
        if (arrListFilter.size() == 0) {
            tvNoDonorFound.setVisibility(View.VISIBLE);
        } else {
            tvNoDonorFound.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        auth.removeAuthStateListener(authStateListener);
        if (donorsRef != null) {
            donorsRef.removeEventListener(childEventListener);
        }
    }


    //display alert dialog when user return from call to donor
    @Override
    public void onResume() {
        super.onResume();
        if (firebaseUser != null) {
            SharedPreferences preferences = getActivity().getSharedPreferences(firebaseUser.getEmail(), Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();
            if (preferences.contains("calling donor")) {
                String donorKey = preferences.getString("calling donor", null);
                Log.d("xyz", "donor key :" + donorKey);
                DatabaseReference ref = usersRef.child(donorKey);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DbModal dbModal = dataSnapshot.getValue(DbModal.class);
                        Toast.makeText(getContext(), "you have called: " + dbModal.getName(), Toast.LENGTH_SHORT).show();
                        showCallHistoryDialog(dbModal);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

                edit.clear();
                edit.apply();
            }
        }
    }

    private void showCallHistoryDialog(final DbModal dbModal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setTitle("you have called " + dbModal.getName() + ", do you want to set remainder");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showDateTimePicker();
            }
        });
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDateTimePicker() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.date_time_picker);
        datePicker = (DatePicker) dialog.findViewById(R.id.date_picker);
        timePicker = (TimePicker) dialog.findViewById(R.id.time_picker);
        btnSetAlarm = (Button) dialog.findViewById(R.id.btn_set);
        btnSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "remainder set", Toast.LENGTH_SHORT).show();
                setAlarm(datePicker, timePicker);
            }
        });
        dialog.show();
    }

    private void setAlarm(DatePicker datePicker, TimePicker timePicker) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), timePicker.getCurrentHour(), timePicker.getCurrentMinute());
        Intent intent = new Intent(getContext(), Alarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(), 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                pendingIntent);
    }

    //click to show call dialog;
    @Override
    public void rvOnclick(int position) {
        customCallDialog = getActivity().getLayoutInflater().inflate(R.layout.custome_call_dialog, null, false);
        final DbModal modal = arrListFilter.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("data",modal);
        FragmentManager fragmentManager = getFragmentManager();
        CustomCallDialog customCall = new CustomCallDialog();
        customCall.setArguments(bundle);
        customCall.show(fragmentManager,"callDialog");
    }

    //click on organization name; display organization profile; click org name
    /*@Override
    public void rvOnClickOrgName(String orgId) {
        Fragment fragment = new FragmentOrgDisplay();
        Bundle bundle = new Bundle();
        bundle.putString("orgId", orgId);
        fragment.setArguments(bundle);
        //calling mainActivity method to replace fragment
        ((MainActivity) getActivity()).mReplaceFragment(fragment, Constants.FRAGMENT_ORGANIZATION_PROFILE, false);
    }*/

    @Override
    public void addToFollowUp(int position) {
        if (isSignedIn) {
            DbModal modal = arrListFilter.get(position);
            followUpRef.child(firebaseUser.getUid()).child(modal.getId()).setValue(modal.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), R.string.msg_added_in_follow_up, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.msg_error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), R.string.msg_sign_in_must, Toast.LENGTH_SHORT).show();
        }
    }
}