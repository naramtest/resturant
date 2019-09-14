package com.emargystudio.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.emargystudio.myapplication.common.SharedPreferenceManger;
import com.emargystudio.myapplication.common.common;
import com.emargystudio.myapplication.dataBase.AppDatabase;
import com.emargystudio.myapplication.dataBase.AppExecutors;
import com.emargystudio.myapplication.dataBase.CategoryViewModel;
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
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private ArrayList<Category> categoriesArray = new ArrayList<>();
    private static final String TAG = "MainActivity";

    private SharedPreferenceManger sharedPreferenceManger;
    private AppDatabase appDatabase;
    int foodNumber = 1;

    //category
    private ArrayList<Category> parseCategories = new ArrayList<>();
    private List<Category> roomCategories = new ArrayList<>();
    private Handler mCategoryHandler;


    //food
    private ArrayList<Food> parseFoods = new ArrayList<>();
    private List<Food> roomFoods = new ArrayList<>();
    private Handler mFoodHandler;


    //log in status
    private boolean is_logged_in;


    FragmentTransaction ft;
    TabLayout tabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //ToolBar init
        final Toolbar toolbar =  findViewById(R.id.htab_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            if (common.lang.equals("ar")){
                getSupportActionBar().setTitle("ايمارجي");
            }else {
                getSupportActionBar().setTitle("Emargy");

            }
        }
        changeToolbarFont(toolbar,MainActivity.this);


        //change app language Depending on user choice
        rtlSupport(common.lang);





        //refresh vars
        sharedPreferenceManger = SharedPreferenceManger.getInstance(this);
        appDatabase = AppDatabase.getInstance(this);
        mCategoryHandler = new Handler();
        mFoodHandler = new Handler();

        //check for user status
        is_logged_in = sharedPreferenceManger.getLogginInStatus();
        tabLayout = findViewById(R.id.htab_tabs);

        //call refresh on app start
        if (isNetworkAvailable()){
            refresh();
        }


            // show mainFragment on app start
            ft = getSupportFragmentManager().beginTransaction();
            Fragment mainFragment = new MainFragment();
            ft.replace(R.id.your_placeholder, mainFragment, "main");
            ft.commit();


            //tabLayout init

            loadAllCategories();
            initTab();



    }

    //tabLayout methods
    private void loadAllCategories() {
        CategoryViewModel categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);
        categoryViewModel.getTasks().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                if (!categoriesArray.isEmpty()) {
                    categoriesArray.clear();
                }
                categoriesArray.addAll(categories);
                tabLayout.removeAllTabs();
                for (Category category : categoriesArray) {
                    if (common.lang.equals("ar")) {
                        tabLayout.addTab(tabLayout.newTab().setText(category.getName()));
                    }else {
                        tabLayout.addTab(tabLayout.newTab().setText(category.getEn_name()));

                    }
                }
                changeTabsFont();
            }
        });
    }
    private void initTab(){
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int categoryID = categoriesArray.get(tab.getPosition()).getCategory_id();
                common.tabNumber = categoriesArray.get(tab.getPosition()).getCategory_id();

                switch (checkForCurrentFragment()) {
                    case "category":
                        CategoryFragment category = (CategoryFragment) getSupportFragmentManager().findFragmentByTag("category");
                        if (category!=null)
                        category.changeEditText(categoryID);
                        break;

                    case "food":
                        FoodFragment food = (FoodFragment) getSupportFragmentManager().findFragmentByTag("food");
                        if(food!=null)
                        food.getCategoryFromTabs(tab.getText().toString(),categoryID);
                        break;

                    case "main":
                        MainFragment main = (MainFragment) getSupportFragmentManager().findFragmentByTag("main");
                        if (main!=null)
                        main.setHeaderText(tab.getText().toString());
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                common.tabNumber = categoriesArray.get(tab.getPosition()).getCategory_id();                int categoryID = categoriesArray.get(tab.getPosition()).getCategory_id();
                switch (checkForCurrentFragment()) {
                    case "category":
                        CategoryFragment category = (CategoryFragment) getSupportFragmentManager().findFragmentByTag("category");
                        if (category!=null)
                            category.changeEditText(categoryID);
                        break;
                    case "food":
                        FoodFragment food = (FoodFragment) getSupportFragmentManager().findFragmentByTag("food");
                        if (food!=null)
                            food.getCategoryFromTabs(tab.getText().toString(),categoryID);
                        break;
                    case "main":
                        MainFragment main = (MainFragment) getSupportFragmentManager().findFragmentByTag("main");
                        if (main!=null)
                            main.setHeaderText(tab.getText().toString());
                        break;
                }
            }
        });
    }

    //init menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!is_logged_in){
            getMenuInflater().inflate(R.menu.login_menu, menu);
            if (common.lang.equals("ar")) {
               // menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_lock));
                menu.getItem(2).setTitle("تسجيل دخول");
                menu.getItem(0).setTitle("تحديث");
            }

        }else {
            getMenuInflater().inflate(R.menu.main_menu, menu);

        }




        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.log_in:
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

                break;
            case R.id.refresh:
                refresh();
                break;
            case R.id.user:
                sharedPreferenceManger.storeUserStatus(false);
                finish();
                startActivity(getIntent());

                break;
            case R.id.food:
                if (!checkForCurrentFragment().equals("food")) {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    if (getSupportActionBar()!=null) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        getSupportActionBar().setDisplayShowHomeEnabled(true);
                    }
                    Fragment foodFragment = new FoodFragment();
                    fragmentTransaction.replace(R.id.your_placeholder, foodFragment, "food");
                    fragmentTransaction.addToBackStack("food");
                    fragmentTransaction.commit();

                }
                break;
            case R.id.category:
                if (!checkForCurrentFragment().equals("category")) {
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    if (getSupportActionBar()!=null) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        getSupportActionBar().setDisplayShowHomeEnabled(true);
                    }
                    Fragment dataFragment = new CategoryFragment();
                    fragmentTransaction.replace(R.id.your_placeholder, dataFragment, "category");
                    fragmentTransaction.addToBackStack("category");
                    fragmentTransaction.commit();

                }
                break;
            case android.R.id.home:
                if (!checkForCurrentFragment().equals("main")){
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    Fragment mainFragment = new MainFragment();
                    fragmentTransaction.replace(R.id.your_placeholder, mainFragment, "main");
                    fragmentTransaction.commit();
                    if (getSupportActionBar()!=null) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        getSupportActionBar().setDisplayShowHomeEnabled(false);
                    }
                }
                break;

            case R.id.change_lang:
                if (common.lang.equals("ar")){
                    common.lang = "en";
                }else {
                    common.lang = "ar";

                }
                finish();
                startActivity(getIntent());
                break;


        }
        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }



    //helper methods
    private void rtlSupport(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getApplicationContext().getResources().updateConfiguration(config, getApplicationContext().getResources().getDisplayMetrics());
        if(lang.equals("ar")){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        }else {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    //all refresh's methods
    private void refresh(){
        queryCategoryFromParse();
        queryFoodFromParse();

    }
    // category
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
                      Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
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


    // food
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

                    Toast.makeText(MainActivity.this, "حدث خطأ ما", Toast.LENGTH_SHORT).show();
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
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                appDatabase.foodDao().insertFood(food);
            }
        });
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


    //get them of the current active fragment
    private String checkForCurrentFragment(){
        String fragmentName = "";


        Fragment category = getSupportFragmentManager().findFragmentByTag("category");
        Fragment main = getSupportFragmentManager().findFragmentByTag("main");
        Fragment food = getSupportFragmentManager().findFragmentByTag("food");
        if (category != null && category.isVisible()){
            fragmentName = "category";
        }else if (main != null && main.isVisible()){
            fragmentName = "main";
        }else if (food != null && food.isVisible()){
            fragmentName = "food";
        }


        return fragmentName;
    }

    //make back always go back to mainFragment when pressed
    @Override
    public void onBackPressed() {
        if (checkForCurrentFragment().equals("main")){
            super.onBackPressed();
        }else {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            Fragment mainFragment = new MainFragment();
            fragmentTransaction.replace(R.id.your_placeholder, mainFragment, "main");
            fragmentTransaction.commit();
        }
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
    }


    //change toolbar and tabLayout fonts
    private void changeTabsFont() {
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = tabChildsCount; i > 0; i--) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    if (common.lang.equals("ar")){
                    ((TextView) tabViewChild).setTypeface(Typeface.createFromAsset(MainActivity.this.getAssets(), "fonts/Cairo-Bold.ttf"));
                    } else {
                        ((TextView) tabViewChild).setTypeface(Typeface.createFromAsset(MainActivity.this.getAssets(), "fonts/Kabrio-Bold.ttf"));

                    }
                }
            }
        }
    }
    public static void changeToolbarFont(Toolbar toolbar, Activity context) {
        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof TextView) {
                TextView tv = (TextView) view;
                if (tv.getText().equals(toolbar.getTitle())) {
                    applyFont(tv, context);
                    break;
                }
            }
        }
    }
    public static void applyFont(TextView tv, Activity context) {
        tv.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Cairo-Bold.ttf"));
    }
}
