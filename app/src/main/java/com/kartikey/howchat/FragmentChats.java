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
import android.util.Log;
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

import java.sql.Time;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentChats extends Fragment {

    private RecyclerView mChatList;

    private DatabaseReference mChatRef;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    private ProgressDialog mProgressDialog;

    private static Context mContext;

    private boolean condition=true;

    public FragmentChats() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_fragment_chats, container, false);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mContext = getActivity().getApplicationContext();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mChatRef = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
        mChatRef.keepSynced(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mChatList = mMainView.findViewById(R.id.chat_recycler_list);
        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(new LinearLayoutManager(getContext()));

        mProgressDialog = new ProgressDialog(mMainView.getContext());
        mProgressDialog.setTitle("Loading List of Chats");
        mProgressDialog.setMessage("Please wait while we fetch Chats details");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Chat, FragmentChats.FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chat, FriendsViewHolder>(
                Chat.class,
                R.layout.users_single_layout,
                FragmentChats.FriendsViewHolder.class,
                mChatRef
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, Chat model, int position) {
                String userId = getRef(position).getKey();
                final String message = model.message;

                if (message.equals("unFriended")){
                    condition=false;
                }else
                {
                    condition=true;
                }
                Log.d("condition2", ""+condition+" "+ message);

                long lastTime = model.time;
                Date date = new Date(lastTime);
                Time time = new Time(date.getTime());
                 final String mTime = time.toString();
                 final String nTime = mTime.substring(0, mTime.length()-3);
                mRootRef.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue().toString();
                        final String image = dataSnapshot.child("thumb_image").getValue().toString();

                        viewHolder.setName(name);
                        viewHolder.setStatus(message);
                        viewHolder.setOnline(nTime);
                        viewHolder.setImage(image);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(mMainView.getContext(), ActivityChat2.class);
                                chatIntent.putExtra("userId", dataSnapshot.getKey());
                                chatIntent.putExtra("name", name);
                                chatIntent.putExtra("thumb_image", image);
                                chatIntent.putExtra("condition", ""+condition);
                                startActivity(chatIntent);
                            }
                        });
                        viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                CharSequence options[] = new CharSequence[]{"Open Profile", "Delete Chat"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which==0){

                                            Intent profileIntent = new Intent(mMainView.getContext(), ActivityProfile.class);
                                            profileIntent.putExtra("userId", dataSnapshot.getKey());
                                            startActivity(profileIntent);

                                        }else if(which == 1){
                                            mRootRef.child("Chat").child(mAuth.getCurrentUser().getUid()).child(dataSnapshot.getKey()).setValue(null);
                                            mRootRef.child("messages").child(mAuth.getCurrentUser().getUid()).child(dataSnapshot.getKey()).setValue(null);
                                            Toast.makeText(mContext, "Chat Deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                });
                                builder.show();
                                return true;
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };


        mChatList.setAdapter(firebaseRecyclerAdapter);
        mProgressDialog.dismiss();

    }

    private static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView mImageUser;

        public FriendsViewHolder(View itemView) {
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
            mStatus.setMaxLines(2);
            mStatus.setText(date);
        }
        public void setImage(String url){
            ImageView mImage = mView.findViewById(R.id.users_image);
            Glide.with(mContext).load(url).into(mImage);
        }
        public void setOnline(String online){
            TextView mTextOnline = mView.findViewById(R.id.users_online_text);
            mTextOnline.setTextColor(Color.BLACK);
            mTextOnline.setText(online);

        }
    }

}


