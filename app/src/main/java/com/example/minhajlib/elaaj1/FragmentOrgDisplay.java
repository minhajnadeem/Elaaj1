package com.example.minhajlib.elaaj1;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentOrgDisplay extends Fragment {

    private String number;

    Toolbar mToolbar;
    ImageView ivOrgLogo;
    TextView tvOrgName,tvOrgDescription,tvOrgFb;
    Button btnCall;

    public FragmentOrgDisplay() {
        // Required empty public constructor
        number = "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_org_display, container, false);
        Bundle bundle = getArguments();

        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ivOrgLogo = (ImageView) view.findViewById(R.id.iv_org_logo);
        tvOrgName = (TextView) view.findViewById(R.id.tv_org_name);
        tvOrgDescription = (TextView) view.findViewById(R.id.tv_org_description);
        tvOrgFb = (TextView) view.findViewById(R.id.tv_org_fb);
        btnCall = (Button) view.findViewById(R.id.btn_call);
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("tel:"+number));
                startActivity(intent);
            }
        });

        if(bundle != null){
            FirebaseDatabase.getInstance().getReference(Constants.DB_ORGANIZATION).child(bundle.getString(Constants.ORG_ID))
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final DbModalOrg dbModalOrg = dataSnapshot.getValue(DbModalOrg.class);
                            Constants.getInstance().downloadWithPicassoOrgLogo(getContext(),dbModalOrg.getOrgLogoUrl(),ivOrgLogo);
                            setToolbarTitle(dbModalOrg.getOrgName());
                            tvOrgFb.setText(dbModalOrg.getOrgFb());
                            number = dbModalOrg.getOrgNumber();
                            tvOrgDescription.setText(dbModalOrg.getDescription());
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
        }

        return view;
    }

    private void setToolbarTitle(String orgName) {
        mToolbar.setTitle(orgName);
    }
}