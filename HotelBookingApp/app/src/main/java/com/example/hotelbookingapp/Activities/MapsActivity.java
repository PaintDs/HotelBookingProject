package com.example.hotelbookingapp.Activities;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

// Thêm import cấu hình API và Retrofit Client của bạn
import com.example.hotelbookingapp.API.ApiService;
import com.example.hotelbookingapp.API.RetrofitClient;
import com.example.hotelbookingapp.Model.Hotel;
import com.example.hotelbookingapp.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity {

    private MapView map = null;
    private ApiService apiService; //Khai báo biến apiService toàn cục ở đây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bổ sung User-Agent định danh bắt buộc để OpenStreetMap không chặn hiển thị khi dùng 5G
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_maps);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(14.0);

        // 1. Tọa độ trung tâm ban đầu cố định tại Hà Nội
        GeoPoint startPoint = new GeoPoint(21.0285, 105.8542);
        mapController.setCenter(startPoint);

        // 2. Hiển thị ghim vị trí hiện tại của người dùng
        MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();

        // Đã tắt lệnh dưới đây để phục vụ đúng luồng "Toàn cảnh".
        // Tránh việc bản đồ tự động giật camera theo GPS của máy làm lệch khỏi trung tâm 30 khách sạn.
        // locationOverlay.enableFollowLocation();

        map.getOverlays().add(locationOverlay);

        //  Khởi tạo apiService chạy qua OkHttpClient bypass Ngrok
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // 3. GỌI API LẤY TOÀN BỘ KHÁCH SẠN VÀ VẼ MARKER
        fetchAllHotelsFromAPI();
    }

    private void fetchAllHotelsFromAPI() {
        // SỬ DỤNG RETROFIT: Tự động chạy luồng ngầm, tự bóc tách GSON an toàn chống OOM
        apiService.getHotels().enqueue(new Callback<List<Hotel>>() {
            @Override
            public void onResponse(Call<List<Hotel>> call, Response<List<Hotel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Hotel> hotels = response.body();

                    // Duyệt mảng vẽ Marker từ danh sách Object sạch
                    for (Hotel hotel : hotels) {
                        double lat = hotel.getLat();
                        double lng = hotel.getLng();
                        String name = hotel.getName();
                        double price = hotel.getPrice_per_night();

                        // Kiểm tra tọa độ hợp lệ trước khi cắm ghim
                        if (lat != 0.0 && lng != 0.0) {
                            addHotelMarker(lat, lng, name, "Giá: " + price + " VNĐ");
                        }
                    }

                    // LỆNH QUAN TRỌNG: Ép OpenStreetMap vẽ lại toàn bộ Marker vừa nạp
                    map.invalidate();
                    Log.d("MAP_SUCCESS", "Đã vẽ thành công toàn bộ khách sạn lên Map!");

                } else {
                    Log.e("MAP_ERROR", "Server trả về lỗi hoặc JSON trống: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Hotel>> call, Throwable t) {
                // Bắt các lỗi đứt mạng, Timeout khi dùng 5G/Wifi trường
                Log.e("MAP_ERROR", "Lỗi kết nối API mạng: " + t.getMessage());
            }
        });
    }

    private void addHotelMarker(double lat, double lng, String name, String price) {
        if (map == null) return;
        Marker hotelMarker = new Marker(map);
        hotelMarker.setPosition(new GeoPoint(lat, lng));
        hotelMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        hotelMarker.setTitle(name);
        hotelMarker.setSnippet(price);

        map.getOverlays().add(hotelMarker);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }
}