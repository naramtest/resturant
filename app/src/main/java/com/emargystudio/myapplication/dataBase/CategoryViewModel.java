package com.emargystudio.myapplication.dataBase;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.emargystudio.myapplication.model.Category;

import java.util.List;

public class CategoryViewModel extends AndroidViewModel {

    private LiveData<List<Category>> categories;
    private LiveData<Category> categoryByID;
    AppDatabase database;

    public CategoryViewModel(Application application) {
        super(application);
        database = AppDatabase.getInstance(this.getApplication());
        categories = database.categoryDao().loadAllFoods();
    }

    public LiveData<List<Category>> getTasks() {
        return categories;
    }

    public LiveData<Category> getCategoryByID(int id){
        categoryByID = database.categoryDao().idQuery(id);
        return categoryByID;
    }
}
