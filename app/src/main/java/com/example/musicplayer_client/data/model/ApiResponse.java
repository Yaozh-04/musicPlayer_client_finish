package com.example.musicplayer_client.data.model;

public class ApiResponse<T> {
    public boolean success;
    public String message;
    public T data;
} 