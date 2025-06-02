package com.example.cinemaapp.Schedule;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.example.cinemaapp.MainActivity;
import com.example.cinemaapp.R;
import com.example.cinemaapp.api.InvoiceAPI;
import com.example.cinemaapp.client.APIClient;
import com.example.cinemaapp.dto.response.InvoiceResponse;
import com.example.cinemaapp.factory.GeneralResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThanhToanActivity extends AppCompatActivity {

    // Khai báo các view
    private TextView movieTitleTextView;
    private TextView theaterInfoTextView;
    private TextView showtimeInfoTextView;
    private TextView seatInfoTextView;
    private TextView ticketDetailsTextView, ticketPriceTextView;
    private TextView foodDetailsTextView, foodPriceTextView;
    private TextView totalAmountTextView;
    private Button completePaymentButton;
    private TextView tvTimerThanhToan;
    private ImageView btnBackThanhToan;
    private CheckBox termsConditionsCheckbox;
    private TextView termsLinkTextView;
    private RadioGroup paymentMethodsGroup;
    private RadioButton paymentVnpayRadioButton;
    private RadioButton paymentMomoRadioButton;
    private RadioButton paymentZaloPayRadioButton;
    private RadioButton paymentShopeePayRadioButton;
    private LinearLayout paymentVnpayLayout;
    private LinearLayout paymentMomoLayout;
    private LinearLayout paymentZaloPayLayout;
    private LinearLayout paymentShopeePayLayout;

    // Các biến dữ liệu
    private String tenPhim;
    private int IdPhim;
    private String imageUrl;
    private String tenRap;
    private String ngayChieu;
    private String gioChieu;
    private InvoiceResponse invoiceResponse;
    private ArrayList<DatDoAnActivity.Combo> danhSachCombo = new ArrayList<>();

    // Các biến liên quan đến TimerService
    private TimerService timerService;
    private boolean isServiceBound = false;
    private ImageView moviePosterImageView;

    // Biến theo dõi trạng thái chọn phương thức thanh toán và điều khoản
    private boolean isPaymentMethodSelected = false;

    // Notification channel ID
    private static final String NOTIFICATION_CHANNEL_ID = "cinema_payment_channel";
    private static final int NOTIFICATION_ID = 1;

    private void selectPayment(int radioButtonId) {
        paymentMethodsGroup.check(radioButtonId);
        isPaymentMethodSelected = true;
        updatePaymentButtonState();
    }

    // BroadcastReceiver và ServiceConnection
    private final BroadcastReceiver timerTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TimerService.ACTION_TIMER_TICK.equals(intent.getAction())) {
                long remainingTime = intent.getLongExtra(TimerService.EXTRA_TIME_REMAINING, 0);
                tvTimerThanhToan.setText(timerService.formatTime(remainingTime / 1000));
            } else if (TimerService.ACTION_TIMER_FINISHED.equals(intent.getAction())) {
                tvTimerThanhToan.setText("00:00");
                Toast.makeText(ThanhToanActivity.this, "Đã hết thời gian.", Toast.LENGTH_LONG).show();
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
            tvTimerThanhToan.setText(timerService.formatTime(timerService.getRemainingTime() / 1000));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            timerService = null;
            isServiceBound = false;
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan);

        // Ánh xạ các view
        moviePosterImageView = findViewById(R.id.movie_poster);
        tvTimerThanhToan = findViewById(R.id.tvTimerThanhToan);
        btnBackThanhToan = findViewById(R.id.btnBackThanhToan);
        termsConditionsCheckbox = findViewById(R.id.terms_conditions);
        termsLinkTextView = findViewById(R.id.terms_link);
        completePaymentButton = findViewById(R.id.complete_payment);
        paymentMethodsGroup = findViewById(R.id.payment_methods_group);
        paymentVnpayRadioButton = findViewById(R.id.payment_vnpay);
        paymentMomoRadioButton = findViewById(R.id.payment_momo);
        paymentZaloPayRadioButton = findViewById(R.id.payment_zalopay);
        paymentShopeePayRadioButton = findViewById(R.id.payment_shopeepay);
        movieTitleTextView = findViewById(R.id.movie_title);
        theaterInfoTextView = findViewById(R.id.theater_info);
        showtimeInfoTextView = findViewById(R.id.showtime_info);
        seatInfoTextView = findViewById(R.id.seat_info);
        totalAmountTextView = findViewById(R.id.total_amount);
        ticketDetailsTextView = findViewById(R.id.ticket_details);
        foodDetailsTextView = findViewById(R.id.food_details);
        ticketPriceTextView = findViewById(R.id.ticket_price);
        foodPriceTextView = findViewById(R.id.food_price);

        // Ánh xạ các LinearLayout phương thức thanh toán
        paymentVnpayLayout = findViewById(R.id.payment_vnpay_layout);
        paymentMomoLayout = findViewById(R.id.payment_momo_layout);
        paymentZaloPayLayout = findViewById(R.id.payment_zalopay_layout);
        paymentShopeePayLayout = findViewById(R.id.payment_shopeepay_layout);

        // Ban đầu vô hiệu hóa nút thanh toán
        completePaymentButton.setEnabled(false);
        completePaymentButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray));

        // Thiết lập SpannableString cho TextView điều khoản
        String prefixText = "Tôi đã đọc, hiểu và đồng ý với ";
        String linkText = "điều khoản";
        String fullText = prefixText + linkText;
        SpannableString spannableString = new SpannableString(fullText);
        spannableString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, fullText.length(), 0);
        int startIndex = fullText.indexOf(linkText);
        int endIndex = startIndex + linkText.length();
        if (startIndex != -1) {
            spannableString.setSpan(new UnderlineSpan(), startIndex, endIndex, 0);
            spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), startIndex, endIndex, 0);
        }
        termsLinkTextView.setText(spannableString);
        termsLinkTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ThanhToanActivity.this, DieuKhoanActivity.class);
            startActivity(intent);
        });

        // Thiết lập OnClickListener cho các LinearLayout phương thức thanh toán
        paymentVnpayLayout.setOnClickListener(v -> selectPayment(R.id.payment_vnpay));
        paymentMomoLayout.setOnClickListener(v -> selectPayment(R.id.payment_momo));
        paymentZaloPayLayout.setOnClickListener(v -> selectPayment(R.id.payment_zalopay));
        paymentShopeePayLayout.setOnClickListener(v -> selectPayment(R.id.payment_shopeepay));

        paymentVnpayRadioButton.setClickable(false);
        paymentShopeePayRadioButton.setClickable(false);
        paymentZaloPayRadioButton.setClickable(false);
        paymentMomoRadioButton.setClickable(false);

        // Thiết lập OnCheckedChangeListener cho RadioGroup
        paymentMethodsGroup.setOnCheckedChangeListener((group, checkedId) -> {
            isPaymentMethodSelected = checkedId != -1;
            updatePaymentButtonState();
        });

        // Thiết lập OnCheckedChangeListener cho CheckBox điều khoản
        termsConditionsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updatePaymentButtonState();
        });

        // Tạo Notification Channel cho Android 8.0 trở lên
        createNotificationChannel();

        // Lấy dữ liệu từ Intent và cập nhật UI
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tenPhim = extras.getString("tenPhim");
            IdPhim = extras.getInt("idPhim", 0);
            imageUrl = extras.getString("imageUrl");
            tenRap = extras.getString("tenRap");
            ngayChieu = extras.getString("ngayChieu");
            gioChieu = extras.getString("gioChieu");
            invoiceResponse = (InvoiceResponse) extras.getSerializable("chiTietHoaDon");

            // Nhận danh sách combo (nếu có)
            ArrayList<DatDoAnActivity.Combo> comboList = (ArrayList<DatDoAnActivity.Combo>) extras.getSerializable("danhSachCombo");
            if (comboList != null) {
                danhSachCombo.addAll(comboList);
            }

            // Set thông tin cơ bản
            movieTitleTextView.setText(tenPhim);
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).into(moviePosterImageView);
            }
            theaterInfoTextView.setText(tenRap);
            showtimeInfoTextView.setText(String.format("%s - %s", ngayChieu, gioChieu));

            // Set động từ invoiceResponse
            if (invoiceResponse != null) {
                // Số lượng vé
                seatInfoTextView.setText(String.format(Locale.getDefault(), "%d vé", invoiceResponse.getCountTicket() != null ? invoiceResponse.getCountTicket() : 0));

                // Chi tiết vé (số lượng và vị trí ghế)
                String seatNames = invoiceResponse.getSeatName() != null && !invoiceResponse.getSeatName().isEmpty()
                        ? String.join(", ", invoiceResponse.getSeatName())
                        : "";
                ticketDetailsTextView.setText(String.format(Locale.getDefault(), "%d vé 2D: %s",
                        invoiceResponse.getCountTicket() != null ? invoiceResponse.getCountTicket() : 0,
                        seatNames));

                // Giá vé
                ticketPriceTextView.setText(String.format(Locale.getDefault(), "%dđ",
                        invoiceResponse.getTicketPrice() != null ? invoiceResponse.getTicketPrice().intValue() : 0));

                // Giá đồ ăn
                foodPriceTextView.setText(String.format(Locale.getDefault(), "%dđ",
                        invoiceResponse.getFoodPrice() != null ? invoiceResponse.getFoodPrice().intValue() : 0));

                // Tổng tiền
                totalAmountTextView.setText(String.format(Locale.getDefault(), "%dđ",
                        invoiceResponse.getTotalPrice() != null ? invoiceResponse.getTotalPrice().intValue() : 0));
            } else {
                // Fallback nếu invoiceResponse null
                seatInfoTextView.setText("0 vé");
                ticketDetailsTextView.setText("0 vé");
                ticketPriceTextView.setText("0đ");
                foodPriceTextView.setText("0đ");
                totalAmountTextView.setText("0đ");
            }

            // Hiển thị chi tiết đồ ăn (giữ nguyên logic từ danhSachCombo)
            StringBuilder foodDetailsBuilder = new StringBuilder();
            if (!danhSachCombo.isEmpty()) {
                for (DatDoAnActivity.Combo combo : danhSachCombo) {
                    foodDetailsBuilder.append(combo.getSoLuong()).append(" x ").append(combo.getTenCombo()).append("\n");
                }
                foodDetailsTextView.setText(foodDetailsBuilder.toString().trim());
            } else {
                foodDetailsTextView.setText("Không có đồ ăn hoặc nước được chọn.");
            }
        }

        // Thiết lập OnClickListener cho nút hoàn tất thanh toán
        completePaymentButton.setOnClickListener(v -> {
            if (termsConditionsCheckbox.isChecked() && isPaymentMethodSelected) {
                String selectedPaymentMethod = "";
                int checkedRadioButtonId = paymentMethodsGroup.getCheckedRadioButtonId();
                if (checkedRadioButtonId == R.id.payment_vnpay) {
                    selectedPaymentMethod = "VNPAY";
                } else if (checkedRadioButtonId == R.id.payment_momo) {
                    selectedPaymentMethod = "Momo";
                } else if (checkedRadioButtonId == R.id.payment_zalopay) {
                    selectedPaymentMethod = "ZaloPay";
                } else if (checkedRadioButtonId == R.id.payment_shopeepay) {
                    selectedPaymentMethod = "ShopeePay";
                }

                InvoiceAPI apiService = APIClient.getClient().create(InvoiceAPI.class);
                Call<GeneralResponse<String>> call = apiService.saveInvoice();
                call.enqueue(new Callback<GeneralResponse<String>>() {
                    @Override
                    public void onResponse(Call<GeneralResponse<String>> call, Response<GeneralResponse<String>> response) {
                        if (response.isSuccessful() && "Invoice saved successfully".equals(response.body().getData())) {
                            showPaymentSuccessNotification();
                            Intent intent = new Intent(ThanhToanActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<GeneralResponse<String>> call, Throwable t) {
                        Toast.makeText(ThanhToanActivity.this, "Lỗi khi lưu hóa đơn: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                Toast.makeText(ThanhToanActivity.this, "Thanh toán bằng " + selectedPaymentMethod + " hoàn tất!", Toast.LENGTH_SHORT).show();
            } else {
                if (!termsConditionsCheckbox.isChecked()) {
                    Toast.makeText(ThanhToanActivity.this, "Vui lòng đồng ý với các điều khoản.", Toast.LENGTH_SHORT).show();
                } else if (!isPaymentMethodSelected) {
                    Toast.makeText(ThanhToanActivity.this, "Vui lòng chọn một phương thức thanh toán.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ThanhToanActivity.this, "Vui lòng chọn phương thức thanh toán và đồng ý với điều khoản.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Bind to TimerService và Register BroadcastReceiver
        Intent serviceIntent = new Intent(this, TimerService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(timerTickReceiver, new IntentFilter(TimerService.ACTION_TIMER_TICK));
        LocalBroadcastManager.getInstance(this).registerReceiver(timerTickReceiver, new IntentFilter(TimerService.ACTION_TIMER_FINISHED));
        btnBackThanhToan.setOnClickListener(v -> finish());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Cinema Payment Notifications";
            String description = "Notifications for payment confirmations";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showPaymentSuccessNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Thay bằng icon của bạn
                .setContentTitle("Thanh toán thành công")
                .setContentText("Bạn đã hoàn tất hóa đơn đặt vé. Vào lịch sử thanh toán để xem chi tiết")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void updatePaymentButtonState() {
        boolean isTermsChecked = termsConditionsCheckbox.isChecked();
        completePaymentButton.setEnabled(isTermsChecked && isPaymentMethodSelected);
        if (completePaymentButton.isEnabled()) {
            completePaymentButton.setBackgroundTintList(getResources().getColorStateList(R.color.green));
        } else {
            completePaymentButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray));
        }
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