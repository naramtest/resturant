package com.emargystudio.myapplication;


import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.emargystudio.myapplication.common.SharedPreferenceManger;
import com.emargystudio.myapplication.dataBase.AppDatabase;
import com.emargystudio.myapplication.dataBase.AppExecutors;
import com.emargystudio.myapplication.model.Category;
import com.google.android.material.textfield.TextInputLayout;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import static com.parse.Parse.getApplicationContext;


public class CategoryFragment extends Fragment {

    private EditText updateEdtAr, addEdtAr , updateEdt , addEdt ;
    private ProgressBar progressBar;
    private TextInputLayout update_containerAr , update_container,add_container,add_containerAr;

    private Button updateBtn ,addBtn , deleteBtn , emptyBtn;

    private ArrayList<Category> parseCategories = new ArrayList<>();
    private List<Category> roomCategories = new ArrayList<>();
    private int id = 0;
    private SharedPreferenceManger sharedPreferenceManger;


    private Category category;



    private AppDatabase appDatabase;
    private Handler mHandler;
    private Handler hintHandler;



    public CategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        initFragmentViews(view);


        appDatabase = AppDatabase.getInstance(getContext());
        mHandler = new Handler();
        hintHandler = new Handler();
        sharedPreferenceManger = SharedPreferenceManger.getInstance(getContext());




        addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
               if (!TextUtils.isEmpty(addEdtAr.getText().toString()) &&!TextUtils.isEmpty(addEdt.getText().toString())){
                   boolean usedName = false;
                   for (Category category : parseCategories){

                       if (category.getName().equals(addEdtAr.getText().toString())
                               ||category.getEn_name().equals(addEdt.getText().toString())){
                           usedName = true;
                       }
                   }

                   if (usedName){
                       Toast.makeText(getContext(), "هذا الأسم مستخدم مسبقا الرجاء اختيار اسم اخر ثم المتابعة", Toast.LENGTH_SHORT).show();

                   }else {
                       saveObjectToParse();

                   }

               }else {
                    Toast.makeText(getContext(), "الرجاء إدخال اسم التصنيف قبل المتابعة", Toast.LENGTH_SHORT).show();
               }

                }
            });


        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (category!= null){
                    boolean needUpdate = false;
                    if (!TextUtils.isEmpty(updateEdtAr.getText().toString())) {
                        if (!updateEdtAr.getText().toString().equals(category.getName())) {
                            needUpdate = true;
                        }else {
                            Toast.makeText(getContext(), "هذا الأسم مستخدم مسبقا الرجاء اختيار اسم اخر", Toast.LENGTH_SHORT).show();
                        }


                    }
                    if (!TextUtils.isEmpty(updateEdt.getText().toString())){
                        if (!updateEdt.getText().toString().equals(category.getEn_name())) {
                            needUpdate = true;
                        }else {
                            Toast.makeText(getContext(), "هذا الأسم مستخدم مسبقا الرجاء اختيار اسم اخر", Toast.LENGTH_SHORT).show();
                        }
                    }

                    if (needUpdate){
                        getParseObjId();
                    }

                }else {
                    Toast.makeText(getContext(), "الرجاء اختيار تصنيف قبل المتابعة", Toast.LENGTH_SHORT).show();

                }
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    deleteCategory();
            }
        });

        emptyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEdt.setText("");
                addEdtAr.setText("");
                add_container.setHint("Category Name");
                add_containerAr.setHint("اسم التصنيف");

                addBtn.setVisibility(View.VISIBLE);
                emptyBtn.setVisibility(View.GONE);

            }
        });

        queryCategoryFromParse();
    }

    private void deleteCategory() {
        int category_id = category.getCategory_id();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("category");

        query.whereEqualTo("id",category_id);

        query.getFirstInBackground(new GetCallback<ParseObject>() {
           @Override
            public void done(ParseObject object, ParseException e) {
                if (e==null) {
                    object.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                           if(e==null)
                            Toast.makeText(getContext(), "تم حذفه", Toast.LENGTH_SHORT).show();
                           AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    appDatabase.categoryDao().deleteFood(category);
                                }
                            });
                        }
                    });
                }
            }
        });

        ParseQuery<ParseObject> queryFood = ParseQuery.getQuery("food");
        queryFood.whereEqualTo("category_id",category_id);

        queryFood.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    for (ParseObject parseObject : objects){

                        parseObject.deleteInBackground();
                   }
                }else {
                    Toast.makeText(getContext(), "حدث خطأ ما الرجاء المحاولة لاحقا", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    //find views and set typefaces
    private void initFragmentViews(@NonNull View view) {
        updateBtn = view.findViewById(R.id.updateBtn);
        addBtn = view.findViewById(R.id.addbtn);
        updateEdtAr = view.findViewById(R.id.update_category_ar);
        addEdtAr = view.findViewById(R.id.add_category_ar);
        updateEdt = view.findViewById(R.id.update_category);
        addEdt = view.findViewById(R.id.add_category);
        progressBar = view.findViewById(R.id.progressBar);
        update_container = view.findViewById(R.id.update_container);
        TextView textView = view.findViewById(R.id.textView);
        add_container = view.findViewById(R.id.add_container);
        update_containerAr = view.findViewById(R.id.name_container_ar);
        add_containerAr = view.findViewById(R.id.add_container_ar);
        deleteBtn = view.findViewById(R.id.delete);
        emptyBtn = view.findViewById(R.id.empty);


        Typeface bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Cairo-Bold.ttf");
        Typeface regular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Cairo-SemiBold.ttf");

        textView.setTypeface(bold);
        updateBtn.setTypeface(bold);
        addBtn.setTypeface(bold);
        update_containerAr.setTypeface(regular);
        update_container.setTypeface(regular);
        add_container.setTypeface(regular);
        updateEdtAr.setTypeface(regular);
        addEdtAr.setTypeface(regular);
        updateEdt.setTypeface(regular);
        addEdt.setTypeface(regular);
        add_containerAr.setTypeface(regular);
        deleteBtn.setTypeface(bold);
        emptyBtn.setTypeface(bold);
    }

    private void queryCategoryFromParse(){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("category");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null){
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

                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void queryCategoryFromRoom(){
        if (sharedPreferenceManger.getFirstTime() == 0){
            camperTowDataBases();
        }else {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
               roomCategories = appDatabase.categoryDao().loadAllFoodsAdapter();

               if (!roomCategories.isEmpty()){
                   mHandler.post(new Runnable() {
                       @Override
                       public void run() {
                           camperTowDataBases();
                       }
                   });
               }
            }
        });
        }

    }
    private void camperTowDataBases() {

        for (Category category : parseCategories){
            boolean exist = false;
            for (Category category1 : roomCategories){
                if (category.getCategory_id()==category1.getCategory_id()){
                    if (category.getVersionNumber()!=category1.getVersionNumber()){
                        category1.setName(category.getName());
                        category1.setEn_name(category.getEn_name());
                        category1.setVersionNumber(category.getVersionNumber());
                        updateCategoriesInRoom(category1);
                    }
                    exist = true;
                    break;
                }else {
                    exist = false;
                }
            }
            if (!exist){
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


    //save category
    private void saveObjectToParse(){

        progressBar.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("category");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                progressBar.setVisibility(View.GONE);
                if (e == null){
                    // Adding objects into the Array
                    for(int i= 0 ; i < objects.size(); i++){

                        ParseObject categoryObject = objects.get(i);
                        if (categoryObject.getInt("id")> id){
                            id = categoryObject.getInt("id");
                        }
                    }

                    saveCategory();

                } else {

                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });



    }
    private void saveCategory() {
        // Configure Query
        ParseObject category = new ParseObject("category");

        // Store an object
        category.put("id", id+1);
        category.put("name", addEdtAr.getText().toString());
        category.put("versionNumber", 1);
        category.put("en_name",addEdt.getText().toString());



        // Saving object
        category.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getContext(), "تم إضافة التصنيف", Toast.LENGTH_SHORT).show();
                    addBtn.setVisibility(View.GONE);
                    emptyBtn.setVisibility(View.VISIBLE);
                    Category category1 = new Category(id+1,
                            addEdtAr.getText().toString(),
                            addEdt.getText().toString(),
                            1);
                    saveCategoryToRoom(category1);
                } else {
                    Toast.makeText(
                            getContext(),
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
    }
    private void saveCategoryToRoom(final Category category1){
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                appDatabase.categoryDao().insertFood(category1);
            }
        });
    }


    //update category
    private void getParseObjId() {

        progressBar.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("category");

        // Query Parameters
        query.whereEqualTo("id", category.getCategory_id());

        // How we need retrive exactly one result we can use the getFirstInBackground method
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {

                    boolean usedName = false;
                    for (Category parseCategory :parseCategories){
                        if (parseCategory.getName().equals(updateEdtAr.getText().toString()) ||
                                parseCategory.getEn_name().equals(updateEdt.getText().toString())){
                            usedName = true;
                            break;
                        }
                    }
                    if (!usedName){
                        String objectId = object.getObjectId();
                        int versionNumber = object.getInt("versionNumber");
                        update(objectId,versionNumber);
                    }else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "هذا الأسم مستخدم مسبقا مع تصنيف اخر ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(
                            getApplicationContext(),
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
    }
    private void update(final String objectId , final int versionNumber){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("category");
        // Retrieve the object by id
       query.getInBackground(objectId, new GetCallback<ParseObject>() {
           @Override
           public void done(ParseObject object, ParseException e) {

               progressBar.setVisibility(View.GONE);
               if (e == null){
                   if (!TextUtils.isEmpty(updateEdtAr.getText().toString())){
                       object.put("name", updateEdtAr.getText().toString());
                       category.setName(updateEdtAr.getText().toString());
                   }
                   if (!TextUtils.isEmpty(updateEdt.getText().toString())){
                       object.put("en_name",updateEdt.getText().toString());
                       category.setEn_name(updateEdt.getText().toString());
                   }

                   object.put("versionNumber",versionNumber+1);
                   category.setVersionNumber(versionNumber+1);

                   object.saveInBackground(new SaveCallback() {
                       @Override
                       public void done(ParseException e) {
                           if (e == null){
                               Toast.makeText(getContext(), "تم تعديل التصنيف بنجاح", Toast.LENGTH_SHORT).show();
                               AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                   @Override
                                   public void run() {
                                       appDatabase.categoryDao().updateFood(category);
                                   }
                               });
                           }else {
                               Toast.makeText(
                                       getApplicationContext(),
                                       e.getMessage(),
                                       Toast.LENGTH_LONG
                               ).show();
                           }
                       }
                   });
               }else {
                   Toast.makeText(
                           getApplicationContext(),
                           e.getMessage(),
                           Toast.LENGTH_LONG
                   ).show();
               }
           }
       });


    }



    //get category name from tabLayout and set it as a hint for editTexts
    void changeEditText(final int category_id){
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                category = appDatabase.categoryDao().loadCategoryByCategoryID(category_id);
                if (category!=null) {
                    hintHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setEditTextsHint(category.getName(),category.getEn_name());
                        }
                    });
                }

            }
        });
    }
    private void setEditTextsHint(String arHint ,String enHint){
        update_containerAr.setHint(arHint);
        update_container.setHint(enHint);

    }
}
