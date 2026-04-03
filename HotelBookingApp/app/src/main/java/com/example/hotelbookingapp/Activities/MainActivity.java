package com.example.hotelbookingapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.List;

import com.example.hotelbookingapp.Adapter.HotelAdapter;
import com.example.hotelbookingapp.Model.Hotel;
import com.google.android.material.button.MaterialButton;

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

    // --- BIẾN MỚI CHO TÍNH NĂNG FILTER ---
    private List<Hotel> masterHotelList = new ArrayList<>(); // Biến giữ danh sách gốc 30 cái
    private double currentLat = 0.0;
    private double currentLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv = findViewById(R.id.rvHotels);
        rv.setLayoutManager(new LinearLayoutManager(this));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MyConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // --- GẮN SỰ KIỆN CHO CÁC NÚT BẤM KHU VỰC ---
        setupFilterButtons();

        checkLocationPermissionAndFetchData();
    }

    // =====================================================================
    // --- TÍNH NĂNG MỚI: LOGIC LỌC DANH SÁCH (CLIENT-SIDE FILTERING) ---
    // =====================================================================
    private void setupFilterButtons() {
        findViewById(R.id.btnAll).setOnClickListener(v -> filterByDistrict("")); // Chuỗi rỗng là lấy tất cả
        findViewById(R.id.btnHoanKiem).setOnClickListener(v -> filterByDistrict("Hoàn Kiếm"));
        findViewById(R.id.btnBaDinh).setOnClickListener(v -> filterByDistrict("Ba Đình"));
        findViewById(R.id.btnHaiBaTrung).setOnClickListener(v -> filterByDistrict("Hai Bà Trưng"));
        findViewById(R.id.btnDongDa).setOnClickListener(v -> filterByDistrict("Đống Đa"));
        findViewById(R.id.btnCauGiay).setOnClickListener(v -> filterByDistrict("Cầu Giấy"));
        findViewById(R.id.btnTayHo).setOnClickListener(v -> filterByDistrict("Tây Hồ"));
    }

    private void filterByDistrict(String districtName) {
        if (masterHotelList == null || masterHotelList.isEmpty()) {
            Toast.makeText(this, "Dữ liệu chưa tải xong, vui lòng đợi!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Hotel> filteredList = new ArrayList<>();
        String search = removeAccent(districtName).trim(); // Dùng hàm không dấu nãy mình gửi

        for (Hotel hotel : masterHotelList) {
            String addrOrig = hotel.getAddress();
            String addrNoAccent = removeAccent(addrOrig);

            // LOG NÀY SẼ CHO BẠN BIẾT SỰ THẬT:
            Log.d("DEBUG_FILTER", "Đang tìm: [" + search + "] trong địa chỉ: [" + addrNoAccent + "]");

            if (search.isEmpty() || addrNoAccent.contains(search)) {
                filteredList.add(hotel);
            }
        }

        HotelAdapter adapter = new HotelAdapter(filteredList, currentLat, currentLng);
        rv.setAdapter(adapter);

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy khách sạn nào!", Toast.LENGTH_SHORT).show();
        }
    }

    // =====================================================================
    // VÙNG QUẢN LÝ QUYỀN VÀ GỌI API (Đã tối ưu để lưu danh sách gốc)
    // =====================================================================
    private void checkLocationPermissionAndFetchData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            getLocationAndCallApi();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndCallApi();
            } else {
                Toast.makeText(this, "Bị từ chối GPS, tải danh sách mặc định", Toast.LENGTH_SHORT).show();
                fetchAllHotels();
            }
        }
    }

    private void getLocationAndCallApi() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                fetchNearbyHotels(currentLat, currentLng);
            } else {
                fetchAllHotels();
            }
        });
    }

    private void fetchNearbyHotels(double lat, double lng) {
        apiService.getNearbyHotels(lat, lng).enqueue(new Callback<List<Hotel>>() {
            @Override
            public void onResponse(Call<List<Hotel>> call, Response<List<Hotel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    masterHotelList = response.body(); // LƯU VÀO BIẾN GỐC
                    filterByDistrict(""); // Mặc định hiển thị tất cả
                }
            }
            @Override
            public void onFailure(Call<List<Hotel>> call, Throwable t) {}
        });
    }

    private void fetchAllHotels() {
        apiService.getHotels().enqueue(new Callback<List<Hotel>>() {
            @Override
            public void onResponse(Call<List<Hotel>> call, Response<List<Hotel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    masterHotelList = response.body(); // LƯU VÀO BIẾN GỐC
                    currentLat = 0.0; currentLng = 0.0;
                    filterByDistrict(""); // Mặc định hiển thị tất cả
                }
            }
            @Override
            public void onFailure(Call<List<Hotel>> call, Throwable t) {}
        });
    }

    // =====================================================================
    // MENU BÊN TRÊN
    // =====================================================================
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_history) {
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            return true;
        } else if (id == R.id.menu_about) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Về ứng dụng")
                    .setMessage("Hotel Booking App v1.0\nPhát triển bởi [NgVanKhai]")
                    .setPositiveButton("Đóng", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // Hàm siêu đẳng: Biến "Tây Hồ" thành "tay ho", "Cầu Giấy" thành "cau giay"
    // Hàm chuyển tiếng Việt có dấu thành không dấu
    public String removeAccent(String s) {
        if (s == null) return "";
        String temp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").toLowerCase().replaceAll("đ", "d");
    }
    private void updateButtonStyles(Button selectedButton) {
        // 1. Danh sách tất cả ID các nút menu của bạn
        int[] btnIds = {R.id.btnAll, R.id.btnHoanKiem, R.id.btnBaDinh, R.id.btnTayHo, R.id.btnCauGiay, R.id.btnDongDa};

        for (int id : btnIds) {
            MaterialButton btn = findViewById(id);
            if (btn == selectedButton) {
                // Nút đang chọn: Nền xanh, chữ trắng
                btn.setBackgroundColor(Color.parseColor("#4CAF50"));
                btn.setTextColor(Color.WHITE);
                btn.setStrokeColor(null);
            } else {
                // Nút không chọn: Nền trắng, viền xám, chữ xám
                btn.setBackgroundColor(Color.TRANSPARENT);
                btn.setTextColor(Color.parseColor("#555555"));
                btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#DDDDDD")));
            }
        }
    }
    private void updateMenuUI(MaterialButton selectedBtn) {
        // Danh sách ID các nút
        int[] ids = {R.id.btnAll, R.id.btnHoanKiem, R.id.btnBaDinh, R.id.btnTayHo};

        for (int id : ids) {
            MaterialButton btn = findViewById(id);
            if (btn == selectedBtn) {
                // Nút được chọn: Đổ nền xanh đặc, chữ trắng, mất viền
                btn.setBackgroundColor(Color.parseColor("#4CAF50"));
                btn.setTextColor(Color.WHITE);
                btn.setStrokeWidth(0);
            } else {
                // Nút không chọn: Nền trắng, viền xám mảnh, chữ xám
                btn.setBackgroundColor(Color.TRANSPARENT);
                btn.setTextColor(Color.parseColor("#757575"));
                btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#DDDDDD")));
                btn.setStrokeWidth(2); // Độ dày viền
            }
        }
    }
}