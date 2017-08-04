package com.example.minhajlib.elaaj1;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentOrganization extends Fragment {

    /*
    declaring variables
     */
    Boolean isSignIn, isVerified, isOrg, isLinkOrg, isHide;
    String mUri, orgType, orgName, orgNumber, userId;
    ArrayList<String> path;
    Constants mConstants;
    Uri uri;

    //views
    EditText etOrgName, etOrgNumber, etOrgId, etOrgDescription, etOrgEamil, etOrgFb, etOrgWeb;
    RadioGroup rgOrgType;
    RadioButton rbOrgPrivate, rbOrgGovt;
    ImageView ivOrgLogo;
    Button btnCreate;
    Switch switchDonor, switchLinkOrg;
    TextView tvChangeCover, tvNowDonor, tvNowLink;
    RelativeLayout switchLinkOrgContainer;

    //firebase
    StorageReference storageReference;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener;

    /*
    constructor
     */
    public FragmentOrganization() {
        // Required empty public constructor
        mConstants = Constants.getInstance();
        path = new ArrayList<>();           //to stores ids of donors
        mUri = "";                          //path of logo
        orgName = "";
        orgType = "";
        userId = "";
        isOrg = false;
        isHide = true;
        isSignIn = false;
        isVerified = false;
        isLinkOrg = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_organization, container, false);

        /*
          initializing components
          */
        //views
        etOrgName = (EditText) view.findViewById(R.id.et_org_name);
        etOrgNumber = (EditText) view.findViewById(R.id.et_org_number);
        etOrgId = (EditText) view.findViewById(R.id.et_org_id);
        etOrgDescription = (EditText) view.findViewById(R.id.et_org_description);
        etOrgEamil = (EditText) view.findViewById(R.id.et_org_email);
        etOrgFb = (EditText) view.findViewById(R.id.et_org_fb);
        etOrgWeb = (EditText) view.findViewById(R.id.et_org_website);
        rgOrgType = (RadioGroup) view.findViewById(R.id.rg_org_type);
        rbOrgGovt = (RadioButton) view.findViewById(R.id.rb_org_govt);
        rbOrgPrivate = (RadioButton) view.findViewById(R.id.rb_org_private);
        ivOrgLogo = (ImageView) view.findViewById(R.id.iv_org_logo);
        btnCreate = (Button) view.findViewById(R.id.btn_create);
        switchDonor = (Switch) view.findViewById(R.id.switch_donors);
        switchLinkOrg = (Switch) view.findViewById(R.id.switch_link_org);
        switchLinkOrgContainer = (RelativeLayout) view.findViewById(R.id.switch_link_org_container);
        tvChangeCover = (TextView) view.findViewById(R.id.tv_change_cover);
        tvNowDonor = (TextView) view.findViewById(R.id.tv_now_donor);
        tvNowLink = (TextView) view.findViewById(R.id.tv_now_link);

        /*
         * listeners for views
         */
        rgOrgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.rb_org_private) {
                    orgType = Constants.PRIVATE;
                } else if (checkedId == R.id.rb_org_govt) {
                    orgType = Constants.GOVT;
                }
            }
        });

        switchDonor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //Toast.makeText(getActivity(), "showing donor", Toast.LENGTH_SHORT).show();
                    tvNowDonor.setVisibility(View.VISIBLE);
                    //isHide = false;
                } else {
                    //Toast.makeText(getActivity(), "hiding donor", Toast.LENGTH_SHORT).show();
                    tvNowDonor.setVisibility(View.GONE);
                    //isHide = true;
                }
            }
        });

        switchLinkOrg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //isLinkOrg = isChecked;
                if (isChecked) {
                    tvNowLink.setVisibility(View.VISIBLE);
                } else {
                    tvNowLink.setVisibility(View.GONE);
                }
            }
        });

        //start gallery intent to choose photo
        tvChangeCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConstants.displayToast(getContext(), "choose horizontal image", Toast.LENGTH_SHORT);
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 102);
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignIn) {
                    createOrg();
                } else {
                    mConstants.displayToast(getContext(), getString(R.string.msg_sign_in_first), Toast.LENGTH_SHORT);
                }
            }
        });

        /**
         * firebase
         */
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.DB_ORGANIZATION);
        storageReference = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser == null) {
                    //sign out
                    isSignIn = false;
                } else {
                    //sign in
                    isSignIn = true;
                    userId = firebaseUser.getUid();
                    checkAlreadyOrganization();             //check if user has organization
                }
            }
        };
        auth.addAuthStateListener(authStateListener);
        return view;
    }

    private void checkAlreadyOrganization() {
        //if already have organization fill ui with organization data
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DbModalOrg dbModalOrg = dataSnapshot.getValue(DbModalOrg.class);
                if (dbModalOrg != null) {
                    //user have organization
                    isOrg = true;
                    updateUi(dbModalOrg);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }


    //fill data of organization
    private void updateUi(final DbModalOrg dbModalOrg) {

        orgName = dbModalOrg.getOrgName();
        orgNumber = dbModalOrg.getOrgNumber();
        etOrgName.setText(orgName);
        etOrgNumber.setText(orgNumber);
        etOrgId.setText(dbModalOrg.getOrgRegistrationId());
        etOrgDescription.setText(dbModalOrg.getDescription());
        etOrgEamil.setText(dbModalOrg.getOrgEmail());
        etOrgFb.setText(dbModalOrg.getOrgFb());
        etOrgWeb.setText(dbModalOrg.getOrgWeb());
        if (dbModalOrg.getOrgType().equals("Private")) {
            rbOrgPrivate.setChecked(true);
        } else if (dbModalOrg.getOrgType().equals("Govt")) {
            rbOrgGovt.setChecked(true);
        }
        //loading logo
        mUri = dbModalOrg.getOrgLogoUrl();
        if (!mUri.isEmpty()) {
            mConstants.downloadWithPicassoOrgLogo(getContext(), mUri, ivOrgLogo);
        }
        //setting switch state
        if (dbModalOrg.getHide()) {
            switchDonor.setChecked(false);
            isHide = true;
        } else {
            switchDonor.setChecked(true);
            isHide = false;
        }

        if (dbModalOrg.getVerified() != null) {
            isVerified = dbModalOrg.getVerified();
            if (isVerified) {
                switchLinkOrgContainer.setVisibility(View.VISIBLE);
                isLinkOrg = dbModalOrg.getLinkToOrg();
                switchLinkOrg.setChecked(isLinkOrg);
            } else {
                switchLinkOrgContainer.setVisibility(View.GONE);
                isLinkOrg = false;
            }
        }
        //if already have organization created then replace the text of button
        btnCreate.setText("update");
    }

    private void createOrg() {

        //before insert in database,validate data
        if (isValidData()) {
            if (isOrg)
                detectChange();     //if name,number,show donor,link to org is changed
            insertDb();
        }
        hideKeyBoard();
    }

    private boolean isValidData() {

        boolean isValid = true;
        if (etOrgName.getText().toString().trim().length() == 0) {
            etOrgName.setError(getString(R.string.error_fill_name));
            isValid = false;
        }
        if (etOrgNumber.getText().toString().trim().length() == 0) {
            etOrgNumber.setError(getString(R.string.error_number));
            isValid = false;
        }
        return isValid;
    }

    //hidding keyboard
    private void hideKeyBoard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void detectChange() {
        Boolean isChange = false;
        if (!(isHide == !switchDonor.isChecked())) {
            Log.d("xyz", "donor hide change");
            isChange = true;
        } else if (!orgName.equals(etOrgName.getText().toString())) {
            Log.d("xyz", "org name change");
            isChange = true;
        } else if (!orgNumber.equals(etOrgNumber.getText().toString())) {
            Log.d("xyz", "org number change");
            isChange = true;
        } else if (!(isLinkOrg == switchLinkOrg.isChecked())) {
            Log.d("xyz", "is link to org change");
            isChange = true;
        }

        if (isChange) {
            loadOrgDonors();
        }
    }

    //getting donors created by this user
    private void loadOrgDonors() {
        FirebaseDatabase.getInstance().getReference(Constants.DB_USERS).orderByChild(Constants.ASSOCIATION)
                .equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                path.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    DbModal dbModal = data.getValue(DbModal.class);
                    //adding id of created donors to use later when update; these ids will be used as path to specific donor
                    path.add(dbModal.getId());
                    //Log.d("xyz", "update path size:" + path.size());
                }
                updateOrgDonors();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    //updating value of isDonor to hide/show donor
    private void updateOrgDonors() {

        String org = "";
        if (switchLinkOrg.isChecked()) {
            org = etOrgName.getText().toString();
        }
        final ProgressDialog progressDialog = mConstants.showProgressDialog(getContext(), getString(R.string.msg_wait));
        HashMap<String, Object> update = new HashMap<>();
        for (String id : path) {

            //path: root/users/user_id/isDonor
            update.put("/" + Constants.DB_USERS + "/" + id + "/" + Constants.IS_DONOR, String.valueOf(switchDonor.isChecked()));
            update.put("/" + Constants.DB_USERS + "/" + id + "/" + Constants.ORG_NAME, org);
            update.put("/" + Constants.DB_USERS + "/" + id + "/" + Constants.USER_CONTACT, etOrgNumber.getText().toString());
        }
        FirebaseDatabase.getInstance().getReference().updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.hide();
                if (task.isSuccessful()) {
                    mConstants.displayToast(getContext(), getString(R.string.msg_success_update), Toast.LENGTH_SHORT);
                }
            }
        });
    }

    public void insertDb() {

        final ProgressDialog progressDialog = mConstants.showProgressDialog(getContext(), getString(R.string.msg_wait));
        DbModalOrg dbModalOrg = new DbModalOrg(
                userId
                , etOrgName.getText().toString()
                , etOrgNumber.getText().toString()
                , etOrgId.getText().toString()
                , etOrgDescription.getText().toString()
                , etOrgEamil.getText().toString()
                , etOrgFb.getText().toString()
                , etOrgWeb.getText().toString()
                , orgType
                , ""
                , mUri
                , !switchDonor.isChecked()
                , switchLinkOrg.isChecked()
                , isVerified);

        databaseReference.child(userId).setValue(dbModalOrg)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.hide();
                        if (task.isSuccessful()) {
                            try {
                                ((MainActivity) getActivity()).mReplaceDonorsList();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            mConstants.displayToast(getContext(), getString(R.string.msg_success_update), Toast.LENGTH_SHORT);
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102 && resultCode == ResultCodes.OK) {
            uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 1024, 512, false);
                uploadPhoto(scaledBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadPhoto(Bitmap bit) {
        //if internet is not available then return
        if (!mConstants.isInternetAvailable()) {
            mConstants.displayToast(getContext(), "please connect internet", Toast.LENGTH_LONG);
            return;
        }
        mConstants.showProgressDialog(getContext(), getString(R.string.msg_wait));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] data = baos.toByteArray();

        final StorageReference sr = storageReference.child("images/" + firebaseUser.getUid()).child("logo");
        sr.putBytes(data)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        mConstants.hideProgressDialog();
                        if (task.isSuccessful()) {
                            sr.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    mUri = uri.toString();
                                    mConstants.downloadWithPicassoOrgLogo(getContext(), mUri, ivOrgLogo);
                                    //mConstants.displayToast(getContext(), "Press update", Toast.LENGTH_LONG);
                                }
                            });
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        auth.removeAuthStateListener(authStateListener);
    }
}