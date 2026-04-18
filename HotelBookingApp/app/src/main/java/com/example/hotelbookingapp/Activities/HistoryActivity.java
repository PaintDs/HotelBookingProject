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

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        loadBookingHistory();
    }

    private void loadBookingHistory() {
        // ============================================================
        // BƯỚC QUAN TRỌNG NHẤT: Lấy email từ Két sắt (SharedPref)
        // ============================================================
        String emailCuaToi = SharedPrefManager.getInstance(this).getEmail();

        if (emailCuaToi.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy Email người dùng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // TRUYỀN emailCuaToi VÀO ĐÂY ĐỂ SERVER BIẾT ĐƯỜNG MÀ LỌC
        apiService.getBookingHistory(emailCuaToi).enqueue(new Callback<List<BookingModel>>() {
            @Override
            public void onResponse(Call<List<BookingModel>> call, Response<List<BookingModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookingModel> list = response.body();

                    // Nếu danh sách rỗng, adapter sẽ tự hiện trắng (hoặc bạn có thể báo Toast)
                    adapter = new HistoryAdapter(list);
                    rvHistory.setAdapter(adapter);

                    if (list.isEmpty()) {
                        Toast.makeText(HistoryActivity.this, "Bạn chưa có đơn đặt phòng nào.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HistoryActivity.this, "Không thể lấy dữ liệu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<BookingModel>> call, Throwable t) {
                Toast.makeText(HistoryActivity.this, "Lỗi kết nối mạng!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}