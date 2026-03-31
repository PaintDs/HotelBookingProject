package com.example.hotelbookingapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.ResponseBody;

public class DetailActivity extends AppCompatActivity {
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 1. Khởi tạo Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MyConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // 2. Ánh xạ ID
        EditText edtName = findViewById(R.id.edtCustomerName);
        EditText edtCccd = findViewById(R.id.edtCccd);
        Button btnBook = findViewById(R.id.btnBook);

        // 3. Xử lý nút Đặt phòng
        btnBook.setOnClickListener(v -> {
            String inputName = edtName.getText().toString().trim();
            String inputCccd = edtCccd.getText().toString().trim();

            if (inputName.isEmpty() || inputCccd.isEmpty()) {
                Toast.makeText(DetailActivity.this, "Vui lòng nhập đủ Tên và CCCD!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy dữ liệu từ Intent (truyền từ MainActivity sang)
            int hId = getIntent().getIntExtra("hotel_id", 1);
            String hName = getIntent().getStringExtra("hotel_name");
            String hPriceStr = getIntent().getStringExtra("hotel_price");

            double hPrice = 0.0;
            try {
                if (hPriceStr != null && !hPriceStr.isEmpty()) {
                    hPrice = Double.parseDouble(hPriceStr);
                }
            } catch (Exception e) {
                Log.e("DETAIL_ERR", "Lỗi ép kiểu giá: " + e.getMessage());
            }

            // Gói dữ liệu vào Model
            BookingRequest request = new BookingRequest(hId, hName, inputName, inputCccd, hPrice);

            // Gửi API Post
            apiService.postBooking(request).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    hideKeyboard();
                    if (response.isSuccessful()) {
                        new AlertDialog.Builder(DetailActivity.this)
                                .setTitle("🎉 Đặt phòng thành công!")
                                .setMessage("Cảm ơn " + inputName + ".\nKhách sạn " + hName + " đã ghi nhận thông tin.")
                                .setCancelable(false)
                                .setPositiveButton("Tuyệt vời", (dialog, which) -> finish())
                                .show();
                    } else {
                        Toast.makeText(DetailActivity.this, "Server lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    hideKeyboard();
                    Toast.makeText(DetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        // 4. Setup nút Back (Dấu mũi tên quay lại)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết đặt phòng");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}