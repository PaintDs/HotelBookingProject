package com.example.hotelbookingapp.Activities;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelbookingapp.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapsActivity extends AppCompatActivity {

    private MapView map = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load cấu hình OSM
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_maps);

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(14.0);

        // 1. Tọa độ trung tâm ban đầu (Hà Nội)
        GeoPoint startPoint = new GeoPoint(21.0285, 105.8542);
        mapController.setCenter(startPoint);

        // 2. Hiển thị vị trí người dùng
        MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        map.getOverlays().add(locationOverlay);

        // 3. GỌI API LẤY TOÀN BỘ KHÁCH SẠN VÀ VẼ MARKER
        fetchAllHotelsFromAPI();
    }

    private void fetchAllHotelsFromAPI() {
        // Chạy ngầm (Background Thread) để không làm đơ App khi gọi mạng
        new Thread(() -> {
            try {
                // CHÚ Ý: Đảm bảo IP này khớp với IP máy tính chạy main.py (cổng 8000)
                URL url = new URL("http://192.168.100.116:8000/hotels");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Chuyển chuỗi JSON trả về thành mảng JSONArray
                JSONArray jsonArray = new JSONArray(response.toString());

                // Đẩy dữ liệu lên giao diện (Main Thread)
                runOnUiThread(() -> {
                    // DÙNG VÒNG LẶP ĐỂ VẼ TẤT CẢ KHÁCH SẠN
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject hotel = jsonArray.getJSONObject(i);

                            // Lấy dữ liệu từ API
                            double lat = hotel.getDouble("lat");
                            double lng = hotel.getDouble("lng");
                            String name = hotel.getString("name");
                            int price = hotel.getInt("price_per_night");

                            // Chỉ vẽ nếu tọa độ hợp lệ (khác 0)
                            if (lat != 0 && lng != 0) {
                                addHotelMarker(lat, lng, name, "Giá: " + price + " VNĐ");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // LỆNH QUAN TRỌNG: Vẽ lại bản đồ sau khi cắm hết Marker
                    map.invalidate();
                });

            } catch (Exception e) {
                Log.e("MAP_ERROR", "Lỗi lấy dữ liệu API: " + e.getMessage());
            }
        }).start();
    }

    private void addHotelMarker(double lat, double lng, String name, String price) {
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