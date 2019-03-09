package com.kartikey.howchat;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityChat extends AppCompatActivity {

    private String mChatUser, mChatUserName, mChatUserImage, mCurrentUserId;
    private Toolbar mChatToolBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;


    private TextView mDisplayName, mUserLastSeen;
    private ImageView mUserImage;

    private ImageView chatAddButton, chatSentButton;
    private EditText chatMessage;

    private RecyclerView mMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private SwipeRefreshLayout mSwipe;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    //new
    private int mCurrentPostion = 0;
    private String mLastKey="";
    private String mPrevKey="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mAuth=FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();

        mChatUser = getIntent().getStringExtra("userId");
        mChatUserName = getIntent().getStringExtra("name");
        mChatUserImage = getIntent().getStringExtra("thumb_image");


        mSwipe = findViewById(R.id.chat_refresh);
        setSupportActionBar(mChatToolBar);

        chatAddButton = findViewById(R.id.chat_add);
        chatSentButton = findViewById(R.id.chat_send);
        chatMessage = findViewById(R.id.chat_message);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view  = inflater.inflate(R.layout.chat_action_bar, null);

        mDisplayName = action_bar_view.findViewById(R.id.chat_name_user);
        mUserImage = action_bar_view.findViewById(R.id.chat_image_user);
        mUserLastSeen = action_bar_view.findViewById(R.id.chat_last_seen_user);

        mDisplayName.setText(mChatUserName);
        Glide.with(getApplicationContext()).load(mChatUserImage).into(mUserImage);
        actionBar.setCustomView(action_bar_view);

        mMessagesList = findViewById(R.id.chat_list_message);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mAdapter = new MessageAdapter(messagesList);

        mMessagesList.setAdapter(mAdapter);
        loadMessages();


        mRootRef.child("Users").child(mChatUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                if(online.equals("true")){
                    mUserLastSeen.setText("Online");

                }else{
                    GetTimeApp getTimeApp = new GetTimeApp();
                    long lastTime = Long.parseLong(online);
                    String lastSeenTime = getTimeApp.getTimeAgo(lastTime, getApplicationContext());
                    mUserLastSeen.setText(lastSeenTime);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap = new HashMap<>();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+mCurrentUserId+"/"+mChatUser, chatAddMap);
                    chatUserMap.put("Chat/"+mChatUser+"/"+mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //---------------------send button-----------------------------
        chatSentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                mCurrentPostion = 0;
                loadMoreMessages();
            }
        });


    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();
                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(mCurrentPostion++, message);
                }else{
                    mPrevKey = mLastKey;
                }

                if(mCurrentPostion == 1){
                    mLastKey = messageKey;
                }


                Log.d("TotalKeys", "last key "+ mLastKey +" Prev Key " +mPrevKey+ " Message key" +messageKey);


                mAdapter.notifyDataSetChanged();
                mSwipe.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10, 0);
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
    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                messagesList.add(mCurrentPostion++,message);

                if(mCurrentPostion == 1){
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }


                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messagesList.size()-1);
                mSwipe.setRefreshing(false);

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
    }

    private void sendMessage() {
        String message = chatMessage.getText().toString();
        if(!message.isEmpty()){

            String current_user_ref = "messages/"+mCurrentUserId+"/"+mChatUser;
            String chat_user_ref = "messages/"+mChatUser+"/"+mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(chat_user_ref+"/"+push_id, messageMap);
            messageUserMap.put(current_user_ref+"/"+push_id, messageMap);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    chatMessage.setText("");
                }
            });

        }
    }

    @Override
    public void onStart(){
        super.onStart();
        //Check If user is Signed in (Non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            Intent startIntent = new Intent(getApplicationContext(), ActivityStart.class);
            startActivity(startIntent);
            finish();

        }else{
            mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("online").setValue(true);

        }
    }
}
