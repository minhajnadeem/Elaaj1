package com.example.minhajlib.elaaj1;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static android.R.attr.bitmap;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCreatedDonorsProfile extends Fragment {

    //declaring variables
    private String[] strArrBloodGroups;
    private String[] strArrCity;
    private String availableTime, bloodPrice, selectedCity, assocId, orgName, tempOrgName, mUri, donorId;
    private Boolean isSignIn, isPaid;
    private Constants mConstants;
    private Bitmap profilePhoto;
    private Uri uri;

    //views variables
    ImageView imageView;
    TextInputEditText etName, etNumber, etAddress, etBloodGroup, etCity;
    TextView tvAvailbleTime, tvFreePaid, tvDatePick, tvDonor;
    Button btnUpdate;
    Switch aSwitch, switchLinkOrg;
    FloatingActionButton fab;
    Context context;
    LinearLayout switchLinkOrgContainer;

    //firebase
    String userUId;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    FirebaseUser firebaseUser;
    OnCompleteListener<Void> onCompleteListener;
    Task<Void> completeTask;
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener;


    public FragmentCreatedDonorsProfile() {
        // Required empty public constructor
        mConstants = Constants.getInstance();
        mUri = "";
        donorId = "";
        orgName = "";
        tempOrgName = "";
        isPaid = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        auth.removeAuthStateListener(authStateListener);
        if (completeTask != null)
            completeTask.addOnCompleteListener(getActivity(), null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_created_donors_profile, container, false);
        context = getContext();
        strArrBloodGroups = getResources().getStringArray(R.array.blood_group);
        strArrCity = getResources().getStringArray(R.array.city);
        //initializing views

        imageView = (ImageView) view.findViewById(R.id.iv_profile_img);
        etName = (TextInputEditText) view.findViewById(R.id.et_profile_name);
        etAddress = (TextInputEditText) view.findViewById(R.id.et_profile_address);
        etNumber = (TextInputEditText) view.findViewById(R.id.et_profile_number);
        etBloodGroup = (TextInputEditText) view.findViewById(R.id.et_blood_group);
        etCity = (TextInputEditText) view.findViewById(R.id.et_city);
        tvAvailbleTime = (TextView) view.findViewById(R.id.tv_available_time);
        tvDatePick = (TextView) view.findViewById(R.id.btn_date_pick);
        tvFreePaid = (TextView) view.findViewById(R.id.tv_free_paid);
        tvDonor = (TextView) view.findViewById(R.id.tv_donor);
        aSwitch = (Switch) view.findViewById(R.id.switch_show_in_list);
        switchLinkOrg = (Switch) view.findViewById(R.id.switch_link_org);
        switchLinkOrgContainer = (LinearLayout) view.findViewById(R.id.switch_link_org_container);
        btnUpdate = (Button) view.findViewById(R.id.btn_update);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);

        /*
        attaching listeners
         */
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 100);
            }
        });

        etBloodGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBloodGroupDialog();
            }
        });

        etCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCityDialog();
            }
        });

        //make me donor switch
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //isDonor = isChecked;
                if (isChecked) {
                    tvDonor.setText(R.string.now_donor);
                } else {
                    tvDonor.setText("");
                }
            }
        });

        //link to organization switch
        switchLinkOrg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tempOrgName = orgName;
                } else {
                    tempOrgName = "";
                }
            }
        });

        //set price here
        tvFreePaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPriceSetDialog();
            }
        });

        //set date last blood donation
        tvDatePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDatePickDialog();
            }
        });

        tvAvailbleTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAvailableTimeDialog();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignIn) {
                    updateUser();
                } else {
                    mConstants.displayToast(getContext(), getString(R.string.msg_sign_in_first), Toast.LENGTH_SHORT);
                }
            }
        });//end btn update listener


        //initializing firebase variables
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            assocId = firebaseUser.getUid();
            isSignIn = true;
        } else {
            assocId = "";
            isSignIn = false;
        }

        /*
        1.get information from previous fragment i.e created donor fragment
        2.if bundle is not null it means that user want to update some donor
        3.change the text of button to "update"
         */
        Bundle bundle = this.getArguments();
        if (bundle != null) {

            donorId = bundle.getString("id");
            Log.d("xyz", donorId);
            btnUpdate.setText("Update");
            loadDonorsData();
        } else {

            btnUpdate.setText("Add New Donor");
            checkOrg();
        }

        onCompleteListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mConstants.displayToast(context, getString(R.string.msg_success_update), Toast.LENGTH_SHORT);
                    getActivity().getSupportFragmentManager().popBackStackImmediate();
                    //((MainActivity) getActivity()).mReplaceFragment(new FragmentCreatedDonors(), Constants.FRAGMENT_CREATED_DONORS_LIST, true);
                } else {
                    mConstants.displayToast(context, task.getResult().toString(), Toast.LENGTH_SHORT);
                }
            }
        };
        return view;

    }//end on create

    private void showDatePickDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater1 = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater1.inflate(R.layout.date_picker, null);
        builder.setView(rootView);

        final DatePicker datePicker = (DatePicker) rootView.findViewById(R.id.date_picker);
        setMaxDateOfDatePicker(datePicker);
        Button button = (Button) rootView.findViewById(R.id.btn_set);

        final AlertDialog dialog = builder.create();
        dialog.show();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvDatePick.setText(
                        datePicker.getDayOfMonth() + "/"
                                + (datePicker.getMonth() + 1) + "/"
                                + datePicker.getYear());
                dialog.dismiss();
            }
        });
    }

    private void showAvailableTimeDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final AlertDialog alertDialog;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.available_time_picker, null, false);
        builder.setView(layout);
        alertDialog = builder.create();

        final LinearLayout layoutFrom = (LinearLayout) layout.findViewById(R.id.layout_from_time);
        final LinearLayout layoutTo = (LinearLayout) layout.findViewById(R.id.layout_to_time);
        Button btnFrom = (Button) layout.findViewById(R.id.btn_set_from);
        Button btnTo = (Button) layout.findViewById(R.id.btn_set_to);
        final TimePicker timePickerFrom = (TimePicker) layout.findViewById(R.id.timePickerFrom);
        final TimePicker timePickerTo = (TimePicker) layout.findViewById(R.id.timePickerTo);

        btnFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amPm;
                int hour = timePickerFrom.getCurrentHour();
                int min = timePickerFrom.getCurrentMinute();

                if (hour == 12) {
                    amPm = " PM";
                } else if (hour > 12) {
                    hour = hour - 12;
                    amPm = " PM";
                } else if (hour == 0) {
                    hour = 12;
                    amPm = " AM";
                } else {
                    amPm = " AM";
                }

                availableTime = hour + ":" + min + amPm;
                tvAvailbleTime.setText(availableTime);

                layoutFrom.setVisibility(View.GONE);
                layoutTo.setVisibility(View.VISIBLE);
            }
        });
        btnTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String amPm;
                int hour = timePickerTo.getCurrentHour();
                int min = timePickerTo.getCurrentMinute();

                if (hour > 12) {
                    hour = hour - 12;
                    amPm = " PM";
                } else if (hour == 0) {
                    hour = 12;
                    amPm = " AM";
                } else {
                    amPm = " AM";
                }
                availableTime += " To " + hour + ":" + min + amPm;

                tvAvailbleTime.setText(availableTime);
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

    }//end

    private void loadDonorsData() {

        //Log.d("xyz", "in load donors :" + donorId);
        reference = firebaseDatabase.getReference(Constants.DB_USERS).child(donorId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {

                    final DbModal dbModal = dataSnapshot.getValue(DbModal.class);
                    if (dbModal != null) {
                        etName.setText(dbModal.getName());
                        etAddress.setText(dbModal.getAddress());
                        etNumber.setText(dbModal.getContact());
                        etBloodGroup.setText(dbModal.getBloodGroup());
                        etCity.setText(dbModal.getCity());
                        etAddress.setText(dbModal.getAddress());
                        tvAvailbleTime.setText(dbModal.getTimeAvailable());
                        tvDatePick.setText(dbModal.getLastDonated());
                        mUri = dbModal.getPhotoUrl();
                        aSwitch.setChecked(Boolean.valueOf(dbModal.getIsDonor()));

                        //downloading profile image in imageView
                        if (!mUri.isEmpty()) {
                            mConstants.downloadWithPicassoNoPlaceholder(getContext(), dbModal.getPhotoUrl(), imageView);
                        } else {
                            imageView.setImageResource(R.drawable.user);
                        }

                        isPaid = dbModal.getPaid();
                        bloodPrice = dbModal.getBloodPrice();
                        if (isPaid) {
                            tvFreePaid.setText(String.format("%s Rs/Bottle", dbModal.getBloodPrice()));
                        } else {
                            tvFreePaid.setText("");
                        }

                        //setting the value of link to organization switch
                        if (dbModal.getOrgName().isEmpty()) {
                            switchLinkOrg.setChecked(false);
                        } else {
                            switchLinkOrg.setChecked(true);
                        }

                    }//end if-else dbModal != null
                }
            } //end onDataChanged

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }//end

    private void checkOrg() {
        FirebaseDatabase.getInstance().getReference(Constants.DB_ORGANIZATION).child(assocId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {

                            //Log.d("xyz", "user has organization");
                            DbModalOrg dbModalOrg = dataSnapshot.getValue(DbModalOrg.class);

                            if (dbModalOrg != null) {
                                if (dbModalOrg.getVerified()) {

                                    orgName = dbModalOrg.getOrgName();
                                    tempOrgName = orgName;
                                    aSwitch.setChecked(!dbModalOrg.getHide());
                                    etNumber.setText(dbModalOrg.getOrgNumber());
                                    switchLinkOrgContainer.setVisibility(View.VISIBLE);
                                    switchLinkOrg.setChecked(true);

                                } else {

                                    orgName = "";
                                    tempOrgName = "";
                                    aSwitch.setChecked(true);
                                    etNumber.setText("");
                                    switchLinkOrgContainer.setVisibility(View.GONE);
                                    switchLinkOrg.setChecked(false);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    //set max date;
    private void setMaxDateOfDatePicker(DatePicker datePicker) {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 59);
        datePicker.setMaxDate(cal.getTimeInMillis());
    }

    private void showPriceSetDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog dialog = null;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_set_price, null, false);


        final EditText etPrice = (EditText) layout.findViewById(R.id.et_price);
        Switch switchFreePaid = (Switch) layout.findViewById(R.id.switch_free_paid);
        switchFreePaid.setChecked(isPaid);
        etPrice.setText(bloodPrice);

        builder.setView(layout);
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isPaid && etPrice.getText().toString().trim().length() > 0) {
                    bloodPrice = etPrice.getText().toString().trim();
                    tvFreePaid.setText(String.format("%s Rs/Bottle", bloodPrice));
                } else {
                    tvFreePaid.setText("");
                }
                dialog.dismiss();
            }
        });

        switchFreePaid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isPaid = isChecked;
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    //city dialog; select city
    private void showCityDialog() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_city, null, false);
        ListView listView = (ListView) layout.findViewById(R.id.list_view);
        DialogBloodGroupAdapter adapter = new DialogBloodGroupAdapter(getContext(), R.layout.view_blood_group, strArrCity);
        listView.setAdapter(adapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(layout);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCity = strArrCity[position];
                etCity.setText(selectedCity);
                alertDialog.dismiss();
            }
        });
    }

    //blood group dialog; select blood group
    private void showBloodGroupDialog() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_blood_group, null, false);
        ListView listView = (ListView) layout.findViewById(R.id.list_view);
        DialogBloodGroupAdapter adapter = new DialogBloodGroupAdapter(getContext(), R.layout.view_blood_group, strArrBloodGroups);
        listView.setAdapter(adapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(layout);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etBloodGroup.setText(strArrBloodGroups[position]);
                alertDialog.dismiss();
            }
        });
    }

    //result of image picker
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == ResultCodes.OK) {
            uri = data.getData();
            try {
                profilePhoto = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                if (profilePhoto.getWidth() >= 512 && profilePhoto.getWidth() >= 512)
                    profilePhoto = Bitmap.createScaledBitmap(profilePhoto, 512, 512, false);
                if (!donorId.isEmpty())
                    uploadPhoto(profilePhoto);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadPhoto(Bitmap bitmap) {
        if (bitmap == null) return;
        final Constants constants = Constants.getInstance();
        constants.showProgressDialog(getContext(), getString(R.string.msg_uploading));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] data = baos.toByteArray();

        final StorageReference sr = storageReference.child("images/" + donorId).child("dp");
        sr.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mConstants.displayToast(context, getString(R.string.msg_success_upload), Toast.LENGTH_SHORT);
                        sr.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mUri = uri.toString();
                                imageView.setImageBitmap(profilePhoto);
                            }
                        });
                        constants.hideProgressDialog();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mConstants.displayToast(context, getString(R.string.error_upload), Toast.LENGTH_SHORT);
                constants.hideProgressDialog();
            }
        });
    }

    private void updateUser() {
        if (!isValidData()) {
            //if data is not valid to update then return
            mConstants.displayToast(getContext(), getString(R.string.msg_empty_fields), Toast.LENGTH_SHORT);
            return;
        }
        //if donorId is empty this means that this is new donor to be created so assign new id
        if (donorId.isEmpty()) {
            /*Calendar currentTime = Calendar.getInstance();
            long time = currentTime.getTimeInMillis();*/
            donorId = firebaseDatabase.getReference(Constants.DB_USERS).push().getKey();
            uploadPhoto(profilePhoto);
        }
        hideKeyBoard();
        doUpdateTask();
    }

    private void hideKeyBoard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void doUpdateTask() {
        //get the reference to database
        //String key = firebaseDatabase.getReference(Constants.DB_USERS).push().getKey();
        reference = firebaseDatabase.getReference(Constants.DB_USERS).child(donorId);
        DbModal dbModal = new DbModal(
                donorId
                , mUri
                , etName.getText().toString()
                , etBloodGroup.getText().toString()
                , etCity.getText().toString()
                , etNumber.getText().toString()
                , etAddress.getText().toString().trim() //.length() == 0 ? "address is not specified by donor" : etAddress.getText().toString()
                , aSwitch.isChecked() + ""
                , assocId
                , tvDatePick.getText().toString() //.trim().length() == 0 ? "" : tvDatePick.getText().toString()
                , tempOrgName
                , tvAvailbleTime.getText().toString()
                , isPaid
                , bloodPrice);

        completeTask = reference.setValue(dbModal).addOnCompleteListener(getActivity(), onCompleteListener);
    }

    //validate data; data validation
    private Boolean isValidData() {

        Boolean isValidate = true;
        if (etName.getText().toString().trim().length() == 0) {
            etName.setError("fill name");
            isValidate = false;
        }
        if (etNumber.getText().toString().trim().length() == 0) {
            etNumber.setError("fill number");
            isValidate = false;
        }
        if (etBloodGroup.getText().toString().equals("")) {
            //etBloodGroup.setErrorEnabled(true);
            etBloodGroup.setError("select your blood group");
            isValidate = false;
        }
        if (etCity.getText().toString().equals("")) {
            //etCity.setErrorEnabled(true);
            etCity.setError("select your city");
            isValidate = false;
        }
        if (etAddress.getText().toString().trim().length() == 0) {
            //etCity.setErrorEnabled(true);
            etAddress.setError("Enter Address");
            isValidate = false;
        }
        return isValidate;
    }
}