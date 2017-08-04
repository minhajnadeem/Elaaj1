package com.example.minhajlib.elaaj1;

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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.DatePicker;

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
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentProfile extends Fragment {

    /*
    declaring variables
     */
    //variables
    private  String[] strArrBloodGroups;
    private  String[] strArrCity;
    private String availableTime, bloodPrice;
    private Boolean isSignIn, isPaid;
    private String mUri;
    Bitmap bitmap;
    Constants mConstants;

    // views
    CircularImageView imageView;
    TextInputEditText etName, etNumber, etAddress, etBloodGroup, etCity;
    TextView tvDatePick, tvAvailableTime, tvFreePaid, tvDonor;
    Switch aSwitch;
    Button btnUpdate;
    MainActivity mainActivity;
    FloatingActionButton fab;

    //firebase variables
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser firebaseUser;
    String userUId;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reference;
    ValueEventListener valueEventListener;
    DatabaseReference myRef;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    OnCompleteListener<Void> onCompleteListener;
    Task<Void> completeTask;

    //constructor
    public FragmentProfile() {
        // Required empty public constructor

        mainActivity = (MainActivity) getActivity();
        mConstants = Constants.getInstance();
        mUri = "";
        isPaid = false;
        bloodPrice = "";
    }//end constructor

    /*
    attaching listener
     */
    @Override
    public void onStart() {
        super.onStart();
    }

    /*
    removing any listeners
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //Log.d("xyz", "on destroy view");
        auth.removeAuthStateListener(authStateListener);
        myRef.removeEventListener(valueEventListener);
        if (completeTask != null)
            completeTask.addOnCompleteListener(getActivity(), null);
    }

    /*
       onCreateView
         */
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.d("xyz", "on create view start");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        strArrBloodGroups = getResources().getStringArray(R.array.blood_group);
        strArrCity = getResources().getStringArray(R.array.city);
        /*
        initializing views
         */
        imageView = (CircularImageView) view.findViewById(R.id.iv_profile_img);
        etName = (TextInputEditText) view.findViewById(R.id.et_profile_name);
        etAddress = (TextInputEditText) view.findViewById(R.id.et_profile_address);
        etNumber = (TextInputEditText) view.findViewById(R.id.et_profile_number);
        etBloodGroup = (TextInputEditText) view.findViewById(R.id.et_blood_group);
        etCity = (TextInputEditText) view.findViewById(R.id.et_city);
        btnUpdate = (Button) view.findViewById(R.id.btn_update);
        aSwitch = (Switch) view.findViewById(R.id.switch_show_in_list);
        tvAvailableTime = (TextView) view.findViewById(R.id.tv_available_time);
        tvDatePick = (TextView) view.findViewById(R.id.btn_date_pick);
        tvFreePaid = (TextView) view.findViewById(R.id.tv_free_paid);
        tvDonor = (TextView) view.findViewById(R.id.tv_make_donor);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);

        /*
        attaching listeners to views
         */
        //for changing profile picture; start gallery activity
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 100);
            }
        });

        //for selecting blood group; dialog appears
        etBloodGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBloodGroupDialog();
            }
        });

        //for selecting city; dialog appears
        etCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCityDialog();
            }
        });

        //for update profile; data will store in database
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //user must signed in before updating profile
                if (isSignIn) {
                    updateUser();
                } else {
                    mConstants.displayToast(getContext(), getString(R.string.msg_sign_in_first), Toast.LENGTH_SHORT);
                }
            }
        });//end btn update listener

        //watch make me donor switch state
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tvDonor.setText(R.string.nowDonor);
                } else {
                    tvDonor.setText("");
                }
            }
        });

        //set if user want to donate blood for free or paid; dialog appears; set price
        tvFreePaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPriceSetDialog();
            }
        });

        //select date last blood donated; date picker dialog appears; date pick;last donated
        tvDatePick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        });

        //set the suitable time in which patient can call him; dialog appears; time pick
        tvAvailableTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                //set available "from" time
                btnFrom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String amPm;
                        int hour = timePickerFrom.getCurrentHour();
                        int min = timePickerFrom.getCurrentMinute();
                        //converting 24 hour time into 12 hour
                        if (hour > 12) {
                            hour = hour - 12;
                            amPm = " PM";
                        } else if (hour == 0) {
                            hour = 12;
                            amPm = " AM";
                        } else {
                            amPm = " AM";
                        }

                        availableTime = hour + ":" + min + amPm;
                        tvAvailableTime.setText(availableTime);
                        layoutFrom.setVisibility(View.GONE);        //hiding layout of "from" time picker
                        layoutTo.setVisibility(View.VISIBLE);       //showing layout of "to" time picker
                    }
                });

                //set available "to" time
                btnTo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String amPm;
                        int hour = timePickerTo.getCurrentHour();
                        int min = timePickerTo.getCurrentMinute();

                        //converting 24 hour time into 12 hour
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

                        availableTime += " To " + hour + ":" + min + amPm;     //concatenate string
                        tvAvailableTime.setText(availableTime);
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });

        /*
        initializing firebase variables
         */
        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference(Constants.DB_USERS);
        auth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        //setting value views
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() != null) {
                    isSignIn = true;
                    firebaseUser = firebaseAuth.getCurrentUser();
                    userUId = firebaseUser.getUid();

                    //attaching listener to read user data from database
                    myRef = reference.child(userUId);
                    myRef.addValueEventListener(valueEventListener);

                    etName.setText(firebaseUser.getDisplayName());
                    //Log.d("xyz", "uid" + firebaseUser.getUid());

                } else {
                    //if user is null i.e either sign out
                    isSignIn = false;
                    imageView.setImageResource(R.drawable.user);
                    etName.setText("");
                    etNumber.setText("");
                    etAddress.setText("");
                    etBloodGroup.setText("");
                    etCity.setText("");
                    aSwitch.setChecked(false);
                    tvDatePick.setText("");
                    tvAvailableTime.setText("");
                    tvFreePaid.setText("");

                    myRef.removeEventListener(valueEventListener);
                }
            }
        };
        auth.addAuthStateListener(authStateListener);

        //loading data of user from database
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    DbModal dbModal = dataSnapshot.getValue(DbModal.class);
                    if (dbModal != null) {
                        //some data is available
                        mUri = dbModal.getPhotoUrl();
                        mConstants.downloadWithPicasso(getContext(), mUri, imageView);
                        etName.setText(dbModal.getName());
                        etAddress.setText(dbModal.getAddress());
                        etNumber.setText(dbModal.getContact());
                        etBloodGroup.setText(dbModal.getBloodGroup());
                        etCity.setText(dbModal.getCity());
                        aSwitch.setChecked(Boolean.valueOf(dbModal.getIsDonor()));   //getDonor return string value true/false Boolean.ValueOf() will convert this string value into boolean
                        tvDatePick.setText(dbModal.getLastDonated());
                        tvAvailableTime.setText(dbModal.getTimeAvailable());
                        isPaid = dbModal.getPaid();
                        bloodPrice = dbModal.getBloodPrice();
                        if (isPaid) {
                            tvFreePaid.setText(String.format("%s Rs/Bottle", dbModal.getBloodPrice()));
                        } else {
                            tvFreePaid.setText("");
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };//end listener

        //listener for data upload
        onCompleteListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mConstants.hideProgressDialog();
                if (task.isSuccessful()) {
                    try {
                        mConstants.displayToast(getContext(), getString(R.string.msg_success_update), Toast.LENGTH_LONG);
                        ((MainActivity) getActivity()).mReplaceDonorsList();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                } else {
                    mConstants.displayToast(getContext(), getString(R.string.error_update), Toast.LENGTH_LONG);
                }
            }
        };

        return view;
    }//end onCreateView

    //set max date
    private void setMaxDateOfDatePicker(DatePicker datePicker) {

        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 59);
        datePicker.setMaxDate(cal.getTimeInMillis());
    }


    //dialog appear when user click on edit text; price set dialog
    private void showPriceSetDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog dialog;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_set_price, null, false);
        final EditText etPrice = (EditText) layout.findViewById(R.id.et_price);
        Switch aSwitch = (Switch) layout.findViewById(R.id.switch_free_paid);
        etPrice.setText(String.format("%s", bloodPrice));
        aSwitch.setChecked(isPaid);

        builder.setView(layout);
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isPaid && etPrice.getText().toString().trim().length() > 0) {
                    bloodPrice = etPrice.getText().toString().trim();
                    tvFreePaid.setText(String.format("%s Rs/Bottle", bloodPrice));
                } else {
                    bloodPrice = "";
                    tvFreePaid.setText("");
                }
                dialog.dismiss();
            }
        });

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                etCity.setText(strArrCity[position]);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == ResultCodes.OK) {
            Uri galleryImgUri = data.getData();
            try {

                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), galleryImgUri);
                //Log.d("xyz", "bitmap size wXh" + bitmap.getHeight() + bitmap.getWidth());
                if (bitmap.getWidth() >= 512 || bitmap.getWidth() >= 512)
                    bitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, false);
                //Log.d("xyz", "bitmap size wXh" + bitmap.getHeight() + bitmap.getWidth());
                uploadPhoto();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadPhoto() {

        mConstants.showProgressDialog(getContext(), getString(R.string.msg_uploading));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] data = baos.toByteArray();

        final StorageReference sr = storageReference.child("images/" + firebaseUser.getEmail()).child("dp");
        sr.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mConstants.hideProgressDialog();
                        sr.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //Log.d("xyz", "photo :" + uri);
                                mUri = uri.toString();
                                imageView.setImageBitmap(bitmap);
                                //mConstants.downloadWithPicasso(getContext(), mUri, imageView);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mConstants.hideProgressDialog();
                        Toast.makeText(mainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUser() {

        //validate data first
        if (!isValidData()) {
            //if data is not valid to update then return
            mConstants.displayToast(getContext(), getString(R.string.msg_empty_fields), Toast.LENGTH_SHORT);
        } else {
            updateUserProfile();
            hideKeyBoard();
        }
    }

    private void hideKeyBoard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //validate data
    private boolean isValidData() {

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
            etBloodGroup.setError("select your blood group");
            isValidate = false;
        }
        if (etCity.getText().toString().trim().length() == 0) {
            etCity.setError("select your city");
            isValidate = false;
        }

        if (etAddress.getText().toString().trim().length() == 0) {
            //etCity.setErrorEnabled(true);
            etAddress.setError("Enter Address");
            isValidate = false;
        }

        if (etNumber.getText().toString().length() != 11) {

            etNumber.setError("Enter Valid Number");
            isValidate = false;
        }

        return isValidate;
    }

    //update; database; insert
    private void updateUserProfile() {

        mConstants.showProgressDialog(getContext(), getString(R.string.msg_uploading));
        if (!userUId.isEmpty()) {

            DbModal dbModal = new DbModal(
                    userUId
                    , mUri
                    , etName.getText().toString()
                    , etBloodGroup.getText().toString()
                    , etCity.getText().toString()
                    , etNumber.getText().toString()
                    , etAddress.getText().toString() //.trim().length() == 0 ? "address not given" : etAddress.getText().toString()
                    , aSwitch.isChecked() + ""
                    , ""
                    , tvDatePick.getText().toString() //.trim().length() == 0 ? "last donation not given" : tvDatePick.getText().toString()
                    , ""
                    , tvAvailableTime.getText().toString() /*tvFromTime.getText().toString() + "," + tvToTime.getText().toString()*/
                    , isPaid
                    , bloodPrice);
            try {
                myRef.setValue(dbModal).addOnCompleteListener(getActivity(), onCompleteListener);
            } catch (NullPointerException e) {
                e.printStackTrace();
                //Log.d("xyz", "getActivity error");
            }
        } else {
            mConstants.displayToast(getActivity(), "failed to update try later", Toast.LENGTH_SHORT);
        }
    }
}