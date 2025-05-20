package com.example.cinemaapp.model;

import java.io.Serializable;

public class Food implements Serializable {

    private Integer id;
    private String foodName;
    private Float price;
    private String description;

    public Food(Integer id, String foodName, Float price, String description) {
        this.id = id;
        this.foodName = foodName;
        this.price = price;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
