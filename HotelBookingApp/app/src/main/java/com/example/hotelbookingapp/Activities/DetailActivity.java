package com.example.hotelbookingapp.Activities;

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

import com.example.hotelbookingapp.API.ApiService;
import com.example.hotelbookingapp.API.MyConfig;
import com.example.hotelbookingapp.R;

import com.example.hotelbookingapp.Model.BookingRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.ResponseBody;

public class DetailActivity extends AppCompatActivity {
    // Biến quản lý các lệnh gọi API (Network Client)
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // =====================================================================
        // 1. NETWORK SETUP: Khởi tạo cầu nối giao tiếp với Backend Python
        // Nhiệm vụ: Thiết lập Retrofit để chuẩn bị gửi các HTTP Request (POST/GET)
        // =====================================================================
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MyConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) // Tự động convert JSON sang Java Object và ngược lại
                .build();
        apiService = retrofit.create(ApiService.class);

        // =====================================================================
        // 2. UI BINDING: Ánh xạ View
        // Nhiệm vụ: Kết nối các thành phần giao diện (XML) vào biến Java để thao tác
        // =====================================================================
        EditText edtName = findViewById(R.id.edtCustomerName);
        EditText edtCccd = findViewById(R.id.edtCccd);
        Button btnBook = findViewById(R.id.btnBook);

        // =====================================================================
        // 3. CORE LOGIC: Xử lý quy trình Đặt phòng (Booking Flow)
        // Nhiệm vụ: Bắt sự kiện người dùng, kiểm tra dữ liệu, đóng gói và gửi lên Server
        // =====================================================================
        btnBook.setOnClickListener(v -> {
            // Bước 3.1: Thu thập và chuẩn hóa dữ liệu đầu vào (Trim space)
            String inputName = edtName.getText().toString().trim();
            String inputCccd = edtCccd.getText().toString().trim();

            // Bước 3.2: Validation (Kiểm tra tính hợp lệ của dữ liệu)
            // Chặn người dùng gửi Request rỗng lên Server, tránh lỗi rác Database
            if (inputName.isEmpty() || inputCccd.isEmpty()) {
                Toast.makeText(DetailActivity.this, "Vui lòng nhập đủ Tên và CCCD!", Toast.LENGTH_SHORT).show();
                return; // Dừng luồng thực thi ngay lập tức
            }

            // Bước 3.3: Trích xuất dữ liệu Intent (Context Data)
            // Nhiệm vụ: Nhận thông tin Khách sạn đã được truyền từ màn hình danh sách (MainActivity/Adapter)
            int hId = getIntent().getIntExtra("hotel_id", 1);
            String hName = getIntent().getStringExtra("hotel_name");
            String hPriceStr = getIntent().getStringExtra("hotel_price");

            // Bước 3.4: Data Parsing (Ép kiểu dữ liệu an toàn)
            // Nhiệm vụ: Chuyển đổi giá tiền từ String sang Double, có bọc try-catch để chống Crash App (NullPointerException)
            double hPrice = 0.0;
            try {
                if (hPriceStr != null && !hPriceStr.isEmpty()) {
                    hPrice = Double.parseDouble(hPriceStr);
                }
            } catch (Exception e) {
                Log.e("DETAIL_ERR", "Lỗi ép kiểu giá: " + e.getMessage());
            }

            // Bước 3.5: Data Packaging (Đóng gói Request)
            // Nhiệm vụ: Gộp toàn bộ thông tin Khách hàng + Khách sạn vào một Object (Model) chuẩn để gửi đi
            BookingRequest request = new BookingRequest(hId, hName, inputName, inputCccd, hPrice);

            // Bước 3.6: Network Call (Thực thi gọi API Bất đồng bộ)
            // Nhiệm vụ: Bắn POST Request mang theo 'request' lên Server Python và chờ phản hồi
            apiService.postBooking(request).enqueue(new Callback<ResponseBody>() {

                // Trường hợp 1: Có kết nối mạng và Server trả về kết quả (HTTP 200, 400, 500...)
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    hideKeyboard(); // Tối ưu UX: Tự động hạ bàn phím xuống khi có kết quả

                    if (response.isSuccessful()) {
                        // Thành công (HTTP 2xx): Hiển thị Dialog thông báo và đóng màn hình (finish)
                        new AlertDialog.Builder(DetailActivity.this)
                                .setTitle("🎉 Đặt phòng thành công!")
                                .setMessage("Cảm ơn " + inputName + ".\nKhách sạn " + hName + " đã ghi nhận thông tin.")
                                .setCancelable(false)
                                .setPositiveButton("Tuyệt vời", (dialog, which) -> finish()) // Đẩy người dùng về màn hình trước
                                .show();
                    } else {
                        // Thất bại logic (HTTP 4xx, 5xx): Server từ chối lưu, báo lỗi code
                        Toast.makeText(DetailActivity.this, "Server lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                // Trường hợp 2: Lỗi vật lý (Mất mạng, sập Server, sai Base URL...)
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    hideKeyboard();
                    Toast.makeText(DetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        // =====================================================================
        // 4. ACTION BAR: Thiết lập thanh công cụ phía trên cùng
        // Nhiệm vụ: Kích hoạt nút Back (Mũi tên) để cải thiện luồng điều hướng (Navigation)
        // =====================================================================
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết đặt phòng");
        }
    }

    // =====================================================================
    // 5. NAVIGATION EVENT: Xử lý sự kiện bấm nút Back trên Action Bar
    // =====================================================================
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Kích hoạt lệnh quay lại mặc định của Android
        return true;
    }

    // =====================================================================
    // 6. UX UTILITY: Hàm tiện ích hỗ trợ trải nghiệm người dùng
    // Nhiệm vụ: Thu hồi bàn phím ảo (Soft Keyboard) xuống để không che khuất các thông báo (Dialog/Toast)
    // =====================================================================
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}