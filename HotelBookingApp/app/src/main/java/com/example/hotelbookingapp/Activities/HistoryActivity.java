package com.example.hotelbookingapp.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbookingapp.API.ApiService;
import com.example.hotelbookingapp.R;

import java.util.List;

import com.example.hotelbookingapp.Adapter.HistoryAdapter;
import com.example.hotelbookingapp.Model.BookingModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public  class HistoryActivity extends AppCompatActivity {
    // Khai báo biến giao diện (Danh sách cuộn) và biến gọi mạng
    private RecyclerView rvHistory;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // =====================================================================
        // 1. UI BINDING & SETUP: Khởi tạo giao diện danh sách
        // Nhiệm vụ: Kết nối RecyclerView từ giao diện XML và thiết lập layout
        // =====================================================================
        rvHistory = findViewById(R.id.rvHistory);
        // LinearLayoutManager: Báo cho Android biết danh sách này sẽ cuộn theo chiều dọc (từ trên xuống)
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        // =====================================================================
        // 2. NETWORK CONFIGURATION: Thiết lập cầu nối API
        // Nhiệm vụ: Cấu hình Retrofit để giao tiếp với Backend Python qua Ngrok
        // (Lưu ý nhỏ cho CV: Ở đây đang gán cứng link ngrok, thực tế có thể dùng MyConfig.BASE_URL cho đồng bộ)
        // =====================================================================
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://uncelebrated-lashandra-articulately.ngrok-free.dev/")
                .addConverterFactory(GsonConverterFactory.create()) // Tự động ép kiểu chuỗi JSON thành List Object Java
                .build();

        apiService = retrofit.create(ApiService.class);

        // =====================================================================
        // 3. DATA FETCHING (GET REQUEST): Truy xuất dữ liệu từ Server
        // Nhiệm vụ: Gọi API lấy toàn bộ lịch sử đặt phòng và chạy ngầm (Bất đồng bộ - enqueue)
        // =====================================================================
        apiService.getBookingHistory().enqueue(new Callback<List<BookingModel>>() {

            // Xử lý khi có phản hồi từ Server (Dù là thành công hay báo lỗi logic)
            @Override
            public void onResponse(Call<List<BookingModel>> call, Response<List<BookingModel>> response) {
                // Log để debug (theo dõi trạng thái HTTP, VD: 200 là OK, 404 là Not Found, 500 là Server hỏng)
                Log.d("HISTORY_DEBUG", "Response Code: " + response.code());

                // Bước 3.1: Kiểm tra luồng dữ liệu trả về có hợp lệ không (HTTP 2xx)
                if (response.isSuccessful() && response.body() != null) {

                    // Bước 3.2: Nhận cục dữ liệu List (Danh sách các đơn đặt phòng)
                    List<BookingModel> list = response.body();

                    // Bước 3.3: UI Update - Xử lý hiển thị dựa trên trạng thái dữ liệu
                    if (list.isEmpty()) {
                        // Trường hợp Database chưa có đơn nào -> Báo cho người dùng biết
                        Toast.makeText(HistoryActivity.this, "Chưa có lịch sử!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Trường hợp có dữ liệu -> Đổ dữ liệu vào Adapter
                        // Adapter đóng vai trò là "công nhân" nhét từng phần tử trong List vào từng dòng của RecyclerView
                        HistoryAdapter adapter = new HistoryAdapter(list);
                        rvHistory.setAdapter(adapter); // Lệnh chốt hạ: Vẽ lên màn hình!
                    }
                }
            }

            // Xử lý khi yêu cầu API bị sụp đổ hoàn toàn (Chưa đến được Server)
            // Ví dụ: Mất Wifi/4G, ngrok hết hạn, sai địa chỉ IP...
            @Override
            public void onFailure(Call<List<BookingModel>> call, Throwable t) {
                // Ghi log nguyên nhân sâu xa để Lập trình viên sửa lỗi
                Log.e("HISTORY_DEBUG", "Lỗi: " + t.getMessage());
                // Báo lỗi thân thiện cho Người dùng
                Toast.makeText(HistoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}