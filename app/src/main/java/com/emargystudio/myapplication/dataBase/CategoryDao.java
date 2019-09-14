package com.emargystudio.myapplication.dataBase;



import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.emargystudio.myapplication.model.Category;
import com.emargystudio.myapplication.model.Food;

import java.util.List;


@Dao
public interface CategoryDao {

    @Query("SELECT * FROM category")
    LiveData<List<Category>> loadAllFoods();

    @Query("SELECT * FROM category WHERE id = :id")
    LiveData<Category> idQuery(int id);

    @Query("SELECT * FROM category")
    List<Category> loadAllFoodsAdapter();

    @Insert
    void insertFood(Category foodMenu);

    @Update
    void updateFood(Category foodMenu);

    @Delete
    void deleteFood(Category foodMenu);

    @Query("DELETE FROM category")
    void deleteAllFood();


    @Query("SELECT * FROM category WHERE id = :id")
    LiveData<Category> loadFoodById(int id);

    @Query("SELECT * FROM category WHERE name = :name")
    Category loadCategoryByName(String name);

    @Query("SELECT * FROM category WHERE category_id = :category_id")
    Category loadCategoryByCategoryID(int category_id);
}
