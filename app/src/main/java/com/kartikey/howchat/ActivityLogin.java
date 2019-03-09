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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class ActivityLogin extends AppCompatActivity {

    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mLoginBtn;

    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference mUser;

    private ConstraintLayout constraintLayout;

    private ProgressDialog mLoginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginProgress = new ProgressDialog(this);

        constraintLayout = findViewById(R.id.login_constraintlayout);

        mToolbar=findViewById(R.id.login_pagetoolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mUser= FirebaseDatabase.getInstance().getReference().child("Users");

        mEmail = findViewById(R.id.logEmail);
        mPassword =findViewById(R.id.logPassword);
        mLoginBtn = findViewById(R.id.logbtn);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                Boolean condition = true;

                if(email.isEmpty()){
                    condition=false;
                    mEmail.getEditText().setText("");
                }
                if(password.isEmpty()){
                    condition=false;
                    mPassword.getEditText().setText("");
                }

                if(condition){
                    mLoginProgress.setTitle("Logging In");
                    mLoginProgress.setMessage("Please wait While we LogIn");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();
                    loginUser(email, password);
                }else{
                    Toast.makeText(ActivityLogin.this, "Enter Valid Details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ActivityLogin.this, "Logged In", Toast.LENGTH_SHORT).show();
                            Intent mainIntent = new Intent(ActivityLogin.this, MainActivity.class);
                            mLoginProgress.dismiss();

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            String currentUserId = mAuth.getUid();

                            mUser.child(currentUserId).child("device_token").setValue(deviceToken);
                            mUser.child(currentUserId).child("email").setValue(mAuth.getCurrentUser().getEmail());


                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            finish();
                            startActivity(mainIntent);
                        } else {
                            mLoginProgress.hide();
                            Snackbar.make(constraintLayout, "Login Failed", Snackbar.LENGTH_LONG)
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
