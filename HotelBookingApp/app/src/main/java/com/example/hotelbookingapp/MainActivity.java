package com.example.hotelbookingapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ApiService apiService;
    private RecyclerView rv;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo giao diện danh sách
        rv = findViewById(R.id.rvHotels);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo dịch vụ định vị của Google
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Cấu hình kết nối API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MyConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Cấu hình nút xem lịch sử
        Button btnOpenHistory = findViewById(R.id.btnOpenHistory);
        btnOpenHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // Bắt đầu quy trình kiểm tra quyền và tải dữ liệu
        checkLocationPermissionAndFetchData();
    }

    // Kiểm tra quyền GPS
    private void checkLocationPermissionAndFetchData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Chưa có quyền -> Hiển thị bảng hỏi xin quyền
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            // Đã có quyền -> Bắt đầu lấy tọa độ
            getLocationAndCallApi();
        }
    }

    // Lấy tọa độ GPS hiện tại và gọi API
    private void getLocationAndCallApi() {
        // =================================================================
        // 🔥 ĐOẠN CODE "HACK" ÉP CỨNG TỌA ĐỘ ĐẠI HỌC THĂNG LONG 🔥
        // =================================================================
        double my_lat = 20.9806;
        double my_lng = 105.8159;

        Toast.makeText(this, "Đang dùng GPS giả: Đại học Thăng Long", Toast.LENGTH_SHORT).show();

        // Gọi thẳng API tính khoảng cách luôn, bỏ qua bước check GPS của máy
        fetchNearbyHotels(my_lat, my_lng);

        // =================================================================
        // 🔒 MÌNH ĐÃ COMMENT (ẨN) ĐOẠN CODE LẤY GPS THẬT XUỐNG DƯỚI NÀY
        // KHI NÀO TEST XONG MUỐN DÙNG LẠI THÌ XÓA DẤU /* VÀ */ ĐI NHÉ
        // =================================================================
        /*
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                // Đã lấy được tọa độ -> Gọi API tìm khách sạn gần đây
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                fetchNearbyHotels(lat, lng);
            } else {
                // Không lấy được tọa độ (do GPS thiết bị đang tắt) -> Gọi API danh sách mặc định
                Toast.makeText(this, "Không tìm thấy GPS, tải danh sách mặc định", Toast.LENGTH_SHORT).show();
                fetchAllHotels();
            }
        });
        */
    }

    // Gửi tọa độ lên Server để lấy danh sách khách sạn đã tính khoảng cách
    private void fetchNearbyHotels(double lat, double lng) {
        apiService.getNearbyHotels(lat, lng).enqueue(new Callback<List<hotel>>() {
            @Override
            public void onResponse(Call<List<hotel>> call, Response<List<hotel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Sửa dòng cũ thành:
                    HotelAdapter adapter = new HotelAdapter(response.body(), lat, lng);
                    rv.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<hotel>> call, Throwable t) {
                Log.e("API_ERR", "Lỗi mạng: " + t.getMessage());
            }
        });
    }

    // Lấy toàn bộ danh sách khách sạn (Trường hợp dự phòng)
    private void fetchAllHotels() {
        apiService.getHotels().enqueue(new Callback<List<hotel>>() {
            @Override
            public void onResponse(Call<List<hotel>> call, Response<List<hotel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Sửa dòng cũ thành (Truyền 0.0 vào để nó biết là mất định vị):
                    HotelAdapter adapter = new HotelAdapter(response.body(), 0.0, 0.0);
                    rv.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<hotel>> call, Throwable t) {
                Log.e("API_ERR", "Lỗi mạng: " + t.getMessage());
            }
        });
    }

    // Lắng nghe kết quả khi người dùng bấm "Cho phép" hoặc "Từ chối" trên bảng xin quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Người dùng cho phép -> Lấy vị trí
                getLocationAndCallApi();
            } else {
                // Người dùng từ chối -> Tải danh sách mặc định
                Toast.makeText(this, "Bị từ chối GPS, tải danh sách mặc định", Toast.LENGTH_SHORT).show();
                fetchAllHotels();
            }
        }
    }
}