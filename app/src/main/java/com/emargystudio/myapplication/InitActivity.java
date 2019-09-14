package com.emargystudio.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Observable;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.emargystudio.myapplication.common.SharedPreferenceManger;
import com.emargystudio.myapplication.common.common;
import com.emargystudio.myapplication.dataBase.AppDatabase;
import com.emargystudio.myapplication.dataBase.AppExecutors;
import com.emargystudio.myapplication.model.Category;
import com.emargystudio.myapplication.model.Food;
import com.google.android.material.tabs.TabLayout;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class InitActivity extends AppCompatActivity {

    private static final String TAG = "InitActivity";


    private SharedPreferenceManger sharedPreferenceManger;
    private AppDatabase appDatabase;
    int foodNumber = 0;
    TextView textView;

    //category
    private ArrayList<Category> parseCategories = new ArrayList<>();
    private List<Category> roomCategories = new ArrayList<>();
    private Handler mCategoryHandler;


    //food
    private ArrayList<Food> parseFoods = new ArrayList<>();
    private List<Food> roomFoods = new ArrayList<>();
    private Handler mFoodHandler;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);


        //refresh vars
        sharedPreferenceManger = SharedPreferenceManger.getInstance(this);
        appDatabase = AppDatabase.getInstance(this);
        mCategoryHandler = new Handler();
        mFoodHandler = new Handler();
        textView = findViewById(R.id.textView3);




        if (isNetworkAvailable()){
            initApp();
        }

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }






    private void initApp(){
        queryCategoryFromParse();
        queryFoodFromParse();

    }



    private void queryCategoryFromParse(){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("category");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) { if (e == null){
                // Adding objects into the Array
                for(int i= 0 ; i < objects.size(); i++){

                    ParseObject categoryObject = objects.get(i);
                    parseCategories.add(new Category(
                            categoryObject.getInt("id"),
                            categoryObject.getString("name"),
                            categoryObject.getString("en_name"),
                            categoryObject.getInt("versionNumber")
                    ));
                }
                queryCategoryFromRoom();
            } else {
                Toast.makeText(InitActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
            }
        });
    }
    private void queryCategoryFromRoom(){
        if (sharedPreferenceManger.getFirstTime() == 0){
            camperCategoryInTowDataBases();
        }else {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    roomCategories = appDatabase.categoryDao().loadAllFoodsAdapter();

                    if (!roomCategories.isEmpty()){
                        mCategoryHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                camperCategoryInTowDataBases();
                            }
                        });
                    }
                }
            });
        }
    }
    private void camperCategoryInTowDataBases() {
        for (Category category : parseCategories){
            boolean categoryExist = false;
            for (Category category1 : roomCategories){
                if (category.getCategory_id()==category1.getCategory_id()){
                    if (category.getVersionNumber()!=category1.getVersionNumber()){
                        category1.setName(category.getName());
                        category1.setEn_name(category.getEn_name());
                        category1.setVersionNumber(category.getVersionNumber());
                        updateCategoriesInRoom(category1);
                    }
                    categoryExist = true;
                    foodNumber+=1;

                    break;
                }
            }
            if (!categoryExist){
                saveCategoryToRoom(category);
                sharedPreferenceManger.storeFirstUse(1);
            }
        }
        for (final Category roomCategory: roomCategories){
            boolean existInRoomOnly = true;
            for (Category parsCategory:parseCategories){
                if (roomCategory.getCategory_id()==parsCategory.getCategory_id()){
                    existInRoomOnly = false;
                }
            }

            if (existInRoomOnly){
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        appDatabase.categoryDao().deleteFood(roomCategory);
                    }
                });
            }
        }
    }
    private void updateCategoriesInRoom(final Category category) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                appDatabase.categoryDao().updateFood(category);
            }
        });
    }
    private void saveCategoryToRoom(final Category category){
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                appDatabase.categoryDao().insertFood(category);
            }
        });
    }


    private void queryFoodFromParse(){

        if (!parseFoods.isEmpty()){
            parseFoods.clear();
        }
        ParseQuery<ParseObject> query = ParseQuery.getQuery("food");
        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){

                    // Adding objects into the Array
                    for(int i= 0 ; i < objects.size(); i++){

                        ParseObject foodObject = objects.get(i);
                        if (foodObject.getInt("id")!=0) {
                            parseFoods.add(new Food(
                                    foodObject.getInt("id"),
                                    foodObject.getInt("category_id"),
                                    foodObject.getString("name"),
                                    foodObject.getString("description"),
                                    foodObject.getString("en_name"),
                                    foodObject.getString("en_description"),
                                    foodObject.getInt("price"),
                                    foodObject.getInt("versionNumber")
                            ));

                        }



                    }

                    queryFoodFromRoom();
                } else {

                    Toast.makeText(InitActivity.this, "حدث خطأ ما", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void queryFoodFromRoom(){
        if (sharedPreferenceManger.getFirstFood() == 0){
            camperFoodInTowDataBases();
        }else {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    roomFoods = appDatabase.foodDao().loadAllFoodsAdapter();

                    if (!roomFoods.isEmpty()){
                        mFoodHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                camperFoodInTowDataBases();
                            }
                        });



                    }
                }
            });
        }
    }
    private void camperFoodInTowDataBases() {

        for (Food food : parseFoods){

            boolean foodExist = false;
            for (Food food1 : roomFoods){
                if (food.getFood_id()==food1.getFood_id()){
                    if (food.getVersionNumber()!=food1.getVersionNumber()){
                        food1.setName(food.getName());
                        food1.setEn_name(food.getEn_name());
                        food1.setEn_description(food.getEn_description());
                        food1.setVersionNumber(food.getVersionNumber());
                        food1.setDescription(food.getDescription());
                        food1.setPrice(food.getPrice());

                        findFoodFromPars(food1,0);
                    }
                    foodExist = true;
                    break;
                }

            }
            if (!foodExist){
                findFoodFromPars(food,1);
                sharedPreferenceManger.storeFirstFood(1);
            }



        }


        for (final Food roomFood: roomFoods){
            boolean existInRoomOnly = true;
            for (Food parsFood:parseFoods){
                if (roomFood.getFood_id()==parsFood.getFood_id()){
                    existInRoomOnly = false;
                }
            }

            if (existInRoomOnly){
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        appDatabase.foodDao().deleteFood(roomFood);
                    }
                });
            }
        }
    }
    private void findFoodFromPars(final Food food , final int status){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("food");

        query.whereEqualTo("id", food.getFood_id());

        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {

                if (e == null){



                    if (object!=null){
                        final ParseFile file = (ParseFile) object.get("image");
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                     

                        int width = displayMetrics.widthPixels;
                        try {
                            Bitmap bitmap = common.decodeFile(file.getFile(),width,200);
                            Uri uri = saveImageToInternalStorage(bitmap,food.getName());

                            food.setImage_uri(uri.toString());


                                    if (status == 0){

                                        updateFoodInRoom(food);
                                    }else {
                                        saveFoodToRoom(food);

                                    }



                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }

                    }
                }else {
                    e.printStackTrace();
                }
            }

        });

    }
    private void updateFoodInRoom(final Food food) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                appDatabase.foodDao().updateFood(food);
            }
        });
    }
    private void saveFoodToRoom(final Food food){

        foodNumber+=1;
        Log.d(TAG, "saveFoodToRoom: "+foodNumber);

        textView.setText(String.valueOf(foodNumber));
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                appDatabase.foodDao().insertFood(food);
            }
        });


        if (foodNumber==parseFoods.size()){
            Intent mainIntent = new Intent(InitActivity.this,MainActivity.class);
            startActivity(mainIntent);
            finish();
        }


    }


    private Uri saveImageToInternalStorage(Bitmap bitmap,String name){
        // Initialize ContextWrapper

        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());

        // Initializing a new file
        // The bellow line return a directory in internal storage
        File file = wrapper.getDir("Images",MODE_PRIVATE);

        // Create a file to save the image
        file = new File(file, name+".jpg");

        try{
            // Initialize a new OutputStream
            OutputStream stream ;

            // If the output file exists, it can be replaced or appended to it
            stream = new FileOutputStream(file);

            // Compress the bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG,70,stream);

            // Flushes the stream
            stream.flush();

            // Closes the stream
            stream.close();

        }catch (IOException e) // Catch the exception
        {
            e.printStackTrace();
        }

        // Parse the gallery image url to uri

        // Return the saved image Uri
        return Uri.parse(file.getAbsolutePath());
    }
}
