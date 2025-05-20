package com.example.cinemaapp.Schedule;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemaapp.R;
import com.example.cinemaapp.api.ShowtimeAPI;
import com.example.cinemaapp.client.APIClient;
import com.example.cinemaapp.dto.MovieShowDTO;
import com.example.cinemaapp.dto.ShowtimeDTO;
import com.example.cinemaapp.factory.GeneralResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RequiresApi(api = Build.VERSION_CODES.O)
public class LichChieuActivity extends AppCompatActivity {

    ImageView rapCoverImageView;
    TextView tenRapTextView;
    TextView diaChiRapTextView;
    LinearLayout ngayChieuLinearLayout;
    RecyclerView lichChieuRecyclerView;
    LichChieuPhimAdapter lichChieuAdapter;
    List<LichChieuPhimAdapter.LichChieuPhimItem> lichChieuList;
    LocalDate selectedDate = LocalDate.now();
    List<TextView> dayTextViews = new ArrayList<>();
    DateTimeFormatter dateFormatterForIntent = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter dateFormatterForBackend = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    ImageView btnBackLichChieu;
    String tenRap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich_chieu);

        rapCoverImageView = findViewById(R.id.rapCoverImageView);
        tenRapTextView = findViewById(R.id.tenRapTextView);
        diaChiRapTextView = findViewById(R.id.diaChiRapTextView);
        ngayChieuLinearLayout = findViewById(R.id.ngayChieuLinearLayout);
        lichChieuRecyclerView = findViewById(R.id.lichChieuRecyclerView);
        lichChieuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnBackLichChieu = findViewById(R.id.btnBackLichChieu);
        btnBackLichChieu.setOnClickListener(v -> finish());

        // Lấy thông tin rạp từ Intent
        Intent intent = getIntent();
        tenRap = intent.getStringExtra("tenRap");
        String diaChiRap = intent.getStringExtra("diaChiRap");
        int anhBiaRap = intent.getIntExtra("anhBiaRap", R.drawable.sample_rap);

        tenRapTextView.setText(tenRap);
        diaChiRapTextView.setText(diaChiRap);
        rapCoverImageView.setImageResource(anhBiaRap);

        // Khởi tạo danh sách lịch chiếu
        lichChieuList = new ArrayList<>();
        lichChieuAdapter = new LichChieuPhimAdapter(this, lichChieuList, tenRap, dateFormatterForIntent.format(selectedDate));
        lichChieuRecyclerView.setAdapter(lichChieuAdapter);

        // Tạo và hiển thị các ô ngày
        populateNgayChieu();

        // Lấy lịch chiếu cho ngày hiện tại
        fetchLichChieu(selectedDate);
    }

    private void populateNgayChieu() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd");
        DateTimeFormatter dayOfWeekFormatter = DateTimeFormatter.ofPattern("E");

        for (int i = 0; i < 7; i++) {
            LocalDate date = currentDate.plusDays(i);
            View dayView = LayoutInflater.from(this).inflate(R.layout.item_ngay_chieu, ngayChieuLinearLayout, false);
            TextView ngayTextView = dayView.findViewById(R.id.ngayTextView);
            ngayTextView.setText(String.format("%s\n%s", dayFormatter.format(date), dayOfWeekFormatter.format(date)));
            dayTextViews.add(ngayTextView);

            final LocalDate finalDate = date;
            ngayTextView.setOnClickListener(v -> {
                selectedDate = finalDate;
                updateSelectedDay();
                fetchLichChieu(finalDate);
            });

            ngayChieuLinearLayout.addView(dayView);
        }
        updateSelectedDay();
    }

    private void fetchLichChieu(LocalDate showDate) {
        ShowtimeAPI showtimeAPI = APIClient.getClient().create(ShowtimeAPI.class);
        String formattedDate = dateFormatterForBackend.format(showDate);

        Log.d("LichChieu", "Fetching showtimes for date: " + formattedDate);
        showtimeAPI.getMovieShowtimes(LocalDate.parse(formattedDate)).enqueue(new Callback<GeneralResponse<List<MovieShowDTO>>>() {
            @Override
            public void onResponse(Call<GeneralResponse<List<MovieShowDTO>>> call, Response<GeneralResponse<List<MovieShowDTO>>> response) {
                Log.d("LichChieu", "API response: " + (response.isSuccessful() ? "Success" : "Failed, code: " + response.code()));
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<MovieShowDTO> movieShowDTOs = response.body().getData();
                    Log.d("LichChieu", "Movies received: " + movieShowDTOs.get(0));
                    updateLichChieuList(movieShowDTOs);
                } else {
                    Log.d("LichChieu", "No data or response failed");
                    Toast.makeText(LichChieuActivity.this, "Không có lịch chiếu cho ngày này", Toast.LENGTH_SHORT).show();
                    updateLichChieuList(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<GeneralResponse<List<MovieShowDTO>>> call, Throwable t) {
                Log.e("LichChieu", "API failure: " + t.getMessage());
                Toast.makeText(LichChieuActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateLichChieuList(new ArrayList<>());
            }
        });
    }

    private void updateLichChieuList(List<MovieShowDTO> movieShowDTOs) {
        lichChieuList.clear();
        Log.d("LichChieu", "Updating lichChieuList with " + movieShowDTOs.size() + " movies");
        for (MovieShowDTO movieShow : movieShowDTOs) {
            List<String> gioChieuList = new ArrayList<>();
            if (movieShow.getShowtimes() != null) {
                for (ShowtimeDTO showtime : movieShow.getShowtimes()) {
                    String startTime = showtime.getStartTime();
                    gioChieuList.add(startTime);
                }
            }
            Log.d("LichChieu", "Movie: " + (movieShow.getMovieName() != null ? movieShow.getMovieName() : "null") + ", Showtimes: " + gioChieuList.size());
            lichChieuList.add(new LichChieuPhimAdapter.LichChieuPhimItem(
                    movieShow.getId(),
                    movieShow.getShowtimes(),
                    movieShow.getImageUrl(),
                    movieShow.getMovieName() != null ? movieShow.getMovieName() : "",
                    gioChieuList
            ));
        }
        lichChieuAdapter.setLichChieuList(lichChieuList);
        lichChieuAdapter.notifyDataSetChanged();
    }

    private void updateSelectedDay() {
        for (int i = 0; i < dayTextViews.size(); i++) {
            TextView dayTextView = dayTextViews.get(i);
            LocalDate date = LocalDate.now().plusDays(i);
            if (date.isEqual(selectedDate)) {
                dayTextView.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
            } else {
                dayTextView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            }
        }
    }


}