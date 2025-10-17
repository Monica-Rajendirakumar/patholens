package com.example.patholens.api;

import com.example.patholens.modules.LoginRequest;
import com.example.patholens.modules.LoginResponse;
import com.example.patholens.modules.RegisterRequest;
import com.example.patholens.modules.AuthResponse;
import com.example.patholens.modules.UserResponse;
import com.example.patholens.modules.UpdateUserRequest;
import com.example.patholens.modules.NewsResponse;
import com.example.patholens.modules.ProfileImageResponse;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;  // ADD THIS LINE
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;  // ADD THIS LINE
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;  // ADD THIS LINE
import retrofit2.http.Path;

public interface ApiService {

    // Login endpoint
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @GET("user/{id}")
    Call<UserResponse> getUser(@Path("id") int userId, @Header("Authorization") String token);

    @GET("me")
    Call<UserResponse> getAuthenticatedUser(@Header("Authorization") String token);

    @PUT("user/{id}")
    Call<UserResponse> updateUser(
            @Path("id") int userId,
            @Header("Authorization") String token,
            @Body UpdateUserRequest request
    );

    @PUT("me")
    Call<UserResponse> updateAuthenticatedUser(
            @Header("Authorization") String token,
            @Body UpdateUserRequest request
    );

    @GET("me/image")
    Call<ProfileImageResponse> getProfileImage(@Header("Authorization") String token);

    @Multipart
    @POST("me/image")
    Call<ProfileImageResponse> uploadProfileImage(
            @Header("Authorization") String token,
            @Part MultipartBody.Part image
    );

    @DELETE("me/image")
    Call<ProfileImageResponse> deleteProfileImage(@Header("Authorization") String token);

    @GET("news")
    Call<NewsResponse> getNews();
}