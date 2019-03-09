package com.kartikey.howchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class ActivityRegister extends AppCompatActivity {

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;

    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;

    private ConstraintLayout constraintLayout;

    private ProgressDialog mRegProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        constraintLayout = findViewById(R.id.constraintlayout);
        mToolbar=findViewById(R.id.register_pagetoolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        mDisplayName = findViewById(R.id.regName);
        mEmail = findViewById(R.id.regEmail);
        mPassword =findViewById(R.id.regPassword);
        mCreateBtn = findViewById(R.id.regbtn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                Boolean condition = true;

                if(display_name.isEmpty()){
                    condition=false;
                    mDisplayName.getEditText().setText("");
                }
                if(email.isEmpty()){
                    condition=false;
                    mEmail.getEditText().setText("");
                }
                if(password.isEmpty()){
                    condition=false;
                    mPassword.getEditText().setText("");
                }
                if(!email.contains("@")||!email.contains(".")){
                    condition=false;
                    mEmail.getEditText().setText("");
                }

                if(condition){
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait While we create your account");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                   registerUser(email, password, display_name);


                }else{
                    Toast.makeText(ActivityRegister.this, "Enter Valid Details", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void registerUser(final String email, String password, String name){
        final String mName = name;
         mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(ActivityRegister.this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                                        final Intent mainIntent = new Intent(ActivityRegister.this, MainActivity.class);


                                        FirebaseUser user = mAuth.getCurrentUser();
                                        String userid = user.getUid();
                                        DatabaseReference mRef = mDatabase.getReference("Users").child(userid);

                                        HashMap<String, String> userMap = new HashMap<>();
                                        userMap.put("name", mName);
                                        userMap.put("status", "Hi there I'm using HowChat");
                                        userMap.put("image", "default");
                                        userMap.put("thumb_image", "default");
                                        userMap.put("imageURL", "default");
                                        userMap.put("online", "true");
                                        userMap.put("device_token", FirebaseInstanceId.getInstance().getToken());
                                        userMap.put("email", email);

                                        mRef.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    mRegProgress.dismiss();

                                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    finish();
                                                    startActivity(mainIntent);
                                                }
                                            }
                                        });


                                    }else{
                                        mRegProgress.hide();
                                       Snackbar.make(constraintLayout, "Registration Failed", Snackbar.LENGTH_LONG)
                                               .setAction("Retry..", new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View v) {
                                                       finish();
                                                       startActivity(getIntent());
                                                   }
                                               })
                                               .show();
                                    }

                                }
                            });

    }
}
