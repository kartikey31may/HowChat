package com.kartikey.howchat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class ActivityChat2 extends AppCompatActivity {

    private Toolbar mChatToolBar;
    private String mChatUser, mChatUserName, mChatUserImage, mCurrentUserId, mCurrentUserName, mCurrentUserImage, temp, cond;
    private TextView mDisplayName, mUserLastSeen;
    private ImageView mUserImage;


    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;

    private MessageInput inputView;
    private MessagesList messagesList;
    private MessagesListAdapter<Message> adapter;

    private Boolean condition=true;

    private StorageReference mImageStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat2);

        inputView = findViewById(R.id.input);
        messagesList = findViewById(R.id.messagesList);

        mAuth=FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mChatUser = getIntent().getStringExtra("userId");
        mChatUserName = getIntent().getStringExtra("name");
        mChatUserImage = getIntent().getStringExtra("thumb_image");
        temp = getIntent().getStringExtra("condition");
        if(temp.equals("false")){
            condition=false;
        }
        Log.d("Condition1", condition+" "+temp);

        mChatToolBar = findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view  = inflater.inflate(R.layout.chat_action_bar, null);

        mDisplayName = action_bar_view.findViewById(R.id.chat_name_user);
        mUserImage = action_bar_view.findViewById(R.id.chat_image_user);
        mUserLastSeen = action_bar_view.findViewById(R.id.chat_last_seen_user);

        mDisplayName.setText(mChatUserName);
        Glide.with(getApplicationContext()).load(mChatUserImage).into(mUserImage);
        actionBar.setCustomView(action_bar_view);

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
                    chatAddMap.put("time", ServerValue.TIMESTAMP);
                    chatAddMap.put("message", "");

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

        mRootRef.child("Users").child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCurrentUserName = dataSnapshot.child("name").getValue().toString();
                mCurrentUserImage = dataSnapshot.child("thumb_image").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                imageView.setBackgroundColor(Color.WHITE);
            Glide.with(ActivityChat2.this).load(url).into(imageView);
            }
        };

        adapter = new MessagesListAdapter<>(mCurrentUserId, imageLoader);
        loadMessages();
        messagesList.setAdapter(adapter);

        inputView.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {

                final String message = inputView.getInputEditText().getText().toString();

                if(!message.isEmpty()&&condition){

                    String current_user_ref = "messages/"+mCurrentUserId+"/"+mChatUser;
                    String chat_user_ref = "messages/"+mChatUser+"/"+mCurrentUserId;
                    String message_user_ref = "Chat/"+mCurrentUserId+"/"+mChatUser;
                    String message_chat_ref = "Chat/"+mChatUser+"/"+mCurrentUserId;

                    DatabaseReference user_message_push = mRootRef.child("messages")
                            .child(mCurrentUserId).child(mChatUser).push();

                    final String push_id = user_message_push.getKey();

                    Map messageMap = new HashMap();
                    messageMap.put("message", message);
                    messageMap.put("seen", false);
                    messageMap.put("type", "text");
                    messageMap.put("time", ServerValue.TIMESTAMP);
                    messageMap.put("from", mCurrentUserId);

                    Map messageUserMap = new HashMap();
                    messageUserMap.put(chat_user_ref+"/"+push_id, messageMap);
                    messageUserMap.put(current_user_ref+"/"+push_id, messageMap);
                    messageUserMap.put(message_chat_ref, messageMap);
                    messageUserMap.put(message_user_ref, messageMap);

                    mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(final DatabaseError databaseError, DatabaseReference databaseReference) {


                        }
                    });

                }else if (condition==false){
                    Toast.makeText(ActivityChat2.this, "Person unFriended you\nyou can't send message to the person", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        inputView.setAttachmentsListener(new MessageInput.AttachmentsListener() {
            @Override
            public void onAddAttachments() {
                Toast.makeText(ActivityChat2.this, "Attachment", Toast.LENGTH_SHORT).show();
            }
        });

    }



    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        messageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String messageText, time, type, from;
                Date date;
                messageText = dataSnapshot.child("message").getValue().toString();
                time = dataSnapshot.child("time").getValue().toString();
                from = dataSnapshot.child("from").getValue().toString();

                date = new Date(Long.parseLong(time));

                Arthur arthur;
                if(mCurrentUserId.equals(from)){
                    arthur = new Arthur(mCurrentUserId, mCurrentUserName, mCurrentUserImage);

                }else{
                    arthur = new Arthur(mChatUser, mChatUserName, mChatUserImage);
                }
                Message m = new Message(dataSnapshot.getKey(), messageText, arthur, date);
                adapter.addToStart(m, true);
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
