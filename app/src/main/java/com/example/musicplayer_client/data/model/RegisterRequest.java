package com.example.musicplayer_client.data.model;

public class RegisterRequest {
    public String username;
    public String password;
    public String fullname;
    public String phone;
    public String gender;
    public String birthday;
    public RegisterRequest(String username, String password, String fullname, String phone, String gender, String birthday) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.phone = phone;
        this.gender = gender;
        this.birthday = birthday;
    }
} 