package com.example.patholens.api;

import com.example.patholens.modules.LoginRequest;
import com.example.patholens.modules.LoginResponse;
import com.example.patholens.modules.RegisterRequest;
import com.example.patholens.modules.AuthResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // Login endpoint
    @POST("api/login")  // Change this to match your Laravel route
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("api/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    // You can add more API endpoints here later
    // Example:
    // @POST("api/register")
    // Call<RegisterResponse> register(@Body RegisterRequest request);
}