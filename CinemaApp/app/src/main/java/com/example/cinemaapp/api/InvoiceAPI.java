package com.example.cinemaapp.api;

import com.example.cinemaapp.dto.FoodDetailDTO;
import com.example.cinemaapp.dto.response.InvoiceResponse;
import com.example.cinemaapp.factory.GeneralResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface InvoiceAPI {
    @POST("/api/invoice/create")
    Call<GeneralResponse<InvoiceResponse>> createInvoice(@Body List<FoodDetailDTO> foodDetailDTOList);

    @GET("/api/invoice/save")
    Call<GeneralResponse<String>> saveInvoice();
}
