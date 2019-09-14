package com.emargystudio.myapplication;


import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.emargystudio.myapplication.common.SharedPreferenceManger;
import com.emargystudio.myapplication.common.common;
import com.emargystudio.myapplication.dataBase.AppDatabase;
import com.emargystudio.myapplication.dataBase.AppExecutors;
import com.emargystudio.myapplication.model.Category;
import com.emargystudio.myapplication.model.Food;
import com.google.android.material.textfield.TextInputLayout;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.parse.Parse.checkInit;
import static com.parse.Parse.getApplicationContext;


public class FoodFragment extends Fragment {


    private static final String TAG = "FoodFragment";
    private static final String ADMIN_CHANNEL_ID ="admin_channel";

    private NotificationManager notificationManager;

    private ImageView imageView;
    private Button choosePic , editDataBtn , editPhotoBtn ,saveBtn , emptyBtn;
    private ProgressBar progressBar;
    private EditText nameEdt , descriptionEdt , priceEdt
            ,nameEdtAR , descriptionEdtAR ;
    private TextInputLayout nameContainer , descriptionContainer , priceContainer
            ,nameContainerAr , descriptionContainerAR ;
    private LinearLayout editContainer , saveContainer;

    private final int GALLARY_PICK = 2;
    private boolean OkToUpload;
    private Bitmap bitmap;


    //dialog views
    private ImageView  upload_image ;
    private FrameLayout rotateBtn ;
    private Button upload_btn ,cancel_btn;
    private int id = 0;

    private Category category;
    private AppDatabase appDatabase;


    //compare vars
    private ArrayList<Food> parseFoods = new ArrayList<>() ;
    private List<Food> roomFoods = new ArrayList<>() ;
    private Handler mHandler;
    private boolean exist;

    private boolean is_edit;
    private Food food;


    private SharedPreferenceManger sharedPreferenceManger;



    public FoodFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_food, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        //init method call
        initViewsAndFonts(view);

        appDatabase  = AppDatabase.getInstance(getContext());
        sharedPreferenceManger = SharedPreferenceManger.getInstance(getContext());
        mHandler = new Handler();

        queryFoodFromParse();




        choosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkEditTexts()) {
                    boolean isUsed = false ;
                    for (Food food : parseFoods) {
                        if (food.getName().equals(nameEdtAR.getText().toString())
                                ||food.getEn_name().equals(nameEdt.getText().toString())){
                            isUsed = true;
                        }
                    }

                    if (isUsed){
                        Toast.makeText(getContext(), "هذا الأسم مستخدما مسبقا الرجاء اختيار اسم اخر ثم المتابعة", Toast.LENGTH_SHORT).show();
                    }else {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setType("image/*");
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLARY_PICK);

                    }
                }
            }
        });


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (is_edit){
                     editFoodData(food,true);

                }else {
                    uploadStory();
                }
            }
        });


        emptyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               nameEdt.setText("");
               descriptionEdt.setText("");
                priceEdt.setText("");
                nameEdtAR.setText("");
                descriptionEdtAR.setText("");
//                imageView.setImageBitmap(null);
                category = null;
                is_edit = false;
                nameContainer.setHint("name");
                nameContainerAr.setHint("الأسم");
                descriptionContainerAR.setHint("الوصف");
                descriptionContainer.setHint("Description");
                priceContainer.setHint("السعر");
