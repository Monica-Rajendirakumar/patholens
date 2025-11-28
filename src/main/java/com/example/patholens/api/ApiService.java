package com.example.patholens.api;

import com.example.patholens.modules.LoginRequest;
import com.example.patholens.modules.LoginResponse;
import com.example.patholens.modules.RegisterRequest;
import com.example.patholens.modules.AuthResponse;
import com.example.patholens.modules.UserResponse;
import com.example.patholens.modules.UpdateUserRequest;
import com.example.patholens.modules.NewsResponse;
import com.example.patholens.modules.ProfileImageResponse;
import com.example.patholens.modules.PatientRequest;
import com.example.patholens.modules.PatientResponse;
import com.example.patholens.modules.VerifyOtpResponse;
import com.example.patholens.modules.SendOtpRequest;
import com.example.patholens.modules.ResetPasswordResponse;
import com.example.patholens.modules.OtpResponse;
import com.example.patholens.modules.ResetPasswordRequest;
import com.example.patholens.modules.VerifyOtpRequest;
import com.example.patholens.modules.ChangePasswordResponse;
import com.example.patholens.modules.ChangePasswordRequest;


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
import okhttp3.RequestBody;

public interface ApiService {

    // Login endpoint
    @POST("v1/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("v1/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @GET("v1/user/{id}")
    Call<UserResponse> getUser(@Path("id") int userId, @Header("Authorization") String token);

    @GET("v1/me")
    Call<UserResponse> getAuthenticatedUser(@Header("Authorization") String token);

    @PUT("v1/user/{id}")
    Call<UserResponse> updateUser(
            @Path("id") int userId,
            @Header("Authorization") String token,
            @Body UpdateUserRequest request
    );

    @PUT("v1/me")
    Call<UserResponse> updateAuthenticatedUser(
            @Header("Authorization") String token,
            @Body UpdateUserRequest request
    );

    @GET("v1/me/image")
    Call<ProfileImageResponse> getProfileImage(@Header("Authorization") String token);

    @Multipart
    @POST("v1/me/image")
    Call<ProfileImageResponse> uploadProfileImage(
            @Header("Authorization") String token,
            @Part MultipartBody.Part image
    );
    // Store patient diagnosis with image
    @Multipart
    @POST("v1/patients")
    Call<PatientResponse> storePatientWithImage(
            @Header("Authorization") String token,
            @Part("user_id") RequestBody userId,
            @Part("patient_name") RequestBody patientName,
            @Part("age") RequestBody age,
            @Part("gender") RequestBody gender,
            @Part("contact_number") RequestBody contactNumber,
            @Part("result") RequestBody result,
            @Part("confidence") RequestBody confidence,
            @Part MultipartBody.Part diagnosingImage
    );

    // Store patient diagnosis without image
    @POST("v1/patients")
    Call<PatientResponse> storePatient(
            @Header("Authorization") String token,
            @Body PatientRequest request
    );

    // Get all patients
    @GET("v1/patients")
    Call<PatientResponse> getAllPatients(@Header("Authorization") String token);

    // Get patient history for specific user
    @GET("v1/patients/user/{user_id}")
    Call<PatientResponse> getUserPatientHistory(
            @Path("user_id") String userId,
            @Header("Authorization") String token
    );

    // Get specific patient record
    @GET("v1/patients/{id}")
    Call<PatientResponse> getPatient(
            @Path("id") int patientId,
            @Header("Authorization") String token
    );

    // Update patient record
    @PUT("v1/patients/{id}")
    Call<PatientResponse> updatePatient(
            @Path("id") int patientId,
            @Header("Authorization") String token,
            @Body PatientRequest request
    );

    // Delete patient record
    @DELETE("v1/patients/{id}")
    Call<PatientResponse> deletePatient(
            @Path("id") int patientId,
            @Header("Authorization") String token
    );

    // Add these methods to your existing ApiService interface

    @POST("password/forgot")
    Call<OtpResponse> sendOtp(@Body SendOtpRequest request);

    @POST("password/verify-otp")
    Call<VerifyOtpResponse> verifyOtp(@Body VerifyOtpRequest request);

    @POST("password/reset")
    Call<ResetPasswordResponse> resetPassword(@Body ResetPasswordRequest request);

    @POST("password/resend-otp")
    Call<OtpResponse> resendOtp(@Body SendOtpRequest request);

    @POST("v1/auth/change-password")
    Call<ChangePasswordResponse> changePassword(
            @Header("Authorization") String token,
            @Body ChangePasswordRequest request
    );


    @DELETE("v1/me/image")
    Call<ProfileImageResponse> deleteProfileImage(@Header("Authorization") String token);

    @GET("v1/news")
    Call<NewsResponse> getNews();
}