package com.example.cinemaapp.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemaapp.MainActivity;
import com.example.cinemaapp.R;
import com.example.cinemaapp.UserInfor.ChangePasswordActivity;
import com.example.cinemaapp.UserInfor.DetailsActivity;
import com.example.cinemaapp.UserInfor.LogoutActivity;
import com.example.cinemaapp.UserInfor.MenuAdapter;
import com.example.cinemaapp.UserInfor.MenuItem;
import com.example.cinemaapp.UserInfor.RewardsActivity;
import com.example.cinemaapp.UserInfor.TransactionHistoryActivity;
import com.example.cinemaapp.UserInfor.UpdateInfoActivity;
import com.example.cinemaapp.api.AuthAPI;
import com.example.cinemaapp.client.APIClient;
import com.example.cinemaapp.dto.UpdateInforDTO;
import com.example.cinemaapp.factory.GeneralResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class UserInfoActivity extends AppCompatActivity {
    TextView tv_star_nguyet;
    ImageView reject, iv_qr_code;
    LinearLayout card_content,getCard_content;

    private UpdateInforDTO updateInforDTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        tv_star_nguyet = findViewById(R.id.tv_star_nguyet);
        reject = findViewById(R.id.reject);
        reject.setOnClickListener(v -> {
            Intent intent = new Intent( UserInfoActivity.this,MainActivity.class);
            startActivity(intent);
        });
        LinearLayout cardContent = findViewById(R.id.card_content);
        LinearLayout qrLayout = findViewById(R.id.qr_layout);
        ImageView ivQrCode = findViewById(R.id.iv_qr_code);

        cardContent.setOnClickListener(v -> {
            cardContent.setVisibility(View.GONE);
            qrLayout.setVisibility(View.VISIBLE);
        });

        ivQrCode.setOnClickListener(v -> {
            qrLayout.setVisibility(View.GONE);
            cardContent.setVisibility(View.VISIBLE);
        });
        // Set up RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rv_menu);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Menu items data
        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(R.drawable.ic_details, "Chi tiết"));
        menuItems.add(new MenuItem(R.drawable.ic_rewards, "Phần thưởng"));
        menuItems.add(new MenuItem(R.drawable.ic_update_info, "Cập nhật thông tin"));
        menuItems.add(new MenuItem(R.drawable.ic_change_password, "Thay đổi mật khẩu"));
        menuItems.add(new MenuItem(R.drawable.ic_transaction_history, "Lịch sử thanh toán"));
        menuItems.add(new MenuItem(R.drawable.ic_logout, "Đăng xuất"));

        // Set adapter
        MenuAdapter adapter = new MenuAdapter(menuItems, new MenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MenuItem item) {
                // Navigate to the appropriate activity based on the item title
                switch (item.getTitle()) {
                    case "Chi tiết":

                        startActivity(new Intent(UserInfoActivity.this, DetailsActivity.class));
                        break;
                    case "Phần thưởng":
                        startActivity(new Intent(UserInfoActivity.this, RewardsActivity.class));
                        break;
                    case "Cập nhật thông tin":
                        fetchUserInfo();
                        break;
                    case "Thay đổi mật khẩu":
                        startActivity(new Intent(UserInfoActivity.this, ChangePasswordActivity.class));
                        break;
                    case "Lịch sử thanh toán":
                        startActivity(new Intent(UserInfoActivity.this, TransactionHistoryActivity.class));
                        break;
                    case "Đăng xuất":

                        new AlertDialog.Builder(UserInfoActivity.this)
                                .setTitle("Xác nhận đăng xuất")
                                .setMessage("Bạn có chắc muốn đăng xuất khỏi tài khoản không?")
                                .setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Xử lý đăng xuất tại đây (ví dụ: xóa session, chuyển về màn hình login)
                                        Intent intent = new Intent(UserInfoActivity.this, LogoutActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear stack
                                        startActivity(intent);
                                        finish(); // Đóng activity hiện tại
                                    }
                                })
                                .setNegativeButton("Hủy", null) // Không làm gì nếu bấm Hủy
                                .show();
                        break;
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent( UserInfoActivity.this,MainActivity.class);
        startActivity(intent);
    }

    private void fetchUserInfo() {
        AuthAPI apiService = APIClient.getClient().create(AuthAPI.class);
        apiService.getInfor().enqueue(new retrofit2.Callback<GeneralResponse<UpdateInforDTO>>() {
            @Override
            public void onResponse(Call<GeneralResponse<UpdateInforDTO>> call, Response<GeneralResponse<UpdateInforDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateInforDTO = response.body().getData();
                    Intent intent = new Intent(UserInfoActivity.this, UpdateInfoActivity.class);
                    intent.putExtra("updateInforDTO", updateInforDTO);
                    startActivity(intent);
                } else {
                    Toast.makeText(UserInfoActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GeneralResponse<UpdateInforDTO>> call, Throwable t) {
                Toast.makeText(UserInfoActivity.this, "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

}