package com.example.patholens.modules;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private Data data;

    @SerializedName("errors")
    private Object errors;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Data getData() {
        return data;
    }

    public Object getErrors() {
        return errors;
    }

    public static class Data {
        @SerializedName("user")
        private User user;

        @SerializedName("token")
        private String token;

        @SerializedName("token_type")
        private String tokenType;

        public User getUser() {
            return user;
        }

        public String getToken() {
            return token;
        }

        public String getTokenType() {
            return tokenType;
        }
    }

    public static class User {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("email")
        private String email;

        @SerializedName("age")
        private int age;

        @SerializedName("gender")
        private String gender;

        @SerializedName("phone_number")
        private String phoneNumber;

        @SerializedName("created_at")
        private String createdAt;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public int getAge() {
            return age;
        }

        public String getGender() {
            return gender;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }
}