package com.example.cinemaapp.Schedule;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.cinemaapp.R;
import com.example.cinemaapp.api.FoodAPI;
import com.example.cinemaapp.api.SeatDetailAPI; // Thêm import này
import com.example.cinemaapp.client.APIClient;
import com.example.cinemaapp.dto.SeatDetailDTO;
import com.example.cinemaapp.dto.SeatObjectDTO;
import com.example.cinemaapp.factory.GeneralResponse;
import com.example.cinemaapp.model.Food;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChonGheActivity extends AppCompatActivity {

    private TextView tvTimerChonGhe;
    private TextView tvTenRapChonGhe;
    private TextView tvThongTinChieuChonGhe;
    private LinearLayout layoutHangGhe;
    private TextView tvTenPhimChonGhe;
    private TextView tvSoLuongGheChon;
    private TextView tvTongTienChonGhe;
    private TextView tvGiaTienChonGhe;
    private Button btnHoanTatChonGhe;
    private ImageView btnBackChonGhe;

    private String tenPhim;
    private Integer IdPhim;

    private Integer IdSuatChieu;
    private String imageUrl;
    private String gioChieu;
    private String tenRap;
    private String tenPhong;
    private String ngayChieu;
    private List<SeatDetailDTO> seatDetailList;
    private SeatObjectDTO seatObjectDTO;
    private Map<String, String> seatStatusMap = new HashMap<>();

    private List<String> gheDaChon = new ArrayList<>();
    private int soVe = 0;
    private int tongTien = 0;
    private final int giaVe = 45000;
    private TimerService timerService;
    private boolean isServiceBound = false;
    private String hangGheDoi = "J";

    private final BroadcastReceiver timerTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TimerService.ACTION_TIMER_TICK.equals(intent.getAction())) {
                long remainingTime = intent.getLongExtra(TimerService.EXTRA_TIME_REMAINING, 0);
                tvTimerChonGhe.setText(timerService.formatTime(remainingTime / 1000));
            } else if (TimerService.ACTION_TIMER_FINISHED.equals(intent.getAction())) {
                tvTimerChonGhe.setText("00:00");
                Toast.makeText(ChonGheActivity.this, "Đã hết thời gian đặt vé", Toast.LENGTH_LONG).show();
                new android.os.Handler().postDelayed(() -> finish(), 3000);
            }
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
            timerService = binder.getService();
            isServiceBound = true;
            tvTimerChonGhe.setText(timerService.formatTime(timerService.getRemainingTime() / 1000));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            timerService = null;
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chon_ghe);

        // Ánh xạ các view
        Toolbar toolbarChonGhe = findViewById(R.id.toolbarChonGhe);
        btnBackChonGhe = findViewById(R.id.btnBackChonGhe);
        tvTimerChonGhe = findViewById(R.id.tvTimerChonGhe);
        tvTenRapChonGhe = findViewById(R.id.tvTenRapChonGhe);
        tvThongTinChieuChonGhe = findViewById(R.id.tvThongTinChieuChonGhe);
        layoutHangGhe = findViewById(R.id.layoutHangGhe);
        tvTenPhimChonGhe = findViewById(R.id.tvTenPhimChonGhe);
        tvSoLuongGheChon = findViewById(R.id.tvSoLuongGheChon);
        tvTongTienChonGhe = findViewById(R.id.tvTongTienChonGhe);
        tvGiaTienChonGhe = findViewById(R.id.tvGiaTienChonGhe);
        btnHoanTatChonGhe = findViewById(R.id.btnHoanTatChonGhe);

        // Nhận thông tin từ Intent
        Bundle intent = getIntent().getExtras();
        if (intent != null) {
            seatObjectDTO = (SeatObjectDTO) intent.getSerializable("ChiTietGhe");
            if (seatObjectDTO != null) {
                seatDetailList = seatObjectDTO.getSeatDetailDTOList();
                if (seatDetailList == null) {
                    seatDetailList = new ArrayList<>();
                    Log.e("ChonGheActivity", "seatDetailList is null in seatObjectDTO");
                }
            } else {
                seatDetailList = new ArrayList<>();
                Log.e("ChonGheActivity", "seatObjectDTO is null");
                Toast.makeText(this, "Không có dữ liệu ghế", Toast.LENGTH_SHORT).show();
            }

            tenPhim = intent.getString("tenPhim");
            IdPhim = intent.getInt("idPhim");
            IdSuatChieu = intent.getInt("idSuatChieu", 0);
            imageUrl = intent.getString("imageUrl");
            gioChieu = intent.getString("gioChieu");
            tenRap = intent.getString("tenRap");
            ngayChieu = intent.getString("ngayChieu");
            tenPhong = "Screen 4";
            tvTenPhimChonGhe.setText(tenPhim);
            tvTenRapChonGhe.setText(tenRap);
            tvThongTinChieuChonGhe.setText(String.format("%s - %s %s ~ %s", tenPhong, ngayChieu, gioChieu, calculateEndTime(gioChieu)));

            // Populate seatStatusMap từ seatDetailList
            for (SeatDetailDTO seat : seatDetailList) {
                seatStatusMap.put(seat.getSeatName(), seat.getStatus());
            }
        } else {
            Log.e("ChonGheActivity", "Intent is null");
            seatDetailList = new ArrayList<>();
            Toast.makeText(this, "Không tải được activity", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tạo layout ghế
        createSeatLayout();

        // Bắt đầu và bind TimerService
        Intent serviceIntent = new Intent(this, TimerService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // Đăng ký BroadcastReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(timerTickReceiver,
                new IntentFilter(TimerService.ACTION_TIMER_TICK));
        LocalBroadcastManager.getInstance(this).registerReceiver(timerTickReceiver,
                new IntentFilter(TimerService.ACTION_TIMER_FINISHED));

        // Xử lý nút quay lại
        btnBackChonGhe.setOnClickListener(v -> finish());

        // Xử lý nút hoàn tất chọn ghế
        btnHoanTatChonGhe.setOnClickListener(v -> {
            if (!gheDaChon.isEmpty()) {
                FoodAPI apiService = APIClient.getClient().create(FoodAPI.class);
                Call<GeneralResponse<List<Food>>> call = apiService.getAllFood();

                call.enqueue(new Callback<GeneralResponse<List<Food>>>() {
                    @Override
                    public void onResponse(Call<GeneralResponse<List<Food>>> call, Response<GeneralResponse<List<Food>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ArrayList<Food> foodList = new ArrayList<>(response.body().getData());

                            Intent intentToDatDoAn = new Intent(ChonGheActivity.this, DatDoAnActivity.class);
                            intentToDatDoAn.putExtra("tenPhim", tenPhim);
                            intentToDatDoAn.putExtra("idPhim", IdPhim);
                            intentToDatDoAn.putExtra("imageUrl", imageUrl);

                            intentToDatDoAn.putExtra("soLuongVe", soVe);
                            intentToDatDoAn.putExtra("viTriGhe", String.join(", ", gheDaChon));
                            intentToDatDoAn.putExtra("giaVe", giaVe);
                            intentToDatDoAn.putExtra("tenRap", tenRap);
                            intentToDatDoAn.putExtra("ngayChieu", ngayChieu);
                            intentToDatDoAn.putExtra("gioChieu", gioChieu);
                            intentToDatDoAn.putExtra("tongTienVe", tongTien);
                            intentToDatDoAn.putExtra("foodList", foodList);
                            Log.d("DEBUG_GHE", "→ ChonGhe gửi: SL=" + gheDaChon.size() + " | viTri=" + String.join(", ", gheDaChon));

                            startActivity(intentToDatDoAn);
                        } else {
                            Toast.makeText(ChonGheActivity.this, "Lỗi khi lấy danh sách món ăn", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GeneralResponse<List<Food>>> call, @NonNull Throwable t) {
                        Toast.makeText(ChonGheActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ChonGheActivity.this, "Vui lòng chọn ghế.", Toast.LENGTH_SHORT).show();
            }
        });

        // Cập nhật thông tin thanh toán sau khi chọn ghế
        updateThongTinThanhToan();
    }

    private String calculateEndTime(String startTime) {
        String[] parts = startTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        minute += 120;
        hour += minute / 60;
        minute %= 60;
        hour %= 24;
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
    }

    private void createSeatLayout() {
        char[] hangChars = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
        int[] soGheGiua = {6, 6, 6, 6, 6, 6, 6, 6, 6, 0}; // Hàng J không có ghế giữa
        int[] soGheBienA_I = {4, 4, 4, 4, 4, 4, 4, 4, 3};
        int soGheDoiJ = 7; // 7 ghế đôi độc lập

        for (int i = 0; i < hangChars.length; i++) {
            char hang = hangChars[i];
            LinearLayout hangWrapperLayout = new LinearLayout(this);
            hangWrapperLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            hangWrapperLayout.setOrientation(LinearLayout.HORIZONTAL);
            hangWrapperLayout.setGravity(Gravity.CENTER_VERTICAL);

            TextView hangLabel = createHangLabel(hang);
            hangWrapperLayout.addView(hangLabel);

            LinearLayout hangLayout = new LinearLayout(this);
            hangLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            hangLayout.setOrientation(LinearLayout.HORIZONTAL);
            hangLayout.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams seatParams = new LinearLayout.LayoutParams(40, 40);
            seatParams.setMargins(8, 8, 8, 8);
            LinearLayout.LayoutParams seatDoiParams = new LinearLayout.LayoutParams(80, 40); // Kích thước cho ghế đôi
            seatDoiParams.setMargins(8, 8, 8, 8);

            if (String.valueOf(hang).equals(hangGheDoi)) {
                // Xử lý ghế đôi hàng J (7 ghế đôi độc lập từ J1 đến J7)
                for (int k = 1; k <= soGheDoiJ; k++) {
                    String seatName = hang + String.valueOf(k);
                    String status = seatStatusMap.getOrDefault(seatName, "AVAILABLE");

                    boolean isBooked = status.equals("BOOKED");
                    boolean isHoldByCurrentUser = status.equals("HOLD_BY_CURRENT_USER");
                    boolean isHoldByAnotherUser = status.equals("HOLD_BY_ANOTHER_USER");

                    ImageView seat = createSeat(hang, k, "Couple", true, isBooked, isHoldByCurrentUser, isHoldByAnotherUser);
                    seat.setTag(seatName + "-Couple-" +
                            (isBooked ? "booked" : isHoldByCurrentUser ? "selected" : isHoldByAnotherUser ? "hold_by_another" : "available"));
                    hangLayout.addView(seat, seatDoiParams);

                    if (isHoldByCurrentUser && !gheDaChon.contains(seatName)) {
                        gheDaChon.add(seatName);
                        soVe += 1; // Mỗi ghế đôi tính 1 vé
                    }
                }
            } else {
                int soGheBien = soGheBienA_I[i];
                // Ghế bên trái
                for (int j = 1; j <= soGheBien; j++) {
                    String seatName = hang + String.valueOf(j);
                    String status = seatStatusMap.getOrDefault(seatName, "AVAILABLE");

                    boolean isBooked = status.equals("BOOKED");
                    boolean isHoldByCurrentUser = status.equals("HOLD_BY_CURRENT_USER");
                    boolean isHoldByAnotherUser = status.equals("HOLD_BY_ANOTHER_USER");

                    ImageView seat = createSeat(hang, j, "Stand", false, isBooked, isHoldByCurrentUser, isHoldByAnotherUser);
                    seat.setTag(seatName + "-Stand-" +
                            (isBooked ? "booked" : isHoldByCurrentUser ? "selected" : isHoldByAnotherUser ? "hold_by_another" : "available"));
                    hangLayout.addView(seat, seatParams);

                    if (isHoldByCurrentUser && !gheDaChon.contains(seatName)) {
                        gheDaChon.add(seatName);
                        soVe += 1;
                    }
                }

                if (soGheBien > 0 && soGheGiua[i] > 0) {
                    View spaceLeft = new View(this);
                    spaceLeft.setLayoutParams(new LinearLayout.LayoutParams(20, 1));
                    hangLayout.addView(spaceLeft);
                }

                // Ghế giữa
                for (int k = 1; k <= soGheGiua[i]; k++) {
                    String loaiGhe = (i < 3) ? "Stand" : (i < 9) ? "VIP" : "Couple";
                    boolean isDoi = loaiGhe.equals("Couple") && !String.valueOf(hang).equals(hangGheDoi);
                    int viTriGhe = (soGheBien > 0 ? soGheBien + k : k);
                    String seatName = hang + String.valueOf(viTriGhe);
                    String status = seatStatusMap.getOrDefault(seatName, "AVAILABLE");

                    boolean isBooked = status.equals("BOOKED");
                    boolean isHoldByCurrentUser = status.equals("HOLD_BY_CURRENT_USER");
                    boolean isHoldByAnotherUser = status.equals("HOLD_BY_ANOTHER_USER");

                    ImageView seat = createSeat(hang, viTriGhe, loaiGhe, isDoi, isBooked, isHoldByCurrentUser, isHoldByAnotherUser);
                    seat.setTag(seatName + "-" + loaiGhe + "-" +
                            (isBooked ? "booked" : isHoldByCurrentUser ? "selected" : isHoldByAnotherUser ? "hold_by_another" : "available"));
                    hangLayout.addView(seat, isDoi ? seatDoiParams : seatParams);

                    if (isHoldByCurrentUser && !gheDaChon.contains(seatName)) {
                        gheDaChon.add(seatName);
                        soVe += 1;
                    }

                    if (isDoi && k < soGheGiua[i]) {
                        k++;
                        viTriGhe = (soGheBien > 0 ? soGheBien + k : k);
                        seatName = hang + String.valueOf(viTriGhe);
                        status = seatStatusMap.getOrDefault(seatName, "AVAILABLE");

                        isBooked = status.equals("BOOKED");
                        isHoldByCurrentUser = status.equals("HOLD_BY_CURRENT_USER");
                        isHoldByAnotherUser = status.equals("HOLD_BY_ANOTHER_USER");

                        seat = createSeat(hang, viTriGhe, loaiGhe, isDoi, isBooked, isHoldByCurrentUser, isHoldByAnotherUser);
                        seat.setTag(seatName + "-" + loaiGhe + "-" +
                                (isBooked ? "booked" : isHoldByCurrentUser ? "selected" : isHoldByAnotherUser ? "hold_by_another" : "available"));
                        hangLayout.addView(seat, seatDoiParams);

                        if (isHoldByCurrentUser && !gheDaChon.contains(seatName)) {
                            gheDaChon.add(seatName);
                            soVe += 1;
                        }
                    }
                }

                if (soGheBien > 0 && soGheGiua[i] > 0) {
                    View spaceRight = new View(this);
                    spaceRight.setLayoutParams(new LinearLayout.LayoutParams(20, 1));
                    hangLayout.addView(spaceRight);
                }

                // Ghế bên phải
                for (int l = 1; l <= soGheBien; l++) {
                    int viTriGhe = (soGheBien > 0 ? soGheBien + soGheGiua[i] + l : soGheGiua[i] + l);
                    String seatName = hang + String.valueOf(viTriGhe);
                    String status = seatStatusMap.getOrDefault(seatName, "AVAILABLE");

                    boolean isBooked = status.equals("BOOKED");
                    boolean isHoldByCurrentUser = status.equals("HOLD_BY_CURRENT_USER");
                    boolean isHoldByAnotherUser = status.equals("HOLD_BY_ANOTHER_USER");

                    ImageView seat = createSeat(hang, viTriGhe, "Stand", false, isBooked, isHoldByCurrentUser, isHoldByAnotherUser);
                    seat.setTag(seatName + "-Stand-" +
                            (isBooked ? "booked" : isHoldByCurrentUser ? "selected" : isHoldByAnotherUser ? "hold_by_another" : "available"));
                    hangLayout.addView(seat, seatParams);

                    if (isHoldByCurrentUser && !gheDaChon.contains(seatName)) {
                        gheDaChon.add(seatName);
                        soVe += 1;
                    }
                }
            }

            hangWrapperLayout.addView(hangLayout);
            layoutHangGhe.addView(hangWrapperLayout);
        }
    }

    private TextView createHangLabel(char hang) {
        TextView label = new TextView(this);
        label.setText(String.valueOf(hang));
        label.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(0, 8, 16, 8);
        label.setLayoutParams(params);
        return label;
    }

    private ImageView createSeat(char hang, int soThuTu, String loaiGhe, boolean isDoi, boolean isBooked, boolean isHoldByCurrentUser, boolean isHoldByAnotherUser) {
        ImageView seatView = new ImageView(this);
        float density = getResources().getDisplayMetrics().density;
        int widthPx = (int) (isDoi ? 80 * density : 40 * density); // Kích thước ghế đôi lớn hơn
        int heightPx = (int) (60 * density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthPx, heightPx);
        params.setMargins(4, 4, 4, 4);
        seatView.setLayoutParams(params);

        AtomicInteger iconResId = new AtomicInteger();
        if (isBooked) {
            iconResId.set(R.drawable.ic_seat_sold);
        } else if (isHoldByCurrentUser) {
            iconResId.set(R.drawable.ic_seat_selected);
        } else if (isHoldByAnotherUser) {
            iconResId.set(R.drawable.ic_seat_hold_by_another);
        } else {
            if (loaiGhe.equals("Stand")) {
                iconResId.set(R.drawable.ic_seat_stand);
            } else if (loaiGhe.equals("VIP")) {
                iconResId.set(R.drawable.ic_seat_vip);
            } else {
                iconResId.set(R.drawable.ic_seat_couple);
            }
        }
        seatView.setImageResource(iconResId.get());

        String maGhe = hang + String.valueOf(soThuTu);
        seatView.setTag(maGhe + "-" + loaiGhe + "-" +
                (isBooked ? "booked" : isHoldByCurrentUser ? "selected" : isHoldByAnotherUser ? "hold_by_another" : "available"));

        seatView.setOnClickListener(v -> {
            String tag = (String) v.getTag();
            String[] parts = tag.split("-");
            String maGheTag = parts[0];
            String loaiGheHienTai = parts[1];
            String status = parts[2];
            boolean isCurrentlyBooked = status.equals("booked");
            boolean isCurrentlySelected = status.equals("selected");
            boolean isCurrentlyHoldByAnother = status.equals("hold_by_another");

            if (isCurrentlyBooked) {
                Toast.makeText(ChonGheActivity.this, "Ghế này đã được đặt", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isCurrentlyHoldByAnother) {
                Toast.makeText(ChonGheActivity.this, "Ghế này đang được giữ bởi người dùng khác", Toast.LENGTH_SHORT).show();
                return;
            }

            SeatDetailAPI apiService = APIClient.getClient().create(SeatDetailAPI.class);
            Call<GeneralResponse<String>> call = apiService.holdSeat(IdSuatChieu, maGheTag);

            call.enqueue(new Callback<GeneralResponse<String>>() {
                @Override
                public void onResponse(Call<GeneralResponse<String>> call, Response<GeneralResponse<String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (!isCurrentlySelected) {
                            gheDaChon.add(maGheTag);
                            soVe += 1;
                            ((ImageView) v).setImageResource(R.drawable.ic_seat_selected);
                            v.setTag(maGheTag + "-" + loaiGheHienTai + "-" + "selected");
                            seatStatusMap.put(maGheTag, "HOLD_BY_CURRENT_USER");
                        } else {
                            gheDaChon.remove(maGheTag);
                            soVe -= 1;
                            if (loaiGhe.equals("Stand")) {
                                iconResId.set(R.drawable.ic_seat_stand);
                            } else if (loaiGhe.equals("VIP")) {
                                iconResId.set(R.drawable.ic_seat_vip);
                            } else {
                                iconResId.set(R.drawable.ic_seat_couple);
                            }
                            ((ImageView) v).setImageResource(iconResId.get());
                            v.setTag(maGheTag + "-" + loaiGheHienTai + "-" + "available");
                            seatStatusMap.put(maGheTag, "AVAILABLE"); // Giả định bỏ giữ ghế khi bỏ chọn
                        }
                        updateThongTinThanhToan();
                    } else {
                        Toast.makeText(ChonGheActivity.this, "Không thể giữ ghế: " + (response.body() != null ? response.body().getStatus().getMessage() : "Lỗi không xác định"), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<GeneralResponse<String>> call, Throwable t) {
                    Toast.makeText(ChonGheActivity.this, "Lỗi kết nối khi giữ ghế: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        return seatView;
    }

    private void updateThongTinThanhToan() {
        int soGhe = gheDaChon.size();
        tongTien = soVe * giaVe;
        tvSoLuongGheChon.setText(String.format(Locale.getDefault(), "2D SUB %d ghế", soVe));
        tvGiaTienChonGhe.setText(String.format(Locale.getDefault(), "%dđ", tongTien));
        btnHoanTatChonGhe.setEnabled(soGhe > 0);
        btnHoanTatChonGhe.setBackgroundTintList(getColorStateList(soGhe > 0 ? R.color.green : R.color.gray));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerTickReceiver);
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }
}