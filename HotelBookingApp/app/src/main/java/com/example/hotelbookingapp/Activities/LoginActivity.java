package com.example.hotelbookingapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbookingapp.API.ApiService;
import com.example.hotelbookingapp.API.RetrofitClient;
import com.example.hotelbookingapp.Model.LoginRequest;
import com.example.hotelbookingapp.Model.LoginResponse;
import com.example.hotelbookingapp.R;
import com.example.hotelbookingapp.Utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private MaterialButton btnLogin;
    private TextView tvGoToRegister;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);

        // TỐI ƯU: Sử dụng RetrofitClient
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        btnLogin.setOnClickListener(v -> handleLogin());

        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(email, password);

        btnLogin.setEnabled(false);
        btnLogin.setText("Đang xử lý...");

        apiService.loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    SharedPrefManager.getInstance(LoginActivity.this)
                            .saveUser(loginResponse.getAccessToken(), loginResponse.getFullName(), email);

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Đăng nhập");
                Toast.makeText(LoginActivity.this, "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}