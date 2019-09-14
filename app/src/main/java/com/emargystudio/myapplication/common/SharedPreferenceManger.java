package com.emargystudio.myapplication.common;

import android.content.Context;
import android.content.SharedPreferences;



public class SharedPreferenceManger {

    private static final String FIRST_TIME = "FIRSTTIME";
    private static final String IS_FIRST_TIME = "is_first_time";
    private static  final String IS_FIRST_FOOD = "is_first_food";

    private static final  String LOGIN_STATUS = "LOGINSTATUS";
    private static final String  IS_LOGED_IN = "is_logged_in";



    private static SharedPreferenceManger mSharedPreferenceManger;
    private static Context mContext;

    public SharedPreferenceManger(Context context) {
        this.mContext = context;
    }

    public static synchronized SharedPreferenceManger getInstance(Context context){

        if(mSharedPreferenceManger == null){
            mSharedPreferenceManger = new SharedPreferenceManger(context);
        }
        return mSharedPreferenceManger;
    }


    public void storeFirstUse(int is_first_time){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(FIRST_TIME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(IS_FIRST_TIME,is_first_time);
        editor.apply();
    }

    public void storeUserStatus(boolean is_logged_in){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(LOGIN_STATUS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_LOGED_IN,is_logged_in);
        editor.apply();
    }

    public boolean getLogginInStatus(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(LOGIN_STATUS, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(IS_LOGED_IN,false);
    }

    public int getFirstTime(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(FIRST_TIME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(IS_FIRST_TIME,0);
    }

    public void storeFirstFood(int is_first_time){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(FIRST_TIME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(IS_FIRST_FOOD,is_first_time);
        editor.apply();
    }

    public int getFirstFood(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(FIRST_TIME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(IS_FIRST_FOOD,0);
    }


}
