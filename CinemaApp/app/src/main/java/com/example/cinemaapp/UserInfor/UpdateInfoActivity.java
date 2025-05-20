package com.example.cinemaapp.UserInfor;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinemaapp.R;
import com.example.cinemaapp.api.AuthAPI;
import com.example.cinemaapp.client.APIClient;
import com.example.cinemaapp.dto.UpdateInforDTO;
import com.example.cinemaapp.factory.GeneralResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Response;

public class UpdateInfoActivity extends AppCompatActivity {
    private EditText etDob, etEmail, etLastName, etFirstName, etPhone, etAddress;
    private Spinner spinnerGender, spinnerProvince;
    public static String ten;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_info);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        etDob = findViewById(R.id.et_dob);
        etEmail = findViewById(R.id.et_email);
        etFirstName = findViewById(R.id.et_first_name);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        spinnerGender = findViewById(R.id.spinner_gender);
        spinnerProvince = findViewById(R.id.spinner_province);

        etDob.setOnClickListener(v -> showDatePickerDialog());

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                Arrays.asList("Nam", "Nữ", "Khác")
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                Arrays.asList("Hà Nội", "TP. Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ")
        );
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);

//        fetchUserInfo();
        Intent intent = getIntent();
        if (intent != null) {
            UpdateInforDTO user = (UpdateInforDTO) intent.getSerializableExtra("updateInforDTO");
            assert user != null;
            etEmail.setText(user.getEmail());
            etFirstName.setText(user.getFullName());
            ten = user.getFullName();
            etPhone.setText(user.getTelephone()==null?"":user.getTelephone());
            etDob.setText(String.valueOf(user.getBirthday()==null?"":user.getBirthday()));
            etAddress.setText(user.getAddress()==null?"":user.getAddress());

            setSpinnerSelection(spinnerGender, user.getGender());
            setSpinnerSelection(spinnerProvince, user.getProvince());
        }

        Button btnUpdate = findViewById(R.id.btn_update);
        btnUpdate.setOnClickListener(v -> {
            UpdateInforDTO userDTO = new UpdateInforDTO();
            userDTO.setEmail(etEmail.getText().toString());
            userDTO.setFullName(etFirstName.getText().toString());
            userDTO.setTelephone(etPhone.getText().toString());
            userDTO.setBirthday(etDob.getText().toString());
            userDTO.setAddress(etAddress.getText().toString());
            userDTO.setGender(spinnerGender.getSelectedItem().toString());
            userDTO.setProvince(spinnerProvince.getSelectedItem().toString());
            try {
                updateUserInfor(userDTO);
            } catch (Exception e) {
                Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


//        private void fetchUserInfo() {
//            AuthAPI apiService = APIClient.getClient().create(AuthAPI.class);
//            apiService.getInfor().enqueue(new retrofit2.Callback<GeneralResponse<UpdateInforDTO>>() {
//                @Override
//                public void onResponse(Call<GeneralResponse<UpdateInforDTO>> call, Response<GeneralResponse<UpdateInforDTO>> response) {
//                    if (response.isSuccessful() && response.body() != null) {
//                        UpdateInforDTO user = response.body().getData();
//                        if (user != null) {
//                            etEmail.setText(user.getEmail());
//                            etFirstName.setText(user.getFullName());
//                            ten = user.getFullName();
//                            etPhone.setText(user.getTelephone()==null?"":user.getTelephone());
//                            etDob.setText(String.valueOf(user.getBirthday()==null?"":user.getBirthday()));
//                            etAddress.setText(user.getAddress()==null?"":user.getAddress());
//
//                            setSpinnerSelection(spinnerGender, user.getGender());
//                            setSpinnerSelection(spinnerProvince, user.getProvince());
//                        }
//                    } else {
//                        Toast.makeText(UpdateInfoActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
//                    }
//                }

//                @Override
//                public void onFailure(Call<GeneralResponse<UpdateInforDTO>> call, Throwable t) {
//                    Toast.makeText(UpdateInfoActivity.this, "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }

    private void updateUserInfor(UpdateInforDTO user) {
        AuthAPI apiService = APIClient.getClient().create(AuthAPI.class);
        Call<GeneralResponse<String>> call = apiService.updateInfor(user);

        call.enqueue(new retrofit2.Callback<GeneralResponse<String>>() {
            @Override
            public void onResponse(Call<GeneralResponse<String>> call, Response<GeneralResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(UpdateInfoActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Quay lại màn hình trước
                } else {
                    Toast.makeText(UpdateInfoActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GeneralResponse<String>> call, Throwable t) {
                Toast.makeText(UpdateInfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    etDob.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }
}
