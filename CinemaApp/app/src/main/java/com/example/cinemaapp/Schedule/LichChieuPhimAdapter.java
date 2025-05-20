package com.example.cinemaapp.Schedule;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemaapp.R;
import com.example.cinemaapp.api.SeatDetailAPI;
import com.example.cinemaapp.client.APIClient;
import com.example.cinemaapp.dto.SeatDetailDTO;
import com.example.cinemaapp.dto.SeatObjectDTO;
import com.example.cinemaapp.dto.ShowtimeDTO;
import com.example.cinemaapp.factory.GeneralResponse;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LichChieuPhimAdapter extends RecyclerView.Adapter<LichChieuPhimAdapter.ViewHolder> {
    private List<LichChieuPhimItem> lichChieuList;

    private Context context;
    private String tenRap;
    private String ngayChieu;

    private final SeatObjectDTO seatObjectDTO = new SeatObjectDTO();
    private List<SeatDetailDTO> seatDetailList;

    public LichChieuPhimAdapter(Context context, List<LichChieuPhimItem> lichChieuList, String tenRap, String ngayChieu) {
        this.lichChieuList = lichChieuList;
        this.context = context;
        this.tenRap = tenRap;
        this.ngayChieu = ngayChieu;
    }

    public void setLichChieuList(List<LichChieuPhimItem> newList) {
        this.lichChieuList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lich_chieu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LichChieuPhimItem item = lichChieuList.get(position);
        holder.tenPhimTextView.setText(item.getTenPhim());
        holder.layoutGioChieu.removeAllViews();

        for (ShowtimeDTO showtimeDTO1 : item.getShowtimeDTOList()) {
            String gioChieu = showtimeDTO1.getStartTime();
            Button btnGio = new Button(context);
            String gioChieuFormat = gioChieu.substring(0, 5);

            btnGio.setText(gioChieuFormat);
            btnGio.setTextSize(14);
            btnGio.setTextColor(context.getResources().getColor(android.R.color.white));
            btnGio.setBackgroundResource(R.drawable.selector_gio_chieu);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 8, 0);
            holder.layoutGioChieu.addView(btnGio, params);

            btnGio.setOnClickListener(v -> {

                SeatDetailAPI apiService = APIClient.getClient().create(SeatDetailAPI.class);
                Call<GeneralResponse<List<SeatDetailDTO>>> call = apiService.getAllSeats(showtimeDTO1.getId());
                call.enqueue(new Callback<GeneralResponse<List<SeatDetailDTO>>>() {
                     @Override
                     public void onResponse(@NonNull Call<GeneralResponse<List<SeatDetailDTO>>> call, @NonNull Response<GeneralResponse<List<SeatDetailDTO>>> response) {
                         Log.d("response", String.valueOf(response.body().getData()));
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("response", response.toString());
                            seatDetailList = response.body().getData();
                            SeatObjectDTO seatObjectDTO = new SeatObjectDTO(seatDetailList);
                            Log.d("list size:", String.valueOf(seatDetailList.size()));
                            Log.d("list size object:", String.valueOf(seatObjectDTO.getSeatDetailDTOList().size()));
                            Intent intent = new Intent(context, ChonGheActivity.class);
                            intent.putExtra("ChiTietGhe", seatObjectDTO);
                            intent.putExtra("idPhim", item.getId());
                            intent.putExtra("idSuatChieu",showtimeDTO1.getId());
                            intent.putExtra("imageUrl", item.getImageUrl());
                            Log.d("imageUrl:",  item.getImageUrl());
                            intent.putExtra("tenPhim", item.getTenPhim());
                            intent.putExtra("gioChieu", gioChieu.substring(0, 5));
                            intent.putExtra("tenRap", tenRap);
                            intent.putExtra("ngayChieu", ngayChieu);
                            context.startActivity(intent);
                        }
                     }

                     @Override
                     public void onFailure(Call<GeneralResponse<List<SeatDetailDTO>>> call, Throwable t) {
                         Log.d("Loi: ", t.getMessage());
                         Toast.makeText(context, "Lỗi khi lấy danh sách ghe", Toast.LENGTH_SHORT).show();
                     }
                 });



            });
        }
    }

    @Override
    public int getItemCount() {
        return lichChieuList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tenPhimTextView;
        LinearLayout layoutGioChieu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tenPhimTextView = itemView.findViewById(R.id.tenPhimTextView);
            layoutGioChieu = itemView.findViewById(R.id.layoutGioChieu);
        }
    }

    public static class LichChieuPhimItem {
        private Integer id;

        private List<ShowtimeDTO> showtimeDTOList;
        private String imageUrl;
        private String tenPhim;
        private List<String> gioChieu;

        public LichChieuPhimItem(Integer id, List<ShowtimeDTO> showtimeDTOList, String imageUrl, String tenPhim, List<String> gioChieu) {
            this.id = id;
            this.showtimeDTOList = showtimeDTOList;
            this.imageUrl = imageUrl;
            this.tenPhim = tenPhim;
            this.gioChieu = gioChieu;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public List<ShowtimeDTO> getShowtimeDTOList() {
            return showtimeDTOList;
        }

        public void setShowtimeDTOList(List<ShowtimeDTO> showtimeDTOList) {
            this.showtimeDTOList = showtimeDTOList;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getTenPhim() {
            return tenPhim;
        }

        public void setTenPhim(String tenPhim) {
            this.tenPhim = tenPhim;
        }

        public List<String> getGioChieu() {
            return gioChieu;
        }

        public void setGioChieu(List<String> gioChieu) {
            this.gioChieu = gioChieu;
        }
    }
}