package com.example.cinemaapp.client;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.cinemaapp.client.MyApplication;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Tạo interceptor để log HTTP request/response
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Tạo OkHttpClient với interceptor thêm Authorization header nếu có token
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder();

                        // Lấy token từ SharedPreferences
                        SharedPreferences prefs = MyApplication.getAppContext()
                                .getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
                        String token = prefs.getString("jwt_token", null);

                        // Gắn header nếu có token
                        if (token != null) {
                            requestBuilder.header("Authorization", "Bearer " + token);
                        }

                        return chain.proceed(requestBuilder.build());
                    })
                    .addInterceptor(logging)
                    .build();

            // Khởi tạo Retrofit với base URL và client đã cấu hình
            retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.1.201:8080/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
