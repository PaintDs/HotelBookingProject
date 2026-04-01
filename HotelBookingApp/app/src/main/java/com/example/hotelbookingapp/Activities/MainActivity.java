package com.example.hotelbookingapp.Activities;

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

import com.example.hotelbookingapp.API.ApiService;
import com.example.hotelbookingapp.API.MyConfig;
import com.example.hotelbookingapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import com.example.hotelbookingapp.Adapter.HotelAdapter;
import com.example.hotelbookingapp.Model.Hotel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    // Các biến quản lý cốt lõi của màn hình chính
    private ApiService apiService;
    private RecyclerView rv;
    private FusedLocationProviderClient fusedLocationClient; // Thư viện định vị tối ưu nhất của Google
    private static final int LOCATION_PERMISSION_CODE = 100; // Mã định danh cho luồng xin quyền GPS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // =====================================================================
        // 1. UI INITIALIZATION: Khởi tạo giao diện
        // =====================================================================
        rv = findViewById(R.id.rvHotels);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // =====================================================================
        // 2. HARDWARE SERVICE SETUP: Khởi tạo Dịch vụ Định vị
        // Nhiệm vụ: Đánh thức FusedLocationProviderClient để chuẩn bị bắt tọa độ (kết hợp GPS, Wifi, 4G)
        // =====================================================================
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // =====================================================================
        // 3. NETWORK SETUP: Cấu hình Retrofit gọi API
        // =====================================================================
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MyConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // =====================================================================
        // 4. NAVIGATION BINDING: Lắng nghe sự kiện chuyển màn hình
        // =====================================================================
        Button btnOpenHistory = findViewById(R.id.btnOpenHistory);
        btnOpenHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // =====================================================================
        // 5. BOOTSTRAP: Khởi động luồng nghiệp vụ chính của App
        // Nhiệm vụ: Vừa vào app là phải check quyền GPS ngay để còn gọi API phù hợp
        // =====================================================================
        checkLocationPermissionAndFetchData();
    }

    // =====================================================================
    // VÙNG 1: QUẢN LÝ QUYỀN (PERMISSION HANDLING)
    // =====================================================================

    // Kiểm tra và yêu cầu quyền truy cập vị trí (Runtime Permission)
    private void checkLocationPermissionAndFetchData() {
        // Nếu Android phát hiện app chưa được cấp quyền Vị trí chính xác (ACCESS_FINE_LOCATION)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Hiển thị Popup hệ thống để hỏi xin quyền từ người dùng
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            // Đã có quyền từ những lần mở app trước -> Đi thẳng vào luồng lấy tọa độ
            getLocationAndCallApi();
        }
    }

    // Lắng nghe quyết định của người dùng từ Popup xin quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            // Nếu người dùng bấm "Cho phép" (Allow)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndCallApi(); // Tiếp tục luồng chính
            } else {
                // FALLBACK: Người dùng bấm "Từ chối" (Deny)
                // Kịch bản an toàn: Vẫn phải cho khách dùng app, nhưng tải danh sách không có khoảng cách
                Toast.makeText(this, "Bị từ chối GPS, tải danh sách mặc định", Toast.LENGTH_SHORT).show();
                fetchAllHotels();
            }
        }
    }

    // =====================================================================
    // VÙNG 2: TÍNH TOÁN TỌA ĐỘ VÀ GỌI API (CORE LOGIC)
    // =====================================================================

    // Trích xuất GPS hiện tại và quyết định luồng gọi API
    private void getLocationAndCallApi() {
        // Check bảo mật bắt buộc của Android: Đảm bảo quyền vẫn còn trước khi chọc vào phần cứng
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Kêu gọi Google Play Services lấy vị trí cuối cùng được ghi nhận
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                // LUỒNG CHÍNH (HAPPY PATH): Lấy thành công tọa độ thực tế
                double lat = location.getLatitude();
                double lng = location.getLongitude();

                // Bắn tọa độ này lên Backend Python để Server tính toán "Đường chim bay"
                fetchNearbyHotels(lat, lng);
            } else {
                // LUỒNG PHỤ (FALLBACK PATH): Trả về null
                // Nguyên nhân: Máy có cấp quyền app nhưng người dùng đang TẮT định vị GPS của điện thoại
                Toast.makeText(this, "Không tìm thấy GPS, đang tải danh sách mặc định", Toast.LENGTH_SHORT).show();
                fetchAllHotels(); // Tải danh sách chay không cần GPS
            }
        });
    }

    // =====================================================================
    // VÙNG 3: GIAO TIẾP MẠNG (NETWORK REQUESTS)
    // =====================================================================

    // Gọi API động: Truyền GPS để lấy danh sách khách sạn CÓ tính khoảng cách
    private void fetchNearbyHotels(double lat, double lng) {
        apiService.getNearbyHotels(lat, lng).enqueue(new Callback<List<Hotel>>() {
            @Override
            public void onResponse(Call<List<Hotel>> call, Response<List<Hotel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Truyền thêm lat/lng vào Adapter để lát nữa bấm nút "Chỉ đường" nó còn có gốc để vẽ
                    HotelAdapter adapter = new HotelAdapter(response.body(), lat, lng);
                    rv.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<Hotel>> call, Throwable t) {
                Log.e("API_ERR", "Lỗi mạng: " + t.getMessage());
            }
        });
    }

    // Gọi API tĩnh (Dự phòng): Lấy danh sách khách sạn KHÔNG có khoảng cách
    private void fetchAllHotels() {
        apiService.getHotels().enqueue(new Callback<List<Hotel>>() {
            @Override
            public void onResponse(Call<List<Hotel>> call, Response<List<Hotel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Truyền 0.0 vào tọa độ hiện tại, Adapter sẽ tự hiểu là mất định vị và ẩn dòng "Cách đây..."
                    HotelAdapter adapter = new HotelAdapter(response.body(), 0.0, 0.0);
                    rv.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<Hotel>> call, Throwable t) {
                Log.e("API_ERR", "Lỗi mạng: " + t.getMessage());
            }
        });
    }
}