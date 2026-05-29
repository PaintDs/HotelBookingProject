package com.example.hotelbookingapp.API;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {

           // Tạo OkHttpClient để tự động chèn Header bypass Ngrok cho mọi Request
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                // Ép Ngrok trả về JSON thẳng cho App thay vì hiện trang web cảnh báo
                                .header("ngrok-skip-browser-warning", "true")
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            // Khởi tạo Retrofit tích hợp OkHttpClient sạch dữ liệu
            retrofit = new Retrofit.Builder()
                    .baseUrl(MyConfig.BASE_URL)
                    .client(okHttpClient) // 🌟 FIX: Ép Retrofit chạy qua cấu hình cấu hình bypass ở trên
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}