package com.example.cinemaapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemaapp.R;
import com.example.cinemaapp.Schedule.ShowtimeHomeActivity;
import com.example.cinemaapp.api.MovieApi;
import com.example.cinemaapp.dto.MovieDetailDTO;
import com.example.cinemaapp.dto.ViewMovieDTO;
import com.example.cinemaapp.factory.GeneralResponse;
import com.example.cinemaapp.client.APIClient;
import com.example.cinemaapp.activity.MovieDetailActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieCardAdapter extends RecyclerView.Adapter<MovieCardAdapter.MovieViewHolder> {

    private final List<ViewMovieDTO> movieList;
    private final Context context;

    public MovieCardAdapter(Context context, List<ViewMovieDTO> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_card, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        ViewMovieDTO movie = movieList.get(position);
        holder.movie_name.setText(movie.getMovieName());
        Integer id = movie.getId();
        String[] timeParts = String.valueOf(movie.getDuration()).split(":");
        int totalMinutes = Integer.parseInt(timeParts[0]) * 60 + Integer.parseInt(timeParts[1]);
        holder.duration.setText(totalMinutes + " phút");
        holder.required_age.setText(movie.getRequiredAge() + "+");
        Glide.with(context).load(movie.getImageUrl()).into(holder.image_url);
        if (movie.getAvailable()) {
            holder.btnBooking.setVisibility(View.VISIBLE);
        } else {
            holder.btnBooking.setVisibility(View.GONE);
        }

        // Xử lý sự kiện nhấn nút "Đặt vé"
        holder.btnBooking.setOnClickListener(v -> {
            Intent intent = new Intent(context, ShowtimeHomeActivity.class);
            intent.putExtra("movieId", movie.getId());
            intent.putExtra("movieName", movie.getMovieName());
            intent.putExtra("movieImageUrl", movie.getImageUrl());
            intent.putExtra("movieDuration", totalMinutes);
            intent.putExtra("movieRequiredAge", movie.getRequiredAge());
            context.startActivity(intent);
            // TODO: chuyển sang màn hình đặt vé
        });

        // Xử lý sự kiện nhấn vào ảnh để mở màn hình chi tiết phim
        holder.image_url.setOnClickListener(v -> {
            // 1. Gửi tên phim đến backend
            MovieApi movieApi = APIClient.getClient().create(MovieApi.class);

            movieApi.sendMovieId(id).enqueue(new Callback<GeneralResponse<MovieDetailDTO>>() {
                @Override
                public void onResponse(Call<GeneralResponse<MovieDetailDTO>> call, Response<GeneralResponse<MovieDetailDTO>> response) {
                    MovieDetailDTO movieDetail = response.body().getData();
                    if (movieDetail != null) {
                        Log.d("MovieDetailActivity", "Genre: " + movieDetail.getGenre());
                        Log.d("MovieDetailActivity", "Description: " + movieDetail.getDescription());
                        Log.d("MovieDetailActivity", "Director: " + movieDetail.getDirector());
                        Log.d("MovieDetailActivity", "Actor: " + movieDetail.getActor());
                    }
                    // ✅ Thành công
                    Toast.makeText(context, "Đã gửi id phim tới backend " + id, Toast.LENGTH_SHORT).show();
                    // 2. Mở MovieDetailActivity
                    Intent intent = new Intent(context, MovieDetailActivity.class);
                    intent.putExtra("MOVIE_GENRE", movieDetail.getGenre() );
                    intent.putExtra("MOVIE_DESCRIPTION", movieDetail.getDescription());
                    intent.putExtra("MOVIE_DIRECTOR", movieDetail.getDirector());
                    intent.putExtra("MOVIE_ACTOR", movieDetail.getActor());
                    intent.putExtra("MOVIE_NAME", movie.getMovieName());
                    intent.putExtra("MOVIE_ID", movie.getId());
                    intent.putExtra("MOVIE_IMAGE_URL", movie.getImageUrl());
                    intent.putExtra("MOVIE_NAME", movie.getMovieName());
                    intent.putExtra("MOVIE_DURATION", totalMinutes);
                    intent.putExtra("MOVIE_REQUIRED_AGE", movie.getRequiredAge());
                    intent.putExtra("MOVIE_AVAILABLE", movie.getAvailable());
                    context.startActivity(intent);
                }

                @Override
                public void onFailure(Call<GeneralResponse<MovieDetailDTO>> call, Throwable t) {
                    // ❌ Thất bại
                    Toast.makeText(context, "Gửi thất bại: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                    // Vẫn mở activity dù gửi lỗi
                    Intent intent = new Intent(context, MovieDetailActivity.class);
                    intent.putExtra("MOVIE_ID", movie.getMovieName());
                    intent.putExtra("MOVIE_IMAGE_URL", movie.getImageUrl());
                    intent.putExtra("MOVIE_NAME", movie.getMovieName());
                    intent.putExtra("MOVIE_DURATION", totalMinutes);
                    intent.putExtra("MOVIE_REQUIRED_AGE", movie.getRequiredAge());
                    intent.putExtra("MOVIE_AVAILABLE", movie.getAvailable());
                    context.startActivity(intent);
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView image_url;
        TextView movie_name, duration, required_age;
        Button btnBooking;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            image_url = itemView.findViewById(R.id.image_url);
            movie_name = itemView.findViewById(R.id.movie_name);
            duration = itemView.findViewById(R.id.duration);
            required_age = itemView.findViewById(R.id.required_age);
            btnBooking = itemView.findViewById(R.id.btnBooking);
        }
    }
}