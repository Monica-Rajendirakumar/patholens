package com.example.patholens.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {

    private static final String PREF_NAME = "PatholensPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_AGE = "userAge";
    private static final String KEY_USER_GENDER = "userGender";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Save login data (used by LoginActivity)
    public void saveLoginData(String token, int userId, String userName, String userEmail) {
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    // Alias for saveLoginData (for compatibility with RegistrationActivity)
    public void saveUserSession(String token, int userId, String userName, String userEmail) {
        saveLoginData(token, userId, userName, userEmail);
    }

    // Save complete user profile data
    public void saveUserProfile(String name, String email, int age, String gender, String phoneNumber) {
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putInt(KEY_USER_AGE, age);
        editor.putString(KEY_USER_GENDER, gender);
        editor.putString(KEY_USER_PHONE, phoneNumber);
        editor.apply();
    }

    // Get token
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // Add this method to check if user just registered
    public void setNewUser(boolean isNew) {
        prefs.edit().putBoolean("is_new_user", isNew).apply();
    }

    public boolean isNewUser() {
        boolean isNew = prefs.getBoolean("is_new_user", false);
        // Clear the flag after reading it once
        if (isNew) {
            prefs.edit().putBoolean("is_new_user", false).apply();
        }
        return isNew;
    }

    // Get user ID
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    // Get user name
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    // Get user email
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    // Get user age
    public int getUserAge() {
        return prefs.getInt(KEY_USER_AGE, 0);
    }

    // Get user gender
    public String getUserGender() {
        return prefs.getString(KEY_USER_GENDER, null);
    }

    // Get user phone
    public String getUserPhone() {
        return prefs.getString(KEY_USER_PHONE, null);
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Clear session (logout)
    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
    // Alias for clearSession (for compatibility)
    public void clearData() {
        clearSession();
    }

    // Get Bearer token for API calls
    public String getBearerToken() {
        String token = getToken();
        return token != null ? "Bearer " + token : null;
    }

}