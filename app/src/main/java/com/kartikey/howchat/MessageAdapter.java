package com.kartikey.howchat;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessagesViewHolder>
{
    private DatabaseReference mRootRef;
    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private String currentUserName, chatUserName, currentUserImage, chatUserImage;

    public MessageAdapter(List<Messages> mMessageList){
        this.mMessageList = mMessageList;
        mAuth=FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.child("Users").child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUserImage = dataSnapshot.child("thumb_image").getValue().toString();
                currentUserName = dataSnapshot.child("name").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);

        return new MessagesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
        String current_user_id = mAuth.getCurrentUser().getUid();

            Messages c = mMessageList.get(position);
            String from_user = c.from;

                holder.messageText.setText(c.message);

                if(from_user.equals(current_user_id)){
                   holder.displayName.setText(currentUserName);
                   Glide.with(holder.v.getContext()).load(currentUserImage).into(holder.userImage);
                }else{

                   /* holder.messageText.setBackgroundResource(R.drawable.message_text_background);
                    holder.messageText.setPadding(12, 12, 12, 12);
                    holder.messageText.setTextColor(Color.WHITE);*/

                }
        //Glide.with(holder.v.getContext()).load(c).into(holder.userImage);
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder{
        public View v;
        public TextView messageText, displayName, time;
        public CircleImageView userImage;

         public MessagesViewHolder(View view){
            super(view);

            v = view;
            messageText = view.findViewById(R.id.chat_list_message_user);
            userImage = view.findViewById(R.id.chat_list_image_user);
            displayName = view.findViewById(R.id.chat_list_message_user_name);
            time = view.findViewById(R.id.chat_list_message_time);

        }
    }
}
