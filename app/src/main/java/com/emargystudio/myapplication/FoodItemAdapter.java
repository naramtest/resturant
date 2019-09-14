package com.emargystudio.myapplication;


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emargystudio.myapplication.common.SharedPreferenceManger;
import com.emargystudio.myapplication.common.common;
import com.emargystudio.myapplication.model.Food;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;


public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder>{

    private List<Food> foods;
    private Context context;
    public DetailsAdapterListener onClickListener;
    private SharedPreferenceManger sharedPreferenceManger;
    boolean is_logged_in;


    public FoodItemAdapter(List<Food> foods, Context context, DetailsAdapterListener onClickListener) {
        this.foods = foods;
        this.context = context;
        this.onClickListener = onClickListener;
        sharedPreferenceManger = SharedPreferenceManger.getInstance(context);
        is_logged_in = sharedPreferenceManger.getLogginInStatus();
    }

    @NonNull
    @Override
    public FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item, parent, false);

        return new FoodItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final FoodItemViewHolder holder, final int position) {
        Food food = foods.get(position);

        if (common.lang.equals("ar")){
            holder.priceTxt.setText(food.getPrice()+" ل.س");
            holder.descriptionTxt.setText(food.getDescription());
            holder.nameTxt.setText(food.getName());

        }else {
            holder.priceTxt.setText(food.getPrice()+" S.P");
            holder.descriptionTxt.setText(food.getEn_description());
            holder.nameTxt.setText(food.getEn_name());
        }
        Picasso.get().load(new File(food.getImage_uri())).fit().centerCrop().into(holder.imageView);

        holder.deleteCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.deleteCartItem(holder,position);
            }
        });

        holder.editCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.editCartItem(holder,position);
            }
        });

        holder.refreshFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.refreshCartITem(holder,position);
            }
        });



    }

    @Override
    public int getItemCount() {
        if (foods == null) {
            return 0;
        }
        return foods.size();
    }


    void setTasks(List<Food> taskEntries) {
        foods = taskEntries;
        notifyDataSetChanged();
    }

    void removeItem(int position) {
        foods.remove(position);
        notifyItemRemoved(position);
    }

    void restoreItem(Food item, int position) {
        foods.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }

    public interface DetailsAdapterListener {

        void deleteCartItem(RecyclerView.ViewHolder v, int position);

        void editCartItem(RecyclerView.ViewHolder v, int position);

        void refreshCartITem(RecyclerView.ViewHolder v, int position);
    }




    class FoodItemViewHolder extends RecyclerView.ViewHolder {


        ImageView imageView;
        TextView nameTxt , descriptionTxt , priceTxt;
        ImageButton deleteCart , editCart ,refreshFood;


        FoodItemViewHolder(@NonNull View itemView) {
            super(itemView);
          imageView = itemView.findViewById(R.id.food_image);
          nameTxt = itemView.findViewById(R.id.name_text);
          descriptionTxt = itemView.findViewById(R.id.description_text);
          priceTxt = itemView.findViewById(R.id.price_text);
          deleteCart= itemView.findViewById(R.id.delete_item);
          editCart  = itemView.findViewById(R.id.edit_cart);
          refreshFood = itemView.findViewById(R.id.refresh_cart);


          if (!is_logged_in){
              deleteCart.setVisibility(View.GONE);
              editCart.setVisibility(View.GONE);
              refreshFood.setVisibility(View.GONE);
          }

            Typeface bold = Typeface.createFromAsset(context.getAssets(), "fonts/Cairo-Bold.ttf");
            Typeface regular = Typeface.createFromAsset(context.getAssets(), "fonts/Cairo-SemiBold.ttf");


            nameTxt.setTypeface(bold);
            descriptionTxt.setTypeface(regular);
            priceTxt.setTypeface(bold);
        }

    }
}