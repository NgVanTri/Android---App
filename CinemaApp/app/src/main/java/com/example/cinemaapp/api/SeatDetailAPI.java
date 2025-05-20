package com.example.cinemaapp.api;

import com.example.cinemaapp.dto.SeatDetailDTO;
import com.example.cinemaapp.factory.GeneralResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SeatDetailAPI {
    @GET("/api/seat-detail/get-all-seat")
    Call<GeneralResponse<List<SeatDetailDTO>>> getAllSeats(@Query("showtimeId") Integer showtimeId);

    @GET("/api/seat-detail/hold")
    Call<GeneralResponse<String>> holdSeat(@Query("showtimeId") Integer seatDetailId, @Query("seatName") String seatName);
}
