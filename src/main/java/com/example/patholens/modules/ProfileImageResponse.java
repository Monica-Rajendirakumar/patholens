package com.example.patholens.modules;

import com.google.gson.annotations.SerializedName;

public class ProfileImageResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private ProfileImageData data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ProfileImageData getData() {
        return data;
    }

    public static class ProfileImageData {
        @SerializedName("profile_image_url")
        private String profileImageUrl;

        @SerializedName("updated_at")
        private String updatedAt;

        public String getProfileImageUrl() {
            return profileImageUrl;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }
    }
}