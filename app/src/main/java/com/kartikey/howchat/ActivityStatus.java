package com.kartikey.howchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ActivityStatus extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button btnSave;
    private TextInputLayout mStatus;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        String status = getIntent().getStringExtra("status");

        mToolbar = findViewById(R.id.status_pagetoolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressDialog = new ProgressDialog(this);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference("Users").child(mCurrentUser.getUid());


        mStatus = findViewById(R.id.statusEdit);
        mStatus.getEditText().setText(status);
        btnSave = findViewById(R.id.statussave_btn);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog.setTitle("Saving Changes");
                mProgressDialog.setMessage("Wait While we Save Changes");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                String status = mStatus.getEditText().getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgressDialog.dismiss();
                        }else{
                            Toast.makeText(ActivityStatus.this, "There was some error in Saving Changes", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }
}
