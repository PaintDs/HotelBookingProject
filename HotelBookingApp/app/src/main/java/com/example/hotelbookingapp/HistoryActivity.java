package com.example.hotelbookingapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public  class HistoryActivity extends AppCompatActivity {
    private RecyclerView rvHistory;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // 1. Ánh xạ ID (Kiểm tra xem XML có ID này chưa)
        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        // 2. Khởi tạo Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://uncelebrated-lashandra-articulately.ngrok-free.dev/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // 3. Gọi API lấy dữ liệu
        apiService.getBookingHistory().enqueue(new Callback<List<BookingModel>>() {
            @Override
            public void onResponse(Call<List<BookingModel>> call, Response<List<BookingModel>> response) {
                Log.d("HISTORY_DEBUG", "Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    List<BookingModel> list = response.body();
                    if (list.isEmpty()) {
                        Toast.makeText(HistoryActivity.this, "Chưa có lịch sử!", Toast.LENGTH_SHORT).show();
                    } else {
                        HistoryAdapter adapter = new HistoryAdapter(list);
                        rvHistory.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<BookingModel>> call, Throwable t) {
                Log.e("HISTORY_DEBUG", "Lỗi: " + t.getMessage());
                Toast.makeText(HistoryActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}