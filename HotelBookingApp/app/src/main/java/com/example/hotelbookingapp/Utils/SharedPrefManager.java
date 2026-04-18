package com.example.hotelbookingapp.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "hotel_booking_prefs";
    private static final String KEY_TOKEN = "key_token";
    private static final String KEY_FULL_NAME = "key_full_name";
    private static final String KEY_EMAIL = "key_email";

    private SharedPreferences sharedPreferences;
    private static SharedPrefManager instance;
    private Context mCtx;

    private SharedPrefManager(Context context) {
        mCtx = context;
        sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            // Dùng getApplicationContext để an toàn hơn
            instance = new SharedPrefManager(context.getApplicationContext());
        }
        return instance;
    }

    // LƯU THÔNG TIN NGƯỜI DÙNG
    public void saveUser(String token, String fullName, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_EMAIL, email);
        // Dùng commit() để đảm bảo dữ liệu được ghi ngay lập tức trước khi chuyển trang
        editor.commit();
    }

    // LẤY EMAIL (Nếu không có trả về chuỗi rỗng)
    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, "");
    }

    public String getFullName() {
        return sharedPreferences.getString(KEY_FULL_NAME, "");
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getString(KEY_TOKEN, null) != null;
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}