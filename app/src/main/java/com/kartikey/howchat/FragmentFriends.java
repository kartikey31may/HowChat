package com.kartikey.howchat;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */


public class FragmentFriends extends Fragment {

    private RecyclerView mFriendList;

    private DatabaseReference mFriendDatabase;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    private ProgressDialog mProgressDialog;

    private static Context mContext;

    public FragmentFriends() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       mMainView =  inflater.inflate(R.layout.fragment_fragment_friends, container, false);

       mFriendList = mMainView.findViewById(R.id.friends_recycler_list);
       mAuth = FirebaseAuth.getInstance();

       mContext = getActivity().getApplicationContext();

       mCurrent_user_id = mAuth.getCurrentUser().getUid();

       mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
       mFriendDatabase.keepSynced(true);
       mRootRef = FirebaseDatabase.getInstance().getReference();

       mFriendList.setHasFixedSize(true);
       mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

       mProgressDialog = new ProgressDialog(mMainView.getContext());
        mProgressDialog.setTitle("Loading List of Friends");
        mProgressDialog.setMessage("Please wait while we fetch Friends details");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


       return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, ChatViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, ChatViewHolder>
                (
                        Friends.class,
                        R.layout.users_single_layout,
                        ChatViewHolder.class,
                        mFriendDatabase
                )
        {
            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, Friends model, int position) {

                viewHolder.setStatus(model.date);
                final String UserId = getRef(position).getKey();

                mRootRef.child("Users").child(UserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String display_name = dataSnapshot.child("name").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();
                        final String imageUrl = dataSnapshot.child("thumb_image").getValue().toString();

                        viewHolder.setImage(imageUrl);
                        viewHolder.setName(display_name);

                        if(dataSnapshot.hasChild("online")) {
                            String online = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setOnline(online);
                        }

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which==0){

                                            Intent profileIntent = new Intent(mMainView.getContext(), ActivityProfile.class);
                                            profileIntent.putExtra("userId", UserId);
                                            startActivity(profileIntent);

                                        }else if(which==1){

                                            Intent chatIntent = new Intent(mMainView.getContext(), ActivityChat2.class);
                                            chatIntent.putExtra("userId", UserId);
                                            chatIntent.putExtra("name", display_name);
                                            chatIntent.putExtra("thumb_image", imageUrl);
                                            chatIntent.putExtra("condition", "check");
                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(mMainView.getContext(), databaseError.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        mFriendList.setAdapter(firebaseRecyclerAdapter);
        mProgressDialog.dismiss();

    }

    private static class ChatViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView mImageUser;

        public ChatViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mImageUser = mView.findViewById(R.id.users_image);
        }

        public void setName(String name) {
            TextView mName = mView.findViewById(R.id.users_name);
            mName.setText(name);
        }

        public void setStatus(String date) {
            TextView mStatus = mView.findViewById(R.id.users_status);
            mStatus.setText("Friends from : "+date);
        }
        public void setImage(String url){
            ImageView mImage = mView.findViewById(R.id.users_image);
            Glide.with(mContext).load(url).into(mImage);
        }
        public void setOnline(String online){
            ImageView mImageOnline = mView.findViewById(R.id.users_online);
            TextView mTextOnline = mView.findViewById(R.id.users_online_text);
            if(online.equals("true")){
                mImageOnline.setBackground(mView.getResources().getDrawable(R.drawable.ic_check_circle_online_64dp));
                mTextOnline.setText("Online");
                mTextOnline.setTextColor(Color.parseColor("#FF08FF00"));
            }else{
                GetTimeApp getTimeApp = new GetTimeApp();
                long lastTime = Long.parseLong(online);
                String lastSeenTime = getTimeApp.getTimeAgo(lastTime, mView.getContext());
                mTextOnline.setText(lastSeenTime);
            }

        }
    }
}
