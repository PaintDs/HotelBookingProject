package com.example.hotelbookingapp.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

        // 1. NHẬN DỮ LIỆU
        hotel = (Hotel) getIntent().getSerializableExtra("hotel_data");
        if (hotel == null) {
            Toast.makeText(this, "Không tìm thấy thông tin khách sạn!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // 2. UI BINDING
        ImageView imgDetail = findViewById(R.id.imgDetail);
        TextView tvName = findViewById(R.id.tvNameDetail);
        TextView tvAddress = findViewById(R.id.tvAddressDetail);
        TextView tvDesc = findViewById(R.id.tvDescriptionDetail);
        RatingBar ratingBar = findViewById(R.id.ratingBar);

        // ĐỔ DỮ LIỆU - FIX LỖI HIỂN THỊ MÔ TẢ
        tvName.setText(hotel.getName());
        tvAddress.setText(hotel.getAddress());

        // Gọi trực tiếp vì hàm getDescription() trong Hotel.java đã có logic bọc thép rồi
        tvDesc.setText(hotel.getDescription());

        ratingBar.setRating(4.5f);

        // FIX LỖI LOAD ẢNH NGROK
        String imageUrl = hotel.getImageUrl();
        if (imageUrl != null) {
            imageUrl = imageUrl.replace("127.0.0.1:8000", "uncelebrated-lashandra-articulately.ngrok-free.dev");
        }

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.loading_img)
                .error(R.drawable.error_img)
                .into(imgDetail);

        // 3. FORM ĐẶT PHÒNG & AUTO-FILL
        EditText edtName = findViewById(R.id.edtCustomerName);
        EditText edtCccd = findViewById(R.id.edtCccd);
        Button btnBook = findViewById(R.id.btnBook);

        SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        edtName.setText(sharedPref.getString(KEY_NAME, ""));
        edtCccd.setText(sharedPref.getString(KEY_CCCD, ""));

        // 4. XỬ LÝ ĐẶT PHÒNG
        btnBook.setOnClickListener(v -> {
            String inputName = edtName.getText().toString().trim();
            String inputCccd = edtCccd.getText().toString().trim();

            if (inputName.isEmpty() || inputCccd.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ Tên và CCCD!", Toast.LENGTH_SHORT).show();
                return;
            }
            // ==========================================
            // 2. KIỂM TRA CCCD CHUẨN THỰC TẾ (NEW)
            // Regex "^0\\d{11}$" nghĩa là: Bắt đầu bằng '0', theo sau là đúng 11 chữ số, và kết thúc.
            // ==========================================
            if (!inputCccd.matches("^0\\d{11}$")) {
                // Báo lỗi đỏ chót ngay tại ô nhập liệu (UI cực kỳ chuyên nghiệp)
                edtCccd.setError("CCCD không hợp lệ! Phải gồm 12 số và bắt đầu bằng số 0.");
                edtCccd.requestFocus(); // Tự động nháy con trỏ chuột vào lại ô này
                return; // Đuổi về, không cho gọi API đặt phòng
            }

            BookingRequest request = new BookingRequest(
                    hotel.getId(),
                    hotel.getName(),
                    inputName,
                    inputCccd,
                    hotel.getPrice_per_night()
            );

            apiService.postBooking(request).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    hideKeyboard();
                    if (response.isSuccessful()) {
                        sharedPref.edit()
                                .putString(KEY_NAME, inputName)
                                .putString(KEY_CCCD, inputCccd)
                                .apply();

                        new AlertDialog.Builder(DetailActivity.this)
                                .setTitle("🎉 Đặt thành công!")
                                .setMessage("Khách sạn " + hotel.getName() + " đã ghi nhận thông tin.")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog, which) -> finish())
                                .show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    hideKeyboard();
                    Toast.makeText(DetailActivity.this, "Lỗi kết nối server!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}