package com.emargystudio.myapplication.model;


import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "category")
public class Category {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int category_id;
    private String name;
    private String en_name;
    private int versionNumber;


    public Category(int id, int category_id, String name, String en_name, int versionNumber) {
        this.id = id;
        this.category_id = category_id;
        this.name = name;
        this.en_name = en_name;
        this.versionNumber = versionNumber;
    }

    @Ignore
    public Category(int category_id, String name, String en_name, int versionNumber) {
        this.category_id = category_id;
        this.name = name;
        this.en_name = en_name;
        this.versionNumber = versionNumber;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEn_name() {
        return en_name;
    }

    public void setEn_name(String en_name) {
        this.en_name = en_name;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }
}
