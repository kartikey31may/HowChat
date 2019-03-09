package com.kartikey.howchat;

import android.app.ProgressDialog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ActivityProfile extends AppCompatActivity {

    private TextView mDisplay;
    private TextView mStatus;
    private ImageView mImage;
    private TextView mFriends;
    private Button mReq, mDec;

    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

    private DatabaseReference mRef;
    private DatabaseReference mFriendReq;
    private DatabaseReference mFriendsRef;
    private DatabaseReference mNotificationRef;
    private DatabaseReference mRootRef;

    private String ImageUserUrl;

    private int Current_state, count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mProgressDialog = new ProgressDialog(this);

        mDisplay = findViewById(R.id.profile_name);
        mStatus = findViewById(R.id.profile_status);
        mImage = findViewById(R.id.profile_image);
        mFriends = findViewById(R.id.profile_friend);
        mReq = findViewById(R.id.profile_req_btn);
        mDec = findViewById(R.id.profile_dec_btn);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        Current_state = 0;

        final String UserId = getIntent().getStringExtra("userId");

        if (mCurrentUser.getUid().equals(UserId)){
            mReq.setVisibility(View.GONE);
        }
        mDisplay.setText(UserId);

        mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(UserId);
        mFriendReq = FirebaseDatabase.getInstance().getReference().child("Friend_Req");
        mFriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationRef = FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();

         mRootRef.child("Friend_req").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {

                 Log.d("hello", UserId);
                 Log.d("hello", dataSnapshot.getKey());
                 Log.d("hello", dataSnapshot.hasChild(UserId)+"");

                 if(dataSnapshot.hasChild(UserId)){
                     String req_type = dataSnapshot.child(UserId).child("request_type").getValue().toString();


                      if(req_type.equals("received")){
                          Current_state=2;
                          mReq.setText("Accept Friend Request");
                          mDec.setVisibility(View.VISIBLE);
                      }

                     if(req_type.equals("sent")){
                         Log.d("bool",req_type);
                         Current_state=1;
                         mReq.setText("Cancel Friend Request");
                     }

                 }
                 else{
                     mFriendsRef.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot dataSnapshot) {
                             if(dataSnapshot.hasChild(UserId)){
                                 Current_state=3;
                                 mReq.setText("UnFriend This Person");
                             }
                         }

                         @Override
                         public void onCancelled(DatabaseError databaseError) {

                         }
                     });
                 }
             }

             @Override
             public void onCancelled(DatabaseError databaseError) {

             }
         });

         mRootRef.child("Friends").child(UserId).addChildEventListener(new ChildEventListener() {
             @Override
             public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                 count++;
                 mFriends.setText("Friends : "+count);
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

        mProgressDialog.setTitle("Loading User");
        mProgressDialog.setMessage("Please wait while we fetch Users details");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String imageUrl = dataSnapshot.child("imageURL").getValue().toString();
                ImageUserUrl = imageUrl;

                if(!imageUrl.equals("default")) {
                    Glide.with(getApplicationContext()).load(imageUrl).into(mImage);
                }

                mDisplay.setText(display_name);
                mStatus.setText(status);
                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //-----------------NOT FRIENDS STATE-------------

                if(Current_state==0){

                    DatabaseReference newNotificationsRef = mRootRef.child("notifications").child(UserId).push();
                    String newNotificationId = newNotificationsRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/"+mCurrentUser.getUid()+"/"+UserId+"/request_type", "sent");
                    requestMap.put("Friend_req/"+UserId+"/"+mCurrentUser.getUid()+"/request_type", "received");
                    requestMap.put("notifications/"+UserId+"/"+newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                Toast.makeText(ActivityProfile.this, "Error Sending Friend Request", Toast.LENGTH_SHORT).show();
                            }else{
                                mReq.setText("Cancel Request Sent");
                                Current_state=1;
                                Toast.makeText(ActivityProfile.this, "Friend Request Send", Toast.LENGTH_SHORT).show();

                            }

                        }
                    });
                }
                //-----------------SENT FRIENDS STATE-------------

                if(Current_state==1){

                    Map delReq = new HashMap();
                    delReq.put("Friend_req/"+mCurrentUser.getUid()+"/"+UserId, null);
                    delReq.put("Friend_req/"+UserId+"/"+mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(delReq, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError==null){
                                Current_state=0;
                                mReq.setText("Send friend Request");
                            }else
                            {
                                Toast.makeText(ActivityProfile.this, "Some Error Occurred", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                    mReq.setText("Send Friend Request");
                    Current_state=0;
                }

                //-----------------ACCEPT FRIENDS STATE-------------

                if(Current_state==2){

                    String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map FriendMap = new HashMap();
                    FriendMap.put("Friends/"+mCurrentUser.getUid()+"/"+UserId+"/date", currentDate);
                    FriendMap.put("Friends/"+UserId+"/"+mCurrentUser.getUid()+"/date", currentDate);

                    FriendMap.put("Friend_req/"+mCurrentUser.getUid()+"/"+UserId,null);
                    FriendMap.put("Friend_req/"+UserId+"/"+mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(FriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError==null){
                                Current_state=3;
                                mReq.setText("UnFriend This Person");
                                mDec.setVisibility(View.GONE);
                            }else{
                                Toast.makeText(ActivityProfile.this, "Error Accepting Friend Request", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });


                }

                //-----------------FRIENDS STATE/UN-FRIEND-------------

                if(Current_state==3){

                    Map unFriend = new HashMap();
                    unFriend.put("Friends/"+mCurrentUser.getUid()+"/"+UserId, null);
                    unFriend.put("Friends/"+UserId+"/"+mCurrentUser.getUid(), null);
                    unFriend.put("Chat/"+mCurrentUser.getUid()+"/"+UserId, null);
                    unFriend.put("Chat/"+UserId+"/"+mCurrentUser.getUid()+"/message", "unFriended");
                    unFriend.put("messages/"+mCurrentUser.getUid()+"/"+UserId, null);

                    mRootRef.updateChildren(unFriend, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError==null){
                                mReq.setText("Send Friend Request");

                                Current_state=0;
                                Toast.makeText(ActivityProfile.this, "Person Unfriended", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(ActivityProfile.this, "There was some Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }

            }
        });



        mDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map delReq = new HashMap();
                delReq.put("Friend_req/"+mCurrentUser.getUid()+"/"+UserId, null);
                delReq.put("Friend_req/"+UserId+"/"+mCurrentUser.getUid(), null);

                mRootRef.updateChildren(delReq, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError==null){
                            Current_state=0;
                            mDec.setVisibility(View.GONE);
                            mReq.setText("Send friend Request");
                            Toast.makeText(ActivityProfile.this, "Send Request Declined", Toast.LENGTH_SHORT).show();

                        }else
                        {
                            Toast.makeText(ActivityProfile.this, "Some Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(ActivityProfile.this);
                View dialogView = getLayoutInflater().inflate(R.layout.imageuser, null);
                ImageView imageView = dialogView.findViewById(R.id.Image_User);
                Glide.with(getApplicationContext()).load(ImageUserUrl).into(imageView);
                mBuilder.setView(dialogView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mCurrentUser != null)
            mRootRef.child("Users").child(mCurrentUser.getUid()).child("online").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mCurrentUser != null)
            mRootRef.child("Users").child(mCurrentUser.getUid()).child("online").setValue(ServerValue.TIMESTAMP);

    }
}
