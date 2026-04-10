package com.example.hotelbookingapp.Activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbookingapp.API.ApiService;
import com.example.hotelbookingapp.API.RetrofitClient;
import com.example.hotelbookingapp.Model.RegisterRequest;
import com.example.hotelbookingapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvGoToLogin;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // TỐI ƯU: Sử dụng RetrofitClient
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        btnRegister.setOnClickListener(v -> handleRegister());
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest request = new RegisterRequest(fullName, email, password);

        btnRegister.setEnabled(false);
        btnRegister.setText("Đang xử lý...");

        apiService.registerUser(request).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký");

                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Email này đã được sử dụng!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                btnRegister.setEnabled(true);
                btnRegister.setText("Đăng ký");
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối Server!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}