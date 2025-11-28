package com.example.patholens.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PrefsManager {

    private static final String TAG = "PrefsManager";
    private static final String PREF_NAME = "PatholensPrefs";

    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_AGE = "userAge";
    private static final String KEY_USER_GENDER = "userGender";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_IS_NEW_USER = "is_new_user";

    private static final int DEFAULT_USER_ID = -1;
    private static final int DEFAULT_AGE = 0;

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveLoginData(String token, int userId, String userName, String userEmail) {
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();

        Log.d(TAG, "Login data saved - UserID: " + userId + ", Token length: " +
                (token != null ? token.length() : 0));
    }

    public void saveUserSession(String token, int userId, String userName, String userEmail) {
        saveLoginData(token, userId, userName, userEmail);
    }

    public void saveUserProfile(String name, String email, int age, String gender, String phoneNumber) {
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putInt(KEY_USER_AGE, age);
        editor.putString(KEY_USER_GENDER, gender);
        editor.putString(KEY_USER_PHONE, phoneNumber);
        editor.apply();
    }

    public String getToken() {
        String token = prefs.getString(KEY_TOKEN, null);
        Log.d(TAG, "Retrieved token: " + (token != null ? "exists (length: " + token.length() + ")" : "null"));
        return token;
    }

    public String getAuthToken() {
        return getToken();
    }

    public String getBearerToken() {
        String token = getToken();
        return token != null ? "Bearer " + token : null;
    }

    public int getUserId() {
        int userId = prefs.getInt(KEY_USER_ID, DEFAULT_USER_ID);
        Log.d(TAG, "Retrieved user ID: " + userId);
        return userId;
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public int getUserAge() {
        return prefs.getInt(KEY_USER_AGE, DEFAULT_AGE);
    }

    public String getUserGender() {
        return prefs.getString(KEY_USER_GENDER, null);
    }

    public String getUserPhone() {
        return prefs.getString(KEY_USER_PHONE, null);
    }

    public boolean isLoggedIn() {
        boolean loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        Log.d(TAG, "Is logged in: " + loggedIn);
        return loggedIn;
    }

    public void setNewUser(boolean isNew) {
        editor.putBoolean(KEY_IS_NEW_USER, isNew);
        editor.apply();
    }

    public boolean isNewUser() {
        boolean isNew = prefs.getBoolean(KEY_IS_NEW_USER, false);
        if (isNew) {
            editor.putBoolean(KEY_IS_NEW_USER, false);
            editor.apply();
        }
        return isNew;
    }

    public void clearSession() {
        Log.d(TAG, "Clearing session data");
        editor.clear();
        editor.apply();
    }

    public void clearLoginData() {
        clearSession();
    }

    public void clearAll() {
        Log.d(TAG, "Clearing all data");
        editor.clear();
        editor.apply();
    }

    public void clearData() {
        clearSession();
    }

    public void debugPrintAll() {
        Log.d(TAG, "=== PrefsManager Debug ===");
        Log.d(TAG, "Is Logged In: " + isLoggedIn());
        Log.d(TAG, "User ID: " + getUserId());
        Log.d(TAG, "User Name: " + getUserName());
        Log.d(TAG, "User Email: " + getUserEmail());
        Log.d(TAG, "User Age: " + getUserAge());
        Log.d(TAG, "User Gender: " + getUserGender());
        Log.d(TAG, "User Phone: " + getUserPhone());
        Log.d(TAG, "Token exists: " + (getToken() != null));
        Log.d(TAG, "========================");
    }
}