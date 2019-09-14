package com.emargystudio.myapplication.dataBase;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.emargystudio.myapplication.model.Food;

import java.util.List;

public class FoodViewModel extends AndroidViewModel {



    private LiveData<List<Food>> foods;
    private LiveData<List<Food>> foodByCategory;
    AppDatabase database;

    public FoodViewModel(Application application) {
        super(application);
        database = AppDatabase.getInstance(this.getApplication());
        foods = database.foodDao().loadAllFoods();

    }

    public LiveData<List<Food>> getTasks() {
        return foods;
    }

    public LiveData<List<Food>> getFood(int id) {
        foodByCategory = database.foodDao().idQuery(id);
        return foodByCategory;
    }
}
