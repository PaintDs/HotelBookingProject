package com.example.hotelbookingapp.Model;

public class LoginResponse {
    private String access_token;
    private String token_type;
    private String full_name;

    public String getAccessToken() { return access_token; }
    public String getFullName() { return full_name; }
}