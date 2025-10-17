package com.example.patholens.modules;

import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private UserData data;

    @SerializedName("errors")
    private Object errors;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public UserData getData() {
        return data;
    }

    public Object getErrors() {
        return errors;
    }

    public static class UserData {
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
    }
}