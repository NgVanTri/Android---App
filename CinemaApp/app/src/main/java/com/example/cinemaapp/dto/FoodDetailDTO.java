package com.example.cinemaapp.dto;

public class FoodDetailDTO {
    private Integer foodId;
    private Integer quantity;

    public FoodDetailDTO(Integer foodId, Integer quantity) {
        this.foodId = foodId;
        this.quantity = quantity;
    }

    public Integer getFoodId() {
        return foodId;
    }

    public void setFooId(Integer fooId) {
        this.foodId = fooId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