//
//                choosePic.setVisibility(View.VISIBLE);
//                editContainer.setVisibility(View.GONE);
//                saveContainer.setVisibility(View.GONE);

                Fragment frg = null;
                frg = getActivity().getSupportFragmentManager().findFragmentByTag("food");
                final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.detach(frg);
                ft.attach(frg);
                ft.commit();



            }
        });


        if (getFoodFromBundle()!=null){
            food = getFoodFromBundle();
            choosePic.setVisibility(View.GONE);
            editContainer.setVisibility(View.VISIBLE);
            is_edit= true;
            setupViews(food);
        }else {
            is_edit = false;
            choosePic.setVisibility(View.VISIBLE);
            editContainer.setVisibility(View.GONE);
        }


    }

    //find fragment views and set fonts
    private void initViewsAndFonts(@NonNull View view) {
        imageView = view.findViewById(R.id.imageView);
        choosePic  = view.findViewById(R.id.add1_btn);
        progressBar = view.findViewById(R.id.progressBar);
        nameEdt = view.findViewById(R.id.name_edt);
        priceEdt = view.findViewById(R.id.price_edt);
        descriptionEdt = view.findViewById(R.id.description_edt);
        editDataBtn = view.findViewById(R.id.edit_data_btn);
        editPhotoBtn = view.findViewById(R.id.edit_photo_btn);
        priceContainer = view.findViewById(R.id.price_container);
        nameContainer =view.findViewById(R.id.name_container);
        nameContainerAr =view.findViewById(R.id.name_container_ar);
        descriptionContainer = view.findViewById(R.id.description_container);
        descriptionContainerAR = view.findViewById(R.id.description_container_ar);
        descriptionEdtAR = view.findViewById(R.id.description_edt_ar);
        nameEdtAR = view.findViewById(R.id.name_edt_ar);
        saveBtn = view.findViewById(R.id.save_btn);
        emptyBtn = view.findViewById(R.id.empty_btn);
        editContainer = view.findViewById(R.id.edit_container);
        saveContainer = view.findViewById(R.id.save_container);


        Typeface face = Typeface.createFromAsset(getContext().getAssets(),"fonts/Cairo-Bold.ttf");
        Typeface faceRegular = Typeface.createFromAsset(getContext().getAssets(),"fonts/Cairo-SemiBold.ttf");
        nameEdt.setTypeface(faceRegular);
        nameEdtAR.setTypeface(faceRegular);
        nameContainer.setTypeface(faceRegular);
        nameContainerAr.setTypeface(faceRegular);
        descriptionEdt.setTypeface(faceRegular);
        descriptionEdtAR.setTypeface(faceRegular);
        descriptionContainer.setTypeface(faceRegular);
        descriptionContainerAR.setTypeface(faceRegular);
        priceContainer.setTypeface(faceRegular);
        priceEdt.setTypeface(faceRegular);
        editPhotoBtn.setTypeface(face);
        choosePic.setTypeface(face);
        editDataBtn.setTypeface(face);
        emptyBtn.setTypeface(face);
        saveBtn.setTypeface(face);
    }

    //if this fragment open to edit food set the value of old food on the editTexts
    private void setupViews(final Food food) {


        nameContainer.setHint(food.getEn_name());
        nameContainerAr.setHint(food.getName());
        descriptionContainerAR.setHint(food.getDescription());
        descriptionContainer.setHint(food.getEn_description());
        Picasso.get().load(new File(food.getImage_uri())).into(imageView);
        priceContainer.setHint(String.valueOf(food.getPrice()));

        editDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(nameEdt.getText().toString())
                        && TextUtils.isEmpty(nameEdtAR.getText().toString())
                                && TextUtils.isEmpty(descriptionEdt.getText().toString())
                                && TextUtils.isEmpty(descriptionEdtAR.getText().toString())
                                && TextUtils.isEmpty(priceEdt.getText().toString())){

                    Toast.makeText(getContext(), "لم تقم بتغير أي من البيانات ", Toast.LENGTH_SHORT).show();

                }else {
                    editFoodData(food,false);

                }

            }
        });

        editPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLARY_PICK);

            }
        });
    }
    private Food getFoodFromBundle() {

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable("food");
        } else {
            return null;
        }
    }

    private void editFoodData(final Food food , final boolean withPhoto) {

        progressBar.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("food");

        // Query Parameters
        query.whereEqualTo("id", food.getFood_id());

        // How we need retrive exactly one result we can use the getFirstInBackground method
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {

                    progressBar.setVisibility(View.GONE);

                    boolean usedName = false;
                    boolean usedEnglishName = false;
                    String nameStrAr = nameEdtAR.getText().toString();
                    String nameStr = nameEdt.getText().toString();
                    for (Food parsefood :parseFoods){
                        if (parsefood.getName().equals(nameStrAr)){
                            usedName = true;

                        }
                        if (parsefood.getEn_name().equals(nameStr)){
                            usedEnglishName = true;

                        }
                    }


                        int versionNumber = object.getInt("versionNumber");
                        if (!TextUtils.isEmpty(nameStrAr) && !nameStrAr.equals(food.getName()) ){
                            if (usedName){
                                Toast.makeText(getContext(), "هذا الأسم مستخدم مسبقا الرجاء اختيار اسم اخر", Toast.LENGTH_SHORT).show();
                            }else {
                                object.put("name", nameStrAr);
                                food.setName(nameStrAr);
                            }
                        }

                    if (!TextUtils.isEmpty(nameStr) && !nameStr.equals(food.getEn_name()) ){
                        if (usedEnglishName){
                            Toast.makeText(getContext(), "هذا الأسم مستخدم مسبقا الرجاء اختيار اسم اخر", Toast.LENGTH_SHORT).show();
                        }else {
                            object.put("en_name", nameStr);
                            food.setName(nameStr);
                        }
                    }
                    String descriptionStrAr = descriptionEdtAR.getText().toString();
                    if (!TextUtils.isEmpty(descriptionStrAr) && !descriptionStrAr.equals(food.getDescription())){
                            object.put("description", descriptionStrAr);
                            food.setDescription(descriptionStrAr);
                        }

                    String descriptionStr = descriptionEdt.getText().toString();
                    if (!TextUtils.isEmpty(descriptionStr) && !descriptionStr.equals(food.getEn_description())){
                        object.put("en_description", descriptionStr);
                        food.setDescription(descriptionStr);
                    }
                    String priceStr = priceEdt.getText().toString();
                    if (!TextUtils.isEmpty(priceStr) && !priceStr.equals(String.valueOf(food.getPrice()))){
                            object.put("price",Integer.parseInt(priceStr));
                            food.setPrice(Integer.parseInt(priceStr));
                        }
                        object.put("versionNumber",versionNumber+1);
                        food.setVersionNumber(versionNumber+1);
                        if(category!=null){
                            if(category.getCategory_id()!=food.getCategory_id()){
                                object.put("category_id",category.getCategory_id());
                                food.setCategory_id(category.getCategory_id());
                            }

                        }


                        if (withPhoto){


                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                            byte[] byteArray = stream.toByteArray();
                            ParseFile file = new ParseFile("image.png", byteArray);
                            object.put("image", file);
                            Uri uri = saveImageToInternalStorage(bitmap,food.getName());
                            food.setImage_uri(uri.toString());

                        }

                        object.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e==null){
                                    if (withPhoto){
                                        sendNotification();
                                    }
                                    Toast.makeText(getContext(), "تم التعديل بنجاح", Toast.LENGTH_SHORT).show();
                                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            appDatabase.foodDao().updateFood(food);
                                        }
                                    });

                                    getActivity().getSupportFragmentManager().popBackStack();
                                }
                            }
                        });

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





    private void uploadImageDialog(){
        final Dialog dialog = new Dialog(getContext());
        if (dialog.getWindow()!=null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setContentView(R.layout.update_image_dialog);


        initDialogViews(dialog);
        changeTxtViewFonts();

        upload_image.setImageBitmap(bitmap);


        rotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = rotateBitmap(bitmap,90);
                upload_image.setImageBitmap(bitmap);
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                imageView.setImageBitmap(bitmap);
                choosePic.setVisibility(View.GONE);
                editContainer.setVisibility(View.GONE);
                saveContainer.setVisibility(View.VISIBLE);


            }
        });

        dialog.show();
    }
    private void uploadStory() {
        progressBar.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("food");
        query.setLimit(1000);
        query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    progressBar.setVisibility(View.GONE);

                    for (int i = 0; i < objects.size(); i++) {

                        ParseObject foodObject = objects.get(i);
                        if (foodObject.getInt("id") > id) {
                            id = foodObject.getInt("id");
                        }
                    }

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                    byte[] byteArray = stream.toByteArray();
                    ParseFile file = new ParseFile("image.png", byteArray);
                    final ParseObject object = new ParseObject("food");


                    object.put("image", file);
                    object.put("id", id + 1);
                    object.put("category_id", category.getCategory_id());
                    object.put("name", nameEdtAR.getText().toString());
                    object.put("en_name", nameEdt.getText().toString());
                    object.put("en_description", descriptionEdt.getText().toString());
                    object.put("description", descriptionEdtAR.getText().toString());
                    object.put("price", Integer.parseInt(priceEdt.getText().toString()));
                    object.put("versionNumber", 1);
                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                sendNotification();
                                Food food = new Food(
                                        id + 1,
                                        category.getCategory_id(),
                                        nameEdtAR.getText().toString(),
                                        descriptionEdtAR.getText().toString(),
                                        nameEdt.getText().toString(),
                                        descriptionEdt.getText().toString(),
                                        Integer.parseInt(priceEdt.getText().toString()),
                                        1
                                );
                                findFoodFromPars(food, 1);
                                imageView.setImageBitmap(bitmap);
                                Toast.makeText(getContext(), "تم رفع الصورة بنجاح", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "حدث خطـأ ما الرجاء المحاولة لاحقا", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });





    }



    //handling tabs press to set value of category
    void getCategoryFromTabs(final String text , final int category_id){
        //updateEdt.setText(text);
        if (is_edit){
            alertSend(text,category_id);
        }else {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    category = appDatabase.categoryDao().loadCategoryByCategoryID(category_id);

                }
            });
        }
    }
    private void alertSend(String text,final int category_id) {
        if (getContext() != null) {

            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle("تعديل التصنيف");
            if (common.lang.equals("ar")){
                alert.setMessage("هل تريد نقل "+food.getName()+" إلى "+text);

            }else {
                alert.setMessage("هل تريد نقل "+food.getEn_name()+" إلى "+text);

            }

            alert.setPositiveButton("تأكيد", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            category = appDatabase.categoryDao().loadCategoryByCategoryID(category_id);

                        }
                    });
                }
            });
            alert.setNegativeButton("إلغاء", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = alert.create();

            dialog.show();
            TextView textView =  dialog.getWindow().findViewById(android.R.id.message);
            TextView alertTitle =  dialog.getWindow().findViewById(R.id.alertTitle);
            Button button1 =  dialog.getWindow().findViewById(android.R.id.button1);
            Button button2 =  dialog.getWindow().findViewById(android.R.id.button2);


            Typeface faceRegular = Typeface.createFromAsset(getContext().getAssets(),"fonts/Cairo-Regular.ttf");
            Typeface face = Typeface.createFromAsset(getContext().getAssets(),"fonts/Cairo-SemiBold.ttf");

            alertTitle.setTypeface(face);
            textView.setTypeface(faceRegular);
            button1.setTypeface(face);
            button2.setTypeface(face);

            alertTitle.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);




        }
    }

    //method to sync to dataBases before add or edit any thing
    //query all of foods in parse to sync with the data in Room
    private void queryFoodFromParse(){
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

                    queryCategoryFromRoom();
                } else {

                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void queryCategoryFromRoom(){
        if (sharedPreferenceManger.getFirstFood() == 0){
            camperTowDataBases();
        }else {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    roomFoods = appDatabase.foodDao().loadAllFoodsAdapter();

                    if (!roomFoods.isEmpty()){
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

        for (Food food : parseFoods){
            exist = false;
            for (Food food1 : roomFoods){
                if (food.getFood_id()==food1.getFood_id()){
                    if (food.getVersionNumber()!=food1.getVersionNumber()){
                        food1.setName(food.getName());
                        food1.setEn_name(food.getEn_name());
                        food1.setVersionNumber(food.getVersionNumber());
                        food1.setDescription(food.getDescription());
                        food1.setEn_description(food.getEn_description());
                        food1.setPrice(food.getPrice());


                        findFoodFromPars(food1,0);
                    }
                    exist = true;
                    break;
                }else {
                    exist = false;
                }
            }
            if (!exist){
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
    //0 = update 1= add
    private void findFoodFromPars(final Food food , final int status){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("food");

        query.whereEqualTo("id", food.getFood_id());

        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {

                if (e == null){

                    if (object!=null){
                        ParseFile file = (ParseFile) object.get("image");
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                        int width = displayMetrics.widthPixels;
                        try {
                            Bitmap bitmap = common.decodeFile(file.getFile(),width,200);
                            Uri uri = saveImageToInternalStorage(bitmap,food.getName());

                            food.setImage_uri(uri.toString());
                            if (status == 0){

                                updateCategoriesInRoom(food);
                            }else {
                                saveCategoryToRoom(food);
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
    private void updateCategoriesInRoom(final Food food) {

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                appDatabase.foodDao().updateFood(food);
            }
        });
    }
    private void saveCategoryToRoom(final Food food){
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                appDatabase.foodDao().insertFood(food);
            }
        });
    }


    //image helper methods
    // Custom method to save a bitmap into internal storage
    private Uri saveImageToInternalStorage(Bitmap bitmap, String name){
        // Initialize ContextWrapper
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());

        // Initializing a new file
        // The bellow line return a directory in internal storage
        File file = wrapper.getDir("Images",MODE_PRIVATE);

        // Create a file to save the image
        file = new File(file, name+".jpg");

        try{
            // Initialize a new OutputStream
            OutputStream stream = null;

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
        Uri savedImageURI = Uri.parse(file.getAbsolutePath());

        // Return the saved image Uri
        return savedImageURI;
    }
    private static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    //return true if any edit text has no text on it
    private boolean checkEditTexts(){
        boolean isEmpty = false;
        if (category==null){
            isEmpty=true;
            Toast.makeText(getContext(), "الرجاء اختيار تصنيف من الأعلى قبل المتابعة", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(nameEdt.getText().toString())){
            isEmpty=true;
            Toast.makeText(getContext(), "الرجاء ادخال الأسم قبل المتابعة", Toast.LENGTH_SHORT).show();

        }else if (TextUtils.isEmpty(descriptionEdt.getText().toString())){
            isEmpty=true;
            Toast.makeText(getContext(), "الرجاء اضافة الوصف بالإنكليزية قبل المتابعة", Toast.LENGTH_SHORT).show();

        }else if (TextUtils.isEmpty(priceEdt.getText().toString())){
            isEmpty=true;
            Toast.makeText(getContext(), "الرجاء ادخال السعر قبل المتابعة", Toast.LENGTH_SHORT).show();

        }else if (TextUtils.isEmpty(descriptionEdtAR.getText().toString())){
            isEmpty=true;
            Toast.makeText(getContext(), "الرجاء ادخال الوصف بالعربي قبل المتابعة", Toast.LENGTH_SHORT).show();

        }else if (TextUtils.isEmpty(nameEdtAR.getText().toString())){
            isEmpty=true;
            Toast.makeText(getContext(), "الرجاء ادخال الأسم قبل المتابعة", Toast.LENGTH_SHORT).show();
        }

        return isEmpty;
    }

    //dialog init views and fonts
    private void changeTxtViewFonts() {
        Typeface face = Typeface.createFromAsset(getContext().getAssets(),"fonts/Cairo-Bold.ttf");
        Typeface faceLight = Typeface.createFromAsset(getContext().getAssets(),"fonts/Cairo-Light.ttf");
        cancel_btn.setTypeface(faceLight);
        upload_btn.setTypeface(face);

    }
    private void initDialogViews(Dialog dialog) {

        rotateBtn = dialog.findViewById(R.id.rotate);
        upload_image = dialog.findViewById(R.id.update_image);
        upload_btn    = dialog.findViewById(R.id.upload_btn);
        cancel_btn = dialog.findViewById(R.id.cancel_btn);
    }


    //send notification when the image finish downloading
    private void sendNotification(){

        notificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = new Random().nextInt(60000);

        NotificationCompat.Builder notificationBuilder;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
            Intent intent = new Intent(getContext(), MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
            stackBuilder.addNextIntentWithParentStack(intent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder = getNotificationBuilder( resultPendingIntent, ADMIN_CHANNEL_ID);


        }else {
            Intent intent = new Intent(getContext(), MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
            stackBuilder.addNextIntentWithParentStack(intent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder = getNotificationBuilder( resultPendingIntent, ADMIN_CHANNEL_ID);
        }



        notificationManager.notify(notificationId /* ID of notification */, notificationBuilder.build());

    }
    private NotificationCompat.Builder getNotificationBuilder( PendingIntent resultPendingIntent, String reservationChannelId) {
        return new NotificationCompat.Builder(getContext(), reservationChannelId)
                .setSmallIcon(R.mipmap.ic_launcher)  //a resource for your custom small icon
                .setContentTitle("ايمارجي") //the "title" value you sent in your notification
                .setContentText("تم رفع الصورة بنجاح") //ditto
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels() {
        CharSequence adminChannelName = getString(R.string.notifications_admin_channel_name);
        String adminChannelDescription = getString(R.string.notifications_admin_channel_description);
        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLARY_PICK){

            if(resultCode == RESULT_OK){


                OkToUpload = true;
                Uri uri =   data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),uri);
                    if(bitmap != null){
                        uploadImageDialog();
                    }else{
                        Toast.makeText(getContext(),"error",Toast.LENGTH_LONG).show();
                    } //String path = getPath(PreferencesActivity.this,uri);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                // captured_iv.setImageBitmap(bitmap);

            }


        }
    }
}
