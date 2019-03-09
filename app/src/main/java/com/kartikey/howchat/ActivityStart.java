package com.kartikey.howchat;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class ActivityStart extends AppCompatActivity {

    private Button btn_reg;
    private Button btn_lgn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        btn_reg = findViewById(R.id.startAddAccount);
        btn_lgn = findViewById(R.id.startLogin);

        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentReg = new Intent(ActivityStart.this, ActivityRegister.class);
                startActivity(intentReg);

            }
        });

        btn_lgn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentReg = new Intent(ActivityStart.this, ActivityLogin.class);
                startActivity(intentReg);

            }
        });
    }

}
