package com.kartikey.howchat;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class ActivityUsers extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUserList;
    private DatabaseReference mRec;
    static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mContext = getApplicationContext();

        mToolbar = findViewById(R.id.users_appbar);
        mUserList = findViewById(R.id.users_list);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d("start",  "starting");
        mRec= FirebaseDatabase.getInstance().getReference().child("Users");
        mRec.keepSynced(true);
        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                mRec
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, Users model, int position) {

                final String userid = getRef(position).getKey();
                viewHolder.setName(model.name);
                viewHolder.setStatus(model.status);
                if(!model.thumb_image.equals("default")) {
                    viewHolder.setImage(model.thumb_image);
                }

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(ActivityUsers.this, ActivityProfile.class);
                        profileIntent.putExtra("userId", userid);
                        startActivity(profileIntent);
                    }
                });
            }
        };
        mUserList.setAdapter(firebaseRecyclerAdapter);



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
