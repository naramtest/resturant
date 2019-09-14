package com.emargystudio.myapplication.model;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "food")
public class Food implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int food_id;
    private int category_id;
    private String name , description,image_uri ,en_name , en_description;
    private int price , versionNumber;

    public Food(int id, int food_id, int category_id, String name, String description, String image_uri, String en_name, String en_description, int price, int versionNumber) {
        this.id = id;
        this.food_id = food_id;
        this.category_id = category_id;
        this.name = name;
        this.description = description;
        this.image_uri = image_uri;
        this.en_name = en_name;
        this.en_description = en_description;
        this.price = price;
        this.versionNumber = versionNumber;
    }

    @Ignore
    public Food(int food_id, int category_id, String name, String description, String en_name, String en_description, int price, int versionNumber) {
        this.food_id = food_id;
        this.category_id = category_id;
        this.name = name;
        this.description = description;
        this.en_name = en_name;
        this.en_description = en_description;
        this.price = price;
        this.versionNumber = versionNumber;
    }

    @Ignore
    protected Food(Parcel in) {
        id = in.readInt();
        food_id = in.readInt();
        category_id = in.readInt();
        name = in.readString();
        description = in.readString();
        image_uri = in.readString();
        en_name = in.readString();
        en_description = in.readString();
        price = in.readInt();
        versionNumber = in.readInt();
    }

    public static final Creator<Food> CREATOR = new Creator<Food>() {
        @Override
        public Food createFromParcel(Parcel in) {
            return new Food(in);
        }

        @Override
        public Food[] newArray(int size) {
            return new Food[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFood_id() {
        return food_id;
    }

    public void setFood_id(int food_id) {
        this.food_id = food_id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }

    public String getEn_name() {
        return en_name;
    }

    public void setEn_name(String en_name) {
        this.en_name = en_name;
    }

    public String getEn_description() {
        return en_description;
    }

    public void setEn_description(String en_description) {
        this.en_description = en_description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(food_id);
        dest.writeInt(category_id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(image_uri);
        dest.writeString(en_name);
        dest.writeString(en_description);
        dest.writeInt(price);
        dest.writeInt(versionNumber);
    }
}
