package com.emargystudio.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.emargystudio.myapplication.common.SharedPreferenceManger;

public class SplashScreen extends AppCompatActivity {

    SharedPreferenceManger sharedPreferenceManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        sharedPreferenceManger = SharedPreferenceManger.getInstance(SplashScreen.this);
        int SPLASH_DISPLAY_LENGTH = 1200;
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                if (sharedPreferenceManger.getFirstTime()!=0){
                    Intent mainIntent = new Intent(SplashScreen.this,MainActivity.class);
                    SplashScreen.this.startActivity(mainIntent);
                    SplashScreen.this.finish();
                }else {
                    Intent mainIntent = new Intent(SplashScreen.this,InitActivity.class);
                    SplashScreen.this.startActivity(mainIntent);
                    SplashScreen.this.finish();
                }

            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
