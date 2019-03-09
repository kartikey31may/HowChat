package com.kartikey.howchat;


import android.app.ProgressDialog;
import android.content.Context;
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
public class FragmentRequest extends Fragment {

    private RecyclerView mRequestList;

    private DatabaseReference mRequestRef;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    private ProgressDialog mProgressDialog;

    private static Context mContext;


    public FragmentRequest() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView =  inflater.inflate(R.layout.fragment_fragment_request, container, false);


        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mContext = getActivity().getApplicationContext();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
        mRequestRef.keepSynced(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mRequestList = mMainView.findViewById(R.id.request_recycler_list);
        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        mProgressDialog = new ProgressDialog(mMainView.getContext());
        mProgressDialog.setTitle("Loading List of Requests");
        mProgressDialog.setMessage("Please wait while we fetch details");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Request, FragmentRequest.RequestViewHolder>firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>(
                Request.class,
                R.layout.users_single_layout,
                FragmentRequest.RequestViewHolder.class,
                mRequestRef
        ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Request model, int position) {
                final String chatuserid = getRef(position).getKey();
                final String type = model.request_type;
                mRootRef.child("Users").child(chatuserid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        String image = dataSnapshot.child("thumb_image").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();

                        viewHolder.setImage(image);
                        viewHolder.setName(name);
                        viewHolder.setStatus(status);
                        viewHolder.setOnline(type);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent profileIntent = new Intent(mContext, ActivityProfile.class);
                                profileIntent.putExtra("userId", chatuserid);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mRequestList.setAdapter(firebaseRecyclerAdapter);
        mProgressDialog.dismiss();

    }

    private static class RequestViewHolder extends RecyclerView.ViewHolder {

        View mView;
        ImageView mImageUser;

        public RequestViewHolder(View itemView) {
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
            mTextOnline.setText(online.toUpperCase());

        }
    }
}
