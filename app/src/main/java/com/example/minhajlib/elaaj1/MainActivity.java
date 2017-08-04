package com.example.minhajlib.elaaj1;

import android.app.Activity;
import android.content.Intent;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

import static android.R.attr.data;
import static android.R.attr.tag;
import static android.R.attr.track;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /***
     * declaring variable
     */

    private final int RC = 1;

    private Constants constants;
    private int navMenuSize;
    private DbModal mDbModal;

    //Views
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MenuItem menuSignOut;
    private MenuItem menuSignIn;
    private MyDrawerToggle myDrawerToggle;
    private Menu menu;
    private CircularImageView imageView;
    private TextView textView;


    //Firebase
    private FirebaseAuth auth;
    private FirebaseUser user;
    ValueEventListener valueEventListener;

    //declaring fragment variables
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener;

    //attaching listeners
    @Override
    protected void onStart() {
        super.onStart();
        navigationView.setNavigationItemSelectedListener(this);
        fragmentManager.addOnBackStackChangedListener(mOnBackStackChangedListener);
    }//end onStart

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * initializing components
         */
        constants = Constants.getInstance();

        //Views
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        myDrawerToggle = new MyDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(myDrawerToggle);
        imageView = (CircularImageView) navigationView.getHeaderView(0).findViewById(R.id.header_img_view);
        textView = (TextView) navigationView.getHeaderView(0).findViewById(R.id.header_tv_name);
        menu = navigationView.getMenu();
        navMenuSize = menu.size();
        menuSignIn = menu.findItem(R.id.menu_sign_in);
        menuSignOut = menu.findItem(R.id.menu_sign_out);

        //Fragment
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new FragmentDonorsList(), Constants.FRAGMENT_DONORS_LIST);
        fragmentTransaction.commit();
        mOnBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {

                Log.d("xyz", "back stack");
                String tag = "";//Constants.FRAGMENT_DONORS_LIST;
                //getting all fragment that are in stack
                List<Fragment> fragmentList = fragmentManager.getFragments();
                if (fragmentList != null) {
                    for (Fragment fragment : fragmentList) {
                        if (fragment != null && fragment.isVisible())
                            tag = fragment.getTag();
                    }
                }
                setToolBarTitle(tag);
            }
        };

        //setting toolbar; displaying donors list fragment on app launch
        setToolBarTitle(Constants.FRAGMENT_DONORS_LIST);

        //Firebase
        auth = FirebaseAuth.getInstance();
        //checking for user session
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                //checking for current user
                if (user != null) {
                    FirebaseDatabase.getInstance()
                            .getReference(Constants.DB_USERS)
                            .child(user.getUid())
                            .addValueEventListener(valueEventListener);
                    //toggle between sign menu
                    menuSignIn.setVisible(false);
                    menuSignOut.setVisible(true);
                    mReplaceDonorsList();
                } else {
                    menuSignIn.setVisible(true);
                    menuSignOut.setVisible(false);
                    imageView.setImageResource(R.drawable.user);
                    textView.setText("");
                    mDbModal = null;
                    mReplaceDonorsList();
                }
            }
        });// end listener

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    mDbModal = dataSnapshot.getValue(DbModal.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
    }//end onCreate

    private void setToolBarTitle(String tag) {

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(tag);
        //highlight the menu item of drawer
        switch (tag) {
            case Constants.FRAGMENT_DONORS_LIST:
                setNavMenuItemSelected(0);
                break;
            case Constants.FRAGMENT_PROFILE:
                setNavMenuItemSelected(1);
                break;
            case Constants.FRAGMENT_MY_DONORS:
                setNavMenuItemSelected(2);
                break;
            case Constants.FRAGMENT_CREATED_DONORS_LIST:
                setNavMenuItemSelected(3);
                break;
            case Constants.FRAGMENT_CREATE_ORGANIZATION:
                setNavMenuItemSelected(4);
                break;
            case Constants.FRAGMENT_ORGANIZATION_LIST:
                setNavMenuItemSelected(5);
                break;
            default:
                setNavMenuItemSelected(-1);
                break;
        }

    }//end method

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
        } else if (!isDonorFragment() && fragmentManager.getBackStackEntryCount() == 0) {
            mReplaceDonorsList();
        } else {
            super.onBackPressed();
        }
    }

    private boolean isDonorFragment() {
        Fragment fragment = fragmentManager.findFragmentByTag(Constants.FRAGMENT_DONORS_LIST);
        return fragment != null;
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        myDrawerToggle.syncState();
    }

    //call back method for navigation menu
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //getting selected menu id
        int selectedMenuId = item.getItemId();

        switch (selectedMenuId) {
            case R.id.menu_sign_in:     //if sign in menu is pressed
                signInUser();
                break;
            case R.id.menu_sign_out:    //if sign out is pressed
                signOutCurrentUser();
                break;
            case R.id.menu_donor_list:
                mReplaceDonorsList();      //false means don't check for user login
                break;
            case R.id.menu_profile:
                mReplaceFragment(new FragmentProfile(), Constants.FRAGMENT_PROFILE, true);      //true means check first user login
                break;
            case R.id.menu_follow_up:
                mReplaceFragment(new FragmentFollowUp(), Constants.FRAGMENT_MY_DONORS, true);
                break;
            case R.id.menu_create_donor:
                mReplaceFragment(new FragmentCreatedDonors(), Constants.FRAGMENT_CREATED_DONORS_LIST, true);
                break;
            case R.id.menu_organization:
                mReplaceFragment(new FragmentOrganization(), Constants.FRAGMENT_CREATE_ORGANIZATION, true);
                break;
            case R.id.menu_organization_list:
                mReplaceFragment(new FragmentOrgsList(), Constants.FRAGMENT_ORGANIZATION_LIST, false);
                break;
        }
        return true;
    }//end callback method for navigation menu

    //method to sign in user; start sign in flow
    private void signInUser() {

        //start sign in activity
        startActivityForResult(new Intent(this, SignInActivity.class), RC);
        drawerLayout.closeDrawers();
    }

    /*
    signOutCurrentUser
     */
    private void signOutCurrentUser() {

        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    constants.displayToast(MainActivity.this, getString(R.string.msg_sign_out), Toast.LENGTH_SHORT);
                    drawerLayout.closeDrawers();
                    mReplaceDonorsList();
                }
            }
        });
    }

    /*
    mReplaceFragment
     */
    public void mReplaceFragment(Fragment fragment, String tag, Boolean isCheckSignIn) {
        //code to replace fragment

        if (isCheckSignIn) {
            if (user == null) {
                constants.displayToast(this, getString(R.string.sign_in_first), Toast.LENGTH_SHORT);
                signInUser();
                return;
            }
        }

        if (fragmentManager == null) {
            fragmentManager = getSupportFragmentManager();
        }
        while (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStackImmediate();
        }

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentManager.popBackStackImmediate(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();

        //adding some transtactions in back stack
        if (tag.equals(Constants.FRAGMENT_CREATED_DONORS_PROFILE) || tag.equals(Constants.FRAGMENT_ORGANIZATION_PROFILE)) {
            fragmentTransaction.addToBackStack(tag);
        }

        setToolBarTitle(tag);
        if (drawerLayout != null)
            drawerLayout.closeDrawers();
    }//end mReplaceFragment

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC && resultCode == RESULT_OK) {
            mReplaceDonorsList();
        }
    }

    /***
     * detaching listeners
     */
    @Override
    protected void onStop() {
        super.onStop();
        navigationView.setNavigationItemSelectedListener(null);
        fragmentManager.removeOnBackStackChangedListener(mOnBackStackChangedListener);
    }

    public void mReplaceDonorsList() {

        fragmentManager = getSupportFragmentManager();
        while (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStackImmediate();
        }
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, new FragmentDonorsList(), Constants.FRAGMENT_DONORS_LIST);
        fragmentTransaction.commitAllowingStateLoss();
        setToolBarTitle(Constants.FRAGMENT_DONORS_LIST);
        if (drawerLayout != null)
            drawerLayout.closeDrawers();
    }

    /*
        MyDrawerToggle
         */
    private class MyDrawerToggle extends ActionBarDrawerToggle {

        public MyDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, @StringRes int openDrawerContentDescRes, @StringRes int closeDrawerContentDescRes) {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            String photo;
            if (mDbModal != null) {
                photo = mDbModal.getPhotoUrl();
                textView.setText(mDbModal.getName());
                if (photo != null)
                    constants.downloadWithPicasso(MainActivity.this, photo, imageView);
            }
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            super.onDrawerSlide(drawerView, slideOffset);
        }

    }//end class MyDrawerToggle

    /*
      setNavigationItemSelected
      highlight menu item
     */
    private void setNavMenuItemSelected(int position) {
        for (int i = 0; i < navMenuSize; i++) {
            if (i == position) {
                menu.getItem(i).setChecked(true);
            } else {
                menu.getItem(i).setChecked(false);
            }
        }
    }
}