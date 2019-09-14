package com.emargystudio.myapplication.dataBase;



import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.emargystudio.myapplication.model.Food;

import java.util.List;


@Dao
public interface FoodDao {

    @Query("SELECT * FROM food")
    LiveData<List<Food>> loadAllFoods();

    @Query("SELECT * FROM food WHERE category_id = :food_id")
    LiveData<List<Food>> idQuery(int food_id);

    @Query("SELECT * FROM food")
    List<Food> loadAllFoodsAdapter();

    @Insert
    void insertFood(Food foodMenu);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFood(Food foodMenu);

    @Delete
    void deleteFood(Food foodMenu);

    @Query("DELETE FROM food")
    void deleteAllFood();


    @Query("SELECT * FROM food WHERE id = :id")
    Food loadFoodById(int id);

    @Query("SELECT * FROM food WHERE name = :name")
    Food loadFoodByName(String name);
}
