package com.example.cinemaapp.api;

import com.example.cinemaapp.factory.GeneralResponse;
import com.example.cinemaapp.model.Food;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.Call;

public interface FoodAPI {
    @GET("api/food/getAll")
    Call<GeneralResponse<List<Food>>> getAllFood();
}
