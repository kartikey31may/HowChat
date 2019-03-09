package com.kartikey.howchat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionPagerAdapter;
    private TabLayout mTabLayout;

    private DatabaseReference mUserRef;
    public static Context mContext;

    private List<Users2> UsersList = new ArrayList<>();

    int counter=0, countUser=0;


    //private MaterialSearchView materialSearchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //materialSearchView = findViewById(R.id.search_view);
        mAuth = FirebaseAuth.getInstance();
        mContext = getApplicationContext();

        mToolbar =findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("HowChat");

        mViewPager = findViewById(R.id.tab_pager);
        mSectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionPagerAdapter);

        mTabLayout = findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);


        /*materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(MainActivity.this, query, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });*/

    }

    @Override
    public void onStart(){
        super.onStart();
        //Check If user is Signed in (Non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            Intent startIntent = new Intent(MainActivity.this, ActivityStart.class);
            startActivity(startIntent);
            finish();

        }else{
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUserRef.child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mAuth.getCurrentUser()!=null){
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        /*MenuItem item = menu.findItem(R.id.action_search);
        materialSearchView.setMenuItem(item);*/

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);

         if(item.getItemId() == R.id.main_logout){
             if(mAuth.getCurrentUser()!=null){
                 mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                 mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
             }
             FirebaseAuth.getInstance().signOut();
             finish();
             startActivity(getIntent());
         }
         if(item.getItemId()==R.id.main_setting){
             Intent settingIntent = new Intent(MainActivity.this, ActivitySettings.class);
             startActivity(settingIntent);
         }
        if(item.getItemId()==R.id.main_user){
            Intent settingIntent = new Intent(MainActivity.this, ActivityUsers.class);
            startActivity(settingIntent);
        }
        if (item.getItemId()==R.id.action_search){
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_search, null);
            final ProgressBar progressBar = dialogView.findViewById(R.id.dialog_search_progressbar);
            final RecyclerView recyclerView = dialogView.findViewById(R.id.dialog_search_recycler);
            final TextInputLayout searchInput = dialogView.findViewById(R.id.dialog_search_input);
            Button btn = dialogView.findViewById(R.id.dialog_search_btn);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UsersList = new ArrayList<>();
                    final String input = searchInput.getEditText().getText().toString();
                    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Users");


                    if(!input.equals("")){
                        recyclerView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.VISIBLE);

                        counter=0;
                        countUser=0;
                        mRef.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                String name = dataSnapshot.child("name").getValue().toString();
                                String status = dataSnapshot.child("status").getValue().toString();
                                String image = dataSnapshot.child("image").getValue().toString();
                                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                                String imageURL = dataSnapshot.child("imageURL").getValue().toString();
                                String id = dataSnapshot.getKey();

                                if(input.equals(name)) {
                                    countUser++;
                                    Users2 u = new Users2(name, status, image, thumb_image, imageURL, id);
                                    UsersList.add(u);
                                }else if(name.contains(input)&&input.length()>3){
                                    countUser++;
                                    Users2 u = new Users2(name, status, image, thumb_image, imageURL, id);
                                    UsersList.add(u);
                                }
                                if(counter++>dataSnapshot.getChildrenCount()){
                                    progressBar.setVisibility(View.GONE);
                                    if(countUser==0){
                                        Toast.makeText(MainActivity.this, "No User Found", Toast.LENGTH_SHORT).show();
                                    }else
                                    Toast.makeText(MainActivity.this, "Search Completed", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        UsersAdapter mAdapter = new UsersAdapter(UsersList);
                        recyclerView.setAdapter(mAdapter);


                    }
                }
            });

            mBuilder.setView(dialogView);
            AlertDialog dialog = mBuilder.create();
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {

                }
            });
            dialog.show();
        }

        return true;
    }
    public class UsersAdapter extends RecyclerView.Adapter<UsersViewHolder>{

        private List<Users2> usersList;

        public UsersAdapter(List<Users2> users){
            usersList = users;
        }

        @NonNull
        @Override
        public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_single_layout, parent, false);

            return new UsersViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
            final Users2 u = usersList.get(position);
            holder.setName(u.name);
            holder.setStatus(u.status);
            if(!u.thumb_image.equals("default")) {
                holder.setImage(u.thumb_image);
            }
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent profileIntent = new Intent(mContext, ActivityProfile.class);
                    profileIntent.putExtra("userId", u.id);
                    startActivity(profileIntent);
                }
            });
        }



        @Override
        public int getItemCount() {
            return usersList.size();
        }
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
        }

        public void setName(String name) {
            TextView mName = mView.findViewById(R.id.users_name);
            mName.setText(name);
        }


        public void setStatus(String status) {
            TextView mStatus = mView.findViewById(R.id.users_status);
            mStatus.setText(status);
        }
        public void setImage(String url){
            ImageView mImage = mView.findViewById(R.id.users_image);
            Glide.with(mContext).load(url).into(mImage);
        }

    }

}
