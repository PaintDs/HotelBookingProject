package com.example.hotelbookingapp.Activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelbookingapp.API.ApiService;
import com.example.hotelbookingapp.API.RetrofitClient;
import com.example.hotelbookingapp.Adapter.HistoryAdapter;
import com.example.hotelbookingapp.Model.BookingModel;
import com.example.hotelbookingapp.R;
import com.example.hotelbookingapp.Utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private ApiService apiService;
    private List<BookingModel> bookingList; // Khai báo list ở mức toàn cục

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        // 1. Khởi tạo Adapter với list rỗng ngay từ đầu để tránh lỗi giật lag UI
        bookingList = new ArrayList<>();
        // Truyền thêm 'this' (Context) vào Adapter để lát nữa gọi Intent chuyển sang màn hình Thanh toán (nếu cần)
        adapter = new HistoryAdapter(this, bookingList);
        rvHistory.setAdapter(adapter);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
    }

    // 2. DỜI HÀM LOAD DỮ LIỆU VÀO onResume()
    @Override
    protected void onResume() {
        super.onResume();
        // Mỗi khi màn hình này hiện lên (ví dụ: từ màn hình Payment quay lại), nó sẽ tự động tải lại danh sách mới nhất.
        loadBookingHistory();
    }

    private void loadBookingHistory() {
        String emailCuaToi = SharedPrefManager.getInstance(this).getEmail();

        if (emailCuaToi == null || emailCuaToi.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy Email người dùng!", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getBookingHistory(emailCuaToi).enqueue(new Callback<List<BookingModel>>() {
            @Override
            public void onResponse(Call<List<BookingModel>> call, Response<List<BookingModel>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    // 3. Cập nhật dữ liệu thông minh
                    bookingList.clear(); // Xóa list cũ
                    bookingList.addAll(response.body()); // Đổ list mới từ API vào
                    adapter.notifyDataSetChanged(); // Ra lệnh cho RecyclerView vẽ lại giao diện

                    if (bookingList.isEmpty()) {
                        Toast.makeText(HistoryActivity.this, "Bạn chưa có đơn đặt phòng nào.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HistoryActivity.this, "Không thể lấy dữ liệu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BookingModel>> call, Throwable t) {
                Toast.makeText(HistoryActivity.this, "Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}