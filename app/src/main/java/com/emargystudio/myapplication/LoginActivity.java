package com.emargystudio.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.emargystudio.myapplication.common.SharedPreferenceManger;
import com.emargystudio.myapplication.common.common;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText username = findViewById(R.id.username);
        final EditText password = findViewById(R.id.password);
        Button login = findViewById(R.id.loginBtn);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(username.getText().toString())&&!TextUtils.isEmpty(password.getText().toString())){
                    String userString = username.getText().toString();
                    String passwordString = password.getText().toString();

                    if (userString.equals(common.userName)){
                        if (passwordString.equals(common.password)) {

                            SharedPreferenceManger.getInstance(LoginActivity.this).storeUserStatus(true);
                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                        }else {
                            Toast.makeText(LoginActivity.this, "كلمة السر خاطئة", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(LoginActivity.this, "اسم المستخدم خاطىء", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(LoginActivity.this, "الرجاء ادخال كلمة السر واسم المستخدم", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
