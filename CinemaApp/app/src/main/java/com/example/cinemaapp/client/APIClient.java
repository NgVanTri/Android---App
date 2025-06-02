package com.example.cinemaapp.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.cinemaapp.client.MyApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
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

            // Tạo CookieJar tùy chỉnh để quản lý cookie
            CookieJar cookieJar = new CookieJar() {
                private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    cookieStore.put(url.host(), cookies);
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    List<Cookie> cookies = cookieStore.get(url.host());
                    return cookies != null ? cookies : new ArrayList<>();
                }
            };

            // Tạo OkHttpClient với CookieJar và interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(cookieJar) // Sử dụng CookieJar tùy chỉnh
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder();

                        SharedPreferences prefs = MyApplication.getAppContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
                        String token = prefs.getString("jwt_token", null);
                        if (token != null) {
                            requestBuilder.header("Authorization", "Bearer " + token);
                        }

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    })
                    .addInterceptor(logging)
                    .build();

            // Khởi tạo Retrofit với base URL và client đã cấu hình
            retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.1.15:8080/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}