package com.example.musicplayer_client.data.network;

import com.example.musicplayer_client.data.model.LoginRequest;
import com.example.musicplayer_client.data.model.RegisterRequest;
import com.example.musicplayer_client.data.model.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/auth/register")
    Call<ApiResponse> register(@Body RegisterRequest request);

    @POST("/api/auth/login")
    Call<ApiResponse> login(@Body LoginRequest request);
} 