package com.example.musicplayer_client.data.network;

import com.example.musicplayer_client.data.model.LoginRequest;
import com.example.musicplayer_client.data.model.RegisterRequest;
import com.example.musicplayer_client.data.model.ApiResponse;
import com.example.musicplayer_client.data.model.SongItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/api/auth/register")
    Call<ApiResponse> register(@Body RegisterRequest request);

    @POST("/api/auth/login")
    Call<ApiResponse> login(@Body LoginRequest request);

    @GET("/api/songs")
    Call<ApiResponse<List<SongItem>>> getSongList();

    @GET("/api/songs/{id}")
    Call<ApiResponse<SongItem>> getSongDetail(@Path("id") long id);

    @GET("/api/songs/search")
    Call<ApiResponse<List<SongItem>>> searchSongs(
        @Query("keyword") String keyword,
        @Query("page") int page,
        @Query("size") int size
    );
} 