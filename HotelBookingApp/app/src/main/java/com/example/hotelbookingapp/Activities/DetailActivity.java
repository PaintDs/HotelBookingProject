package com.example.hotelbookingapp.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log; // Thêm Log để soi lỗi
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hotelbookingapp.API.ApiService;
import com.example.hotelbookingapp.API.RetrofitClient;
import com.example.hotelbookingapp.Model.BookingRequest;
import com.example.hotelbookingapp.Model.Hotel;
import com.example.hotelbookingapp.R;
import com.example.hotelbookingapp.Utils.SharedPrefManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {
    private ApiService apiService;
    private Hotel hotel;

    private static final String PREF_NAME = "UserCache";
    private static final String KEY_NAME = "saved_name";
    private static final String KEY_CCCD = "saved_cccd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 1. NHẬN DỮ LIỆU TỪ INTENT
        hotel = (Hotel) getIntent().getSerializableExtra("hotel_data");
        if (hotel == null) {
            Toast.makeText(this, "Không tìm thấy thông tin khách sạn!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // 2. ÁNH XẠ UI
        ImageView imgDetail = findViewById(R.id.imgDetail);
        TextView tvName = findViewById(R.id.tvNameDetail);
        TextView tvAddress = findViewById(R.id.tvAddressDetail);
        TextView tvDesc = findViewById(R.id.tvDescriptionDetail);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        EditText edtName = findViewById(R.id.edtCustomerName);
        EditText edtCccd = findViewById(R.id.edtCccd);
        Button btnBook = findViewById(R.id.btnBook);

        tvName.setText(hotel.getName());
        tvAddress.setText(hotel.getAddress());
        tvDesc.setText(hotel.getDescription());
        ratingBar.setRating(4.5f);

        // Xử lý ảnh cho Ngrok
        String imageUrl = hotel.getImageUrl();
        if (imageUrl != null && imageUrl.contains("127.0.0.1")) {
            imageUrl = imageUrl.replace("127.0.0.1:8000", "uncelebrated-lashandra-articulately.ngrok-free.dev");
        }

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.loading_img)
                .error(R.drawable.error_img)
                .into(imgDetail);

        // Load cache thông tin nhập liệu cũ
        SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        edtName.setText(sharedPref.getString(KEY_NAME, ""));
        edtCccd.setText(sharedPref.getString(KEY_CCCD, ""));

        // 4. XỬ LÝ ĐẶT PHÒNG
        btnBook.setOnClickListener(v -> {

            // KIỂM TRA ĐĂNG NHẬP (Dùng getApplicationContext cho ổn định)
            SharedPrefManager prefManager = SharedPrefManager.getInstance(getApplicationContext());

            if (!prefManager.isLoggedIn()) {
                Toast.makeText(this, "Vui lòng đăng nhập để đặt phòng!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }

            String inputName = edtName.getText().toString().trim();
            String inputCccd = edtCccd.getText().toString().trim();

            // LẤY EMAIL CHÍNH CHỦ
            String userEmail = prefManager.getEmail();

            // LOG ĐỂ SOI LỖI TRONG LOGCAT (Search từ khóa: CHECK_DATA)
            Log.d("CHECK_DATA", "Email gui len: " + userEmail);
            Log.d("CHECK_DATA", "Ten khach: " + inputName);

            // VALIDATION
            if (inputName.isEmpty() || inputCccd.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!inputCccd.matches("^0\\d{11}$")) {
                edtCccd.setError("CCCD phải đủ 12 số và bắt đầu bằng số 0!");
                return;
            }

            if (userEmail == null || userEmail.isEmpty()) {
                Toast.makeText(this, "Lỗi: Không tìm thấy Email đăng nhập. Hãy đăng nhập lại!", Toast.LENGTH_LONG).show();
                return;
            }
            android.util.Log.e("KIEM_TRA_EMAIL", "Email lấy từ két sắt là: ---> [" + userEmail + "]");

            // ĐÓNG GÓI REQUEST
            BookingRequest request = new BookingRequest(

                    hotel.getId(),
                    hotel.getName(),
                    inputName,
                    inputCccd,
                    hotel.getPrice_per_night(),
                    userEmail
            );

            // GỌI API
            apiService.postBooking(request).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    hideKeyboard();
                    if (response.isSuccessful()) {
                        // Lưu cache Tên/CCCD
                        sharedPref.edit()
                                .putString(KEY_NAME, inputName)
                                .putString(KEY_CCCD, inputCccd)
                                .apply();

                        new AlertDialog.Builder(DetailActivity.this)
                                .setTitle("🎉 Đặt thành công!")
                                .setMessage("Thông tin đã được gửi cho tài khoản: " + userEmail)
                                .setCancelable(false)
                                .setPositiveButton("Xem lịch sử", (dialog, which) -> {
                                    startActivity(new Intent(DetailActivity.this, HistoryActivity.class));
                                    finish();
                                })
                                .setNegativeButton("OK", (dialog, which) -> finish())
                                .show();
                    } else {
                        Toast.makeText(DetailActivity.this, "Lỗi Server: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    hideKeyboard();
                    Log.e("API_ERROR", "Loi ket noi: " + t.getMessage());
                    Toast.makeText(DetailActivity.this, "Lỗi kết nối Server!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
    }
}