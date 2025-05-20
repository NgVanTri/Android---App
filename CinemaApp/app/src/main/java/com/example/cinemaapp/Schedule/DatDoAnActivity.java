package com.example.cinemaapp.Schedule;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.cinemaapp.R;
import com.example.cinemaapp.api.InvoiceAPI;
import com.example.cinemaapp.client.APIClient;
import com.example.cinemaapp.dto.FoodDetailDTO;
import com.example.cinemaapp.dto.response.InvoiceResponse;
import com.example.cinemaapp.factory.GeneralResponse;
import com.example.cinemaapp.model.Food;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DatDoAnActivity extends AppCompatActivity {
    public static class Combo implements Serializable {
        private String tenCombo;
        private int soLuong;
        private int giaCombo;

        public Combo(String tenCombo, int soLuong, int giaCombo) {
            this.tenCombo = tenCombo;
            this.soLuong = soLuong;
            this.giaCombo = giaCombo;
        }

        public String getTenCombo() {
            return tenCombo;
        }

        public int getSoLuong() {
            return soLuong;
        }

        public int getGiaCombo() {
            return giaCombo;
        }
    }

    private String viTriGhe;
    private ImageView btnBackDatDoAn;
    private TextView tvTenPhimDatDoAn;
    private TextView tvSoLuongGheDatDoAn;
    private TextView tvTongTienDatDoAn;
    private TextView tvGiaTienDatDoAn;
    private Button btnHoanTatChonGhe;
    private String tenPhim;
    private Integer IdPhim;
    private String imageUrl;
    private int soLuongGhe = 0;
    private int tongTienVe = 0;
    private int tongTienDoAn = 0;
    private int tongTienThanhToan = 0;
    private TextView tvTimerDatDoAn;
    private TextView tvTenRapDatDoAn;
    private TextView tvNgayChieuDatDoAn;
    private TextView tvGioChieuDatDoAn;
    private String tenRap;
    private String ngayChieu;
    private String gioChieu;

    private InvoiceResponse invoiceResponse;

    // Dữ liệu từ foodList
    private List<Food> foodList;
    private int[] soLuongCombos;
    private float[] giaCombos;
    private String[] tenCombos;
    private String[] moTaCombos;
    private final int maxCombos = 6;

    // View cho combo
    private ImageView[] btnTangCombos;
    private ImageView[] btnGiamCombos;
    private TextView[] tvSoLuongCombos;
    private TextView[] tvTenCombos;
    private TextView[] tvMoTaCombos;
    private TextView[] tvGiaCombos;

    private TimerService timerService;
    private boolean isServiceBound = false;

    private final BroadcastReceiver timerTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TimerService.ACTION_TIMER_TICK.equals(intent.getAction())) {
                long remainingTime = intent.getLongExtra(TimerService.EXTRA_TIME_REMAINING, 0);
                tvTimerDatDoAn.setText(timerService.formatTime(remainingTime / 1000));
            } else if (TimerService.ACTION_TIMER_FINISHED.equals(intent.getAction())) {
                tvTimerDatDoAn.setText("00:00");
                Toast.makeText(DatDoAnActivity.this, "Đã hết thời gian.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
            timerService = binder.getService();
            isServiceBound = true;
            tvTimerDatDoAn.setText(timerService.formatTime(timerService.getRemainingTime() / 1000));
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
        setContentView(R.layout.activity_dat_do_an);

        // Bind TimerService
        Intent serviceIntent = new Intent(this, TimerService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        // Register BroadcastReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(timerTickReceiver,
                new IntentFilter(TimerService.ACTION_TIMER_TICK));
        LocalBroadcastManager.getInstance(this).registerReceiver(timerTickReceiver,
                new IntentFilter(TimerService.ACTION_TIMER_FINISHED));

        // Ánh xạ view
        btnBackDatDoAn = findViewById(R.id.btnBackChonGhe);
        tvTenPhimDatDoAn = findViewById(R.id.tvTenPhimChonGhe);
        tvSoLuongGheDatDoAn = findViewById(R.id.tvSoLuongGheChon);
        tvTongTienDatDoAn = findViewById(R.id.tvTongTienChonGhe);
        tvGiaTienDatDoAn = findViewById(R.id.tvGiaTienChonGhe);
        btnHoanTatChonGhe = findViewById(R.id.btnHoanTatChonGhe);
        btnHoanTatChonGhe.setText("Hoàn tất thanh toán (2/3)");
        tvTimerDatDoAn = findViewById(R.id.tvTimerDatDoAn);
        tvTenRapDatDoAn = findViewById(R.id.tvTenRapDatDoAn);
        tvNgayChieuDatDoAn = findViewById(R.id.tvNgayChieuDatDoAn);
        tvGioChieuDatDoAn = findViewById(R.id.tvGioChieuDatDoAn);

        // Ánh xạ view combo
        btnTangCombos = new ImageView[]{
                findViewById(R.id.btnTangCombo1),
                findViewById(R.id.btnTangCombo2),
                findViewById(R.id.btnTangCombo3),
                findViewById(R.id.btnTangCombo4),
                findViewById(R.id.btnTangCombo5),
                findViewById(R.id.btnTangCombo6)
        };
        btnGiamCombos = new ImageView[]{
                findViewById(R.id.btnGiamCombo1),
                findViewById(R.id.btnGiamCombo2),
                findViewById(R.id.btnGiamCombo3),
                findViewById(R.id.btnGiamCombo4),
                findViewById(R.id.btnGiamCombo5),
                findViewById(R.id.btnGiamCombo6)
        };
        tvSoLuongCombos = new TextView[]{
                findViewById(R.id.tvSoLuongCombo1),
                findViewById(R.id.tvSoLuongCombo2),
                findViewById(R.id.tvSoLuongCombo3),
                findViewById(R.id.tvSoLuongCombo4),
                findViewById(R.id.tvSoLuongCombo5),
                findViewById(R.id.tvSoLuongCombo6)
        };
        tvTenCombos = new TextView[]{
                findViewById(R.id.tvTenCombo1),
                findViewById(R.id.tvTenCombo2),
                findViewById(R.id.tvTenCombo3),
                findViewById(R.id.tvTenCombo4),
                findViewById(R.id.tvTenCombo5),
                findViewById(R.id.tvTenCombo6)
        };
        tvMoTaCombos = new TextView[]{
                findViewById(R.id.tvMoTaCombo1),
                findViewById(R.id.tvMoTaCombo2),
                findViewById(R.id.tvMoTaCombo3),
                findViewById(R.id.tvMoTaCombo4),
                findViewById(R.id.tvMoTaCombo5),
                findViewById(R.id.tvMoTaCombo6)
        };
        tvGiaCombos = new TextView[]{
                findViewById(R.id.tvGiaCombo1),
                findViewById(R.id.tvGiaCombo2),
                findViewById(R.id.tvGiaCombo3),
                findViewById(R.id.tvGiaCombo4),
                findViewById(R.id.tvGiaCombo5),
                findViewById(R.id.tvGiaCombo6)
        };

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            tenPhim = intent.getStringExtra("tenPhim");
            IdPhim = intent.getIntExtra("idPhim", 0);
            imageUrl = intent.getStringExtra("imageUrl");
            soLuongGhe = intent.getIntExtra("soLuongVe", 0);
            tongTienVe = intent.getIntExtra("tongTienVe", 0);
            tenRap = intent.getStringExtra("tenRap");
            ngayChieu = intent.getStringExtra("ngayChieu");
            gioChieu = intent.getStringExtra("gioChieu");
            foodList = (List<Food>) intent.getSerializableExtra("foodList");
            viTriGhe = intent.getStringExtra("viTriGhe");

            Log.d("DEBUG_GHE", "← DatDoAn nhận: SL=" + soLuongGhe + " | viTri=" + viTriGhe);
            Log.d("FOOD_LIST", "Size: " + (foodList != null ? foodList.size() : "null"));
            if (foodList != null) {
                for (Food food : foodList) {
                    Log.d("FOOD_LIST", "ID: " + food.getId() + ", Name: " + food.getFoodName() +
                            ", Price: " + food.getPrice() + ", Desc: " + food.getDescription());
                }
            }

            // Cập nhật giao diện
            tvTenPhimDatDoAn.setText(tenPhim);
            tvSoLuongGheDatDoAn.setText(String.format(Locale.getDefault(), "2D SUB %d ghế", soLuongGhe));
            tvTongTienDatDoAn.setText("Tổng tiền:");
            tvGiaTienDatDoAn.setText(String.format(Locale.getDefault(), "%dđ", tongTienVe));
            tongTienThanhToan = tongTienVe;
            tvTenRapDatDoAn.setText(tenRap);
            tvNgayChieuDatDoAn.setText(ngayChieu);
            tvGioChieuDatDoAn.setText(String.format("%s - %s", gioChieu, calculateEndTime(gioChieu)));
        }

        // Khởi tạo dữ liệu combo từ foodList
        initializeFoodData();

        // Xử lý nút back
        btnBackDatDoAn.setOnClickListener(v -> finish());

        // Xử lý nút hoàn tất
        btnHoanTatChonGhe.setOnClickListener(v -> {

            List<FoodDetailDTO> foodOrderList = new ArrayList<>();
            for (int i = 0; i < Math.min(foodList.size(), maxCombos); i++) {
                if (soLuongCombos[i] > 0) {
                    foodOrderList.add(new FoodDetailDTO(
                            foodList.get(i).getId(),
                            soLuongCombos[i]
                    ));
                }
            }




            ArrayList<Combo> danhSachCombo = new ArrayList<>();
            for (int i = 0; i < Math.min(foodList.size(), maxCombos); i++) {
                if (soLuongCombos[i] > 0) {
                    danhSachCombo.add(new Combo(
                            tenCombos[i],
                            soLuongCombos[i],
                            (int) giaCombos[i]
                    ));
                }
            }

            InvoiceAPI apiService = APIClient.getClient().create(InvoiceAPI.class);
            Call<GeneralResponse<InvoiceResponse>> call = apiService.createInvoice(foodOrderList);
            call.enqueue(new Callback<GeneralResponse<InvoiceResponse>>() {

                @Override
                public void onResponse(Call<GeneralResponse<InvoiceResponse>> call, Response<GeneralResponse<InvoiceResponse>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        invoiceResponse = response.body().getData();
                        Intent intentThanhToan = new Intent(DatDoAnActivity.this, ThanhToanActivity.class);
                        intentThanhToan.putExtra("tongTienThanhToan", tongTienThanhToan);
                        intentThanhToan.putExtra("tenPhim", tenPhim);
                        intentThanhToan.putExtra("idPhim", IdPhim);
                        intentThanhToan.putExtra("imageUrl", imageUrl);
                        intentThanhToan.putExtra("soLuongVe", soLuongGhe);
                        intentThanhToan.putExtra("viTriGhe", viTriGhe);
                        intentThanhToan.putExtra("tongTienVe", tongTienVe);
                        intentThanhToan.putExtra("tongTienDoAn", tongTienDoAn);
                        intentThanhToan.putExtra("tenRap", tenRap);
                        intentThanhToan.putExtra("ngayChieu", ngayChieu);
                        intentThanhToan.putExtra("gioChieu", gioChieu);
                        intentThanhToan.putExtra("danhSachCombo", danhSachCombo);
                        intentThanhToan.putExtra("chiTietHoaDon", invoiceResponse);

                        Log.d("DEBUG_GHE", "→ DatDoAn gửi: SL=" + soLuongGhe + " | viTri=" + viTriGhe);
                        Log.d("DANH_SACH_COMBO", danhSachCombo.toString());
                        startActivity(intentThanhToan);
                    }
                }

                @Override
                public void onFailure(Call<GeneralResponse<InvoiceResponse>> call, Throwable t) {
                    Toast.makeText(DatDoAnActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        });

        // Thiết lập listener cho combo
        setupComboListeners();
    }

    private void initializeFoodData() {
        if (foodList == null || foodList.isEmpty()) {
            Toast.makeText(this, "Không có danh sách đồ ăn", Toast.LENGTH_SHORT).show();
            // Ẩn tất cả view combo
            for (int i = 0; i < maxCombos; i++) {
                btnTangCombos[i].setVisibility(View.GONE);
                btnGiamCombos[i].setVisibility(View.GONE);
                tvSoLuongCombos[i].setVisibility(View.GONE);
                tvTenCombos[i].setVisibility(View.GONE);
                tvMoTaCombos[i].setVisibility(View.GONE);
                tvGiaCombos[i].setVisibility(View.GONE);
            }
            return;
        }

        // Khởi tạo mảng
        soLuongCombos = new int[maxCombos];
        giaCombos = new float[maxCombos];
        tenCombos = new String[maxCombos];
        moTaCombos = new String[maxCombos];

        // Format giá
        DecimalFormat decimalFormat = new DecimalFormat("#,###đ");

        // Map foodList vào view
        int foodCount = Math.min(foodList.size(), maxCombos);
        for (int i = 0; i < maxCombos; i++) {
            if (i < foodCount) {
                // Gán dữ liệu từ foodList
                Food food = foodList.get(i);
                soLuongCombos[i] = 0;
                giaCombos[i] = food.getPrice();
                tenCombos[i] = food.getFoodName();
                moTaCombos[i] = food.getDescription() != null ? food.getDescription() : "";
                tvSoLuongCombos[i].setText("0");
                tvTenCombos[i].setText(food.getFoodName());
                tvMoTaCombos[i].setText(moTaCombos[i]);
                tvGiaCombos[i].setText(decimalFormat.format(food.getPrice()));
            } else {
                // Ẩn view không dùng
                btnTangCombos[i].setVisibility(View.GONE);
                btnGiamCombos[i].setVisibility(View.GONE);
                tvSoLuongCombos[i].setVisibility(View.GONE);
                tvTenCombos[i].setVisibility(View.GONE);
                tvMoTaCombos[i].setVisibility(View.GONE);
                tvGiaCombos[i].setVisibility(View.GONE);
            }
        }
    }

    private void setupComboListeners() {
        for (int i = 0; i < maxCombos; i++) {
            final int index = i;
            if (index < foodList.size()) {
                btnTangCombos[i].setOnClickListener(v -> {
                    soLuongCombos[index]++;
                    tvSoLuongCombos[index].setText(String.valueOf(soLuongCombos[index]));
                    tongTienDoAn += (int) giaCombos[index];
                    updateTongTienThanhToan();
                });

                btnGiamCombos[i].setOnClickListener(v -> {
                    if (soLuongCombos[index] > 0) {
                        soLuongCombos[index]--;
                        tvSoLuongCombos[index].setText(String.valueOf(soLuongCombos[index]));
                        tongTienDoAn -= (int) giaCombos[index];
                        updateTongTienThanhToan();
                    }
                });
            }
        }
    }

    private void updateTongTienThanhToan() {
        tongTienThanhToan = tongTienVe + tongTienDoAn;
        tvGiaTienDatDoAn.setText(String.format(Locale.getDefault(), "%dđ", tongTienThanhToan));
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