package com.emargystudio.myapplication;


import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.emargystudio.myapplication.common.common;
import com.emargystudio.myapplication.dataBase.AppDatabase;
import com.emargystudio.myapplication.dataBase.AppExecutors;
import com.emargystudio.myapplication.dataBase.FoodViewModel;
import com.emargystudio.myapplication.model.Food;
import com.google.android.material.snackbar.Snackbar;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.parse.Parse.getApplicationContext;


public class MainFragment extends Fragment {

    private List<Food> foodOrders;
    List<Food> foodList;
    private AppDatabase mDb;
    private FoodItemAdapter foodItemAdapter;
    private TextView textView;
    private RecyclerView recyclerView;
    private LinearLayout linearLayout;
    private boolean recover;
    private Handler handler;


    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        foodOrders = new ArrayList<>();
        foodList = new ArrayList<>();

        handler = new Handler();

        mDb = AppDatabase.getInstance(getContext());
        foodItemAdapter = new FoodItemAdapter(foodList,getContext(),new FoodItemAdapter.DetailsAdapterListener() {
            @Override
            public void deleteCartItem(RecyclerView.ViewHolder v, int position) {
                String name = foodList.get(position).getName();

                // backup of removed item for undo purpose
                final Food deletedItem = foodList.get(position);
                final int deletedIndex = position;

                 recover = false;

                // remove the item from recycler view
                foodItemAdapter.removeItem(position);
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.foodDao().deleteFood(deletedItem);

                    }
                });


                // showing snack bar with Undo option
                Snackbar snackbar = Snackbar
                        .make(linearLayout, name + "تم حذف ", Snackbar.LENGTH_LONG);
                snackbar.setAction("ترجاع", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // undo is selected, restore the deleted item
                        foodItemAdapter.restoreItem(deletedItem, deletedIndex);
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mDb.foodDao().insertFood(deletedItem);
                                recover = true;

                            }
                        });
                    }
                });

                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();


                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!recover){
                            deleteFromParse(deletedItem);
                        }
                        //Do something after 100ms
                    }
                }, 3500);

            }


            @Override
            public void editCartItem(RecyclerView.ViewHolder v, int position) {

                editFood(foodList.get(position));

            }

            @Override
            public void refreshCartITem(RecyclerView.ViewHolder v, int position) {

                refreshFood(foodList.get(position));

            }
        });
        recyclerView = view.findViewById(R.id.cart_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(foodItemAdapter);
        recyclerView.setHasFixedSize(true);
        textView = view.findViewById(R.id.category_header_name);
        linearLayout = view.findViewById(R.id.linearLayout);

        Typeface bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Cairo-Bold.ttf");
        textView.setTypeface(bold);


        loadFoods();


    }

    private void editFood(Food food) {
        Bundle args = new Bundle();
        args.putParcelable("food", food);
        Fragment foodFragment = new FoodFragment();
        foodFragment.setArguments(args);
        if (getActivity()!=null) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ft.replace(R.id.your_placeholder, foodFragment, "food");
            ft.addToBackStack("food");
            ft.commit();
        }
    }

    private void deleteFromParse(Food food){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("food");

        query.whereEqualTo("id", food.getFood_id());


        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {


                object.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        Toast.makeText(getContext(), "تم", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void refreshFood(final Food food) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("food");

        query.whereEqualTo("id", food.getFood_id());

        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                final Food newFood = new Food(
                        object.getInt("id"),
                        object.getInt("category_id"),
                        object.getString("name"),
                        object.getString("description"),
                        object.getString("en_name"),
                        object.getString("en_description"),
                        object.getInt("price"),
                        object.getInt("versionNumber")
                );

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                int width = displayMetrics.widthPixels;
                final ParseFile file = (ParseFile) object.get("image");
                try {
                    Bitmap bitmap = decodeFile(file.getFile(),width,200);
                    Uri uri = saveImageToInternalStorage(bitmap, food.getName());

                    food.setImage_uri(uri.toString());
                    food.setName(newFood.getName());
                    food.setEn_name(newFood.getEn_name());
                    food.setDescription(newFood.getDescription());
                    food.setEn_description(newFood.getEn_description());
                    food.setPrice(newFood.getPrice());
                    food.setVersionNumber(newFood.getVersionNumber());
                    food.setCategory_id(newFood.getCategory_id());

                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            mDb.foodDao().updateFood(food);
                        }
                    });
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
//                if (file!=null){
//                    file.getDataInBackground(new GetDataCallback() {
//                        @Override
//                        public void done(byte[] data, ParseException e) {
//                            if (e == null && data != null) {
//
//                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//
//                                Uri uri = saveImageToInternalStorage(bitmap, food.getName());
//
//                                food.setImage_uri(uri.toString());
//                                food.setName(newFood.getName());
//                                food.setEn_name(newFood.getEn_name());
//                                food.setDescription(newFood.getDescription());
//                                food.setEn_description(newFood.getEn_description());
//                                food.setPrice(newFood.getPrice());
//                                food.setVersionNumber(newFood.getVersionNumber());
//                                food.setCategory_id(newFood.getCategory_id());
//
//                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        mDb.foodDao().updateFood(food);
//                                    }
//                                });
//
//                            }
//                        }
//                    });
//                }

            }
        });
    }

    public static Bitmap decodeFile(File f,int WIDTH,int HIGHT){
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);

            //The new size we want to scale to
            final int REQUIRED_WIDTH=WIDTH;
            final int REQUIRED_HIGHT=HIGHT;
            //Find the correct scale value. It should be the power of 2.
            int scale=1;
            while(o.outWidth/scale/2>=REQUIRED_WIDTH && o.outHeight/scale/2>=REQUIRED_HIGHT)
                scale*=2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
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


    private void loadFoods(){

        FoodViewModel foodViewModel = ViewModelProviders.of(this).get(FoodViewModel.class);

        foodViewModel.getTasks().observe(this, new Observer<List<Food>>() {
            @Override
            public void onChanged(List<Food> foods1) {

                foodOrders = foods1;
                if (!foodList.isEmpty()){
                    foodList.clear();
                }
                for (Food food : foods1){
                    if (food.getCategory_id() == common.tabNumber){
                        foodList.add(food);
                    }
                }
                foodItemAdapter.setTasks(foodList);
                foodItemAdapter.notifyDataSetChanged();


            }
        });
    }

    void setHeaderText(String text){

        textView.setText(text);
        if (!foodList.isEmpty()){
            foodList.clear();
        }
        for (Food food : foodOrders){
            if (food.getCategory_id() == common.tabNumber){
                foodList.add(food);
            }
        }
        foodItemAdapter.setTasks(foodList);
        foodItemAdapter.notifyDataSetChanged();

    }

}
