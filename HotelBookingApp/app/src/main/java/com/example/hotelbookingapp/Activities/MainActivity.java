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
import com.example.hotelbookingapp.API.RetrofitClient;
import com.example.hotelbookingapp.Adapter.HotelAdapter;
import com.example.hotelbookingapp.Model.Hotel;
import com.example.hotelbookingapp.R;
import com.example.hotelbookingapp.Utils.SharedPrefManager; 
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
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

        rv = findViewById(R.id.rvHotels);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        rv.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#4CAF50"), Color.BLUE);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            checkLocationPermissionAndFetchData();
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // TỐI ƯU: Sử dụng RetrofitClient thay vì tạo mới Builder
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        setupFilterButtons();
        updateMenuUI(findViewById(R.id.btnAll));
        checkLocationPermissionAndFetchData();
    }

    private void setupFilterButtons() {
        findViewById(R.id.btnAll).setOnClickListener(v -> handleFilterClick("", (MaterialButton) v));
        findViewById(R.id.btnHoanKiem).setOnClickListener(v -> handleFilterClick("Hoàn Kiếm", (MaterialButton) v));
        findViewById(R.id.btnBaDinh).setOnClickListener(v -> handleFilterClick("Ba Đình", (MaterialButton) v));
        findViewById(R.id.btnHaiBaTrung).setOnClickListener(v -> handleFilterClick("Hai Bà Trưng", (MaterialButton) v));
        findViewById(R.id.btnDongDa).setOnClickListener(v -> handleFilterClick("Đống Đa", (MaterialButton) v));
        findViewById(R.id.btnCauGiay).setOnClickListener(v -> handleFilterClick("Cầu Giấy", (MaterialButton) v));
        findViewById(R.id.btnTayHo).setOnClickListener(v -> handleFilterClick("Tây Hồ", (MaterialButton) v));
        
        // Nút mở bản đồ
        findViewById(R.id.fabMap).setOnClickListener(v -> {
             startActivity(new Intent(this, MapsActivity.class));
        });
    }

    private void handleFilterClick(String district, MaterialButton btn) {
        currentSelectedDistrict = district;
        filterByDistrict(district);
        updateMenuUI(btn);
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

    private void fetchNearbyHotels(double lat, double lng) {
        swipeRefreshLayout.setRefreshing(true);
        apiService.getNearbyHotels(lat, lng).enqueue(new Callback<List<Hotel>>() {
            @Override
            public void onResponse(Call<List<Hotel>> call, Response<List<Hotel>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    masterHotelList = response.body();
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
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        android.view.MenuItem accountItem = menu.findItem(R.id.action_account);
        if (accountItem != null) {
            if (SharedPrefManager.getInstance(this).isLoggedIn()) {
                accountItem.setTitle("Đăng xuất (" + SharedPrefManager.getInstance(this).getFullName() + ")");
            } else {
                accountItem.setTitle("Đăng nhập");
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_account) {
            if (SharedPrefManager.getInstance(this).isLoggedIn()) {
                SharedPrefManager.getInstance(this).logout();
                Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
            } else {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
            return true;
        } else if (id == R.id.menu_history) {
            if (!SharedPrefManager.getInstance(this).isLoggedIn()) {
                Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            }
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
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
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