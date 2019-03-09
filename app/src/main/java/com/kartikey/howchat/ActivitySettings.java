package com.kartikey.howchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class ActivitySettings extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private DatabaseReference mUserDatabaseRef;
    private FirebaseUser mCurrentUser;

    private TextView mName, mStatus;
    private CircleImageView mImage;

    private Button mStatusBtn, mImageBtn;

    private static final int galleryInt =101;

    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Account Settings");
        progressDialog.setMessage("Please wait While we Fetch your Information");
        progressDialog.setCanceledOnTouchOutside(false);

        mName = findViewById(R.id.setting_name);
        mStatus=findViewById(R.id.setting_status);
        mImage=findViewById(R.id.setting_image);

        mStatusBtn = findViewById(R.id.setting_chng_status);
        mImageBtn = findViewById(R.id.setting_chng_image);

        mImageStorage = FirebaseStorage.getInstance().getReference();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();


        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        mUserDatabaseRef.keepSynced(true);

        progressDialog.show();

        mUserDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                String imageURL = dataSnapshot.child("imageURL").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);

                if(!imageURL.equals("default")){
                     Glide.with(getApplicationContext())
                             .load(imageURL)
                             .into(mImage);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();

            }
        });

        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent status_intent = new Intent(ActivitySettings.this, ActivityStatus.class);
                status_intent.putExtra("status", mStatus.getText().toString());
                startActivity(status_intent);
            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent galleryIntent = new Intent();
                galleryIntent.setType("image/");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), galleryInt);
                */

                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(ActivitySettings.this);


            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                final Uri resultUri = result.getUri();

                final File thumb_filePath = new File(resultUri.getPath());

                Bitmap bitmap = null;
                try {
                    bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();


                final StorageReference filepath = mImageStorage.child("profile_images").child(mCurrentUser.getUid()).child(mCurrentUser.getUid()+"_image.jpg");
                final StorageReference filepath_thumb = mImageStorage.child("profile_images").child(mCurrentUser.getUid()).child(mCurrentUser.getUid()+"_image_thumb.jpg");

                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please wait While we make the changes");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final String ImageUrl = taskSnapshot.toString();

                            /*filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users").child(mCurrentUser.getUid());
                                    mRef.child("imageURL").setValue(task.getResult().toString());
                                    Glide.with(ActivitySettings.this)
                                            .load(task.getResult().toString())
                                            .into(mImage);
                                }
                            });*/
                                UploadTask uploadTask = filepath_thumb.putBytes(thumb_byte);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                                        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                final String ThumbUrl = taskSnapshot.toString();
                                                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users").child(mCurrentUser.getUid());
                                                mRef.child("image").setValue(mCurrentUser.getUid()+"_image.jpg");
                                                mRef.child("imageURL").setValue(ImageUrl);
                                                mRef.child("thumb_image").setValue(ThumbUrl);
                                                Glide.with(ActivitySettings.this)
                                                        .load(resultUri)
                                                        .into(mImage);

                                                progressDialog.dismiss();
                                            }
                                        });

                                        if(task.isSuccessful()){

                                        }else{
                                            Toast.makeText(ActivitySettings.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                                            progressDialog.hide();
                                        }
                                    }
                                });
                            }
                        });
                        if(task.isSuccessful()){

                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
