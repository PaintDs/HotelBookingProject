package com.example.hotelbookingapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.hotelbookingapp.API.ApiService;
import com.example.hotelbookingapp.API.MyConfig;
import com.example.hotelbookingapp.Adapter.HotelAdapter;
import com.example.hotelbookingapp.Model.Hotel;
import com.example.hotelbookingapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    // 1. Biến nhớ quận đang chọn (Quan trọng để giữ trạng thái khi reload)
    private String currentSelectedDistrict = "";

    private ApiService apiService;
    private RecyclerView rv;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FusedLocationProviderClient fusedLocationClient;

    private List<Hotel> masterHotelList = new ArrayList<>();
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    private static final int LOCATION_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ UI
        rv = findViewById(R.id.rvHotels);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Cấu hình SwipeRefresh
        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#4CAF50"), Color.BLUE);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Khi vuốt làm mới, giữ nguyên quận đang chọn nhưng tải lại data mới nhất
            checkLocationPermissionAndFetchData();
        });

        // Network & GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MyConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        setupFilterButtons();

        // Mặc định làm sáng nút "Tất cả" khi mới vào App
        updateMenuUI(findViewById(R.id.btnAll));

        checkLocationPermissionAndFetchData();
    }

    // =====================================================================
    // XỬ LÝ BỘ LỌC (Cập nhật để lưu trạng thái)
    // =====================================================================
    private void setupFilterButtons() {
        // Mỗi khi click, gán giá trị vào currentSelectedDistrict để khi reload App sẽ nhớ
        findViewById(R.id.btnAll).setOnClickListener(v -> handleFilterClick("", (MaterialButton) v));
        findViewById(R.id.btnHoanKiem).setOnClickListener(v -> handleFilterClick("Hoàn Kiếm", (MaterialButton) v));
        findViewById(R.id.btnBaDinh).setOnClickListener(v -> handleFilterClick("Ba Đình", (MaterialButton) v));
        findViewById(R.id.btnHaiBaTrung).setOnClickListener(v -> handleFilterClick("Hai Bà Trưng", (MaterialButton) v));
        findViewById(R.id.btnDongDa).setOnClickListener(v -> handleFilterClick("Đống Đa", (MaterialButton) v));
        findViewById(R.id.btnCauGiay).setOnClickListener(v -> handleFilterClick("Cầu Giấy", (MaterialButton) v));
        findViewById(R.id.btnTayHo).setOnClickListener(v -> handleFilterClick("Tây Hồ", (MaterialButton) v));
    }

    private void handleFilterClick(String district, MaterialButton btn) {
        currentSelectedDistrict = district; // Ghi nhớ quận
        filterByDistrict(district);        // Lọc danh sách
        updateMenuUI(btn);                 // Đổi màu nút
    }

    private void filterByDistrict(String districtName) {
        if (masterHotelList == null || masterHotelList.isEmpty()) return;

        List<Hotel> filteredList = new ArrayList<>();
        String search = removeAccent(districtName).trim();

        for (Hotel hotel : masterHotelList) {
            String addrNoAccent = removeAccent(hotel.getAddress());
            if (search.isEmpty() || addrNoAccent.contains(search)) {
                filteredList.add(hotel);
            }
        }

        HotelAdapter adapter = new HotelAdapter(filteredList, currentLat, currentLng);
        rv.setAdapter(adapter);
    }

    // =====================================================================
    // QUẢN LÝ GỌI API (Sửa để nhớ Filter sau khi Reload)
    // =====================================================================
    private void fetchNearbyHotels(double lat, double lng) {
        swipeRefreshLayout.setRefreshing(true);
        apiService.getNearbyHotels(lat, lng).enqueue(new Callback<List<Hotel>>() {
            @Override
            public void onResponse(Call<List<Hotel>> call, Response<List<Hotel>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    masterHotelList = response.body();

                    // THAY ĐỔI: Thay vì mặc định "", dùng biến currentSelectedDistrict
                    filterByDistrict(currentSelectedDistrict);
                }
            }
            @Override
            public void onFailure(Call<List<Hotel>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Lỗi kết nối Server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAllHotels() {
        swipeRefreshLayout.setRefreshing(true);
        apiService.getHotels().enqueue(new Callback<List<Hotel>>() {
            @Override
            public void onResponse(Call<List<Hotel>> call, Response<List<Hotel>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    masterHotelList = response.body();
                    currentLat = 0.0; currentLng = 0.0;

                    // THAY ĐỔI: Tự động lọc lại quận cũ sau khi tải xong data
                    filterByDistrict(currentSelectedDistrict);
                }
            }
            @Override
            public void onFailure(Call<List<Hotel>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Lỗi kết nối Server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- (Phần GPS, Menu, removeAccent giữ nguyên phía dưới) ---

    private void checkLocationPermissionAndFetchData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            getLocationAndCallApi();
        }
    }

    private void getLocationAndCallApi() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        swipeRefreshLayout.setRefreshing(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                fetchNearbyHotels(currentLat, currentLng);
            } else {
                fetchAllHotels();
            }
        }).addOnFailureListener(e -> fetchAllHotels());
    }

    public String removeAccent(String s) {
        if (s == null) return "";
        String temp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").toLowerCase().replaceAll("đ", "d");
    }

    private void updateMenuUI(MaterialButton selectedBtn) {
        int[] ids = {R.id.btnAll, R.id.btnHoanKiem, R.id.btnBaDinh, R.id.btnTayHo, R.id.btnCauGiay, R.id.btnDongDa, R.id.btnHaiBaTrung};
        for (int id : ids) {
            MaterialButton btn = findViewById(id);
            if (btn == null) continue;
            if (btn == selectedBtn) {
                btn.setBackgroundColor(Color.parseColor("#4CAF50"));
                btn.setTextColor(Color.WHITE);
                btn.setStrokeWidth(0);
            } else {
                btn.setBackgroundColor(Color.TRANSPARENT);
                btn.setTextColor(Color.parseColor("#757575"));
                btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#DDDDDD")));
                btn.setStrokeWidth(2);
            }
        }
    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndCallApi();
            } else {
                fetchAllHotels();
            }
        }
    }
}