package com.example.patholens.modules;

import com.google.gson.annotations.SerializedName;

public class Patient {

    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("patient_name")
    private String patientName;

    @SerializedName("age")
    private int age;

    @SerializedName("gender")
    private String gender;

    @SerializedName("contact_number")
    private String contactNumber;

    @SerializedName("diagnosising_image")
    private String diagnosisingImage;

    @SerializedName("result")
    private String result;

    @SerializedName("confidence")
    private double confidence;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Constructors
    public Patient() {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getDiagnosisingImage() {
        return diagnosisingImage;
    }

    public void setDiagnosisingImage(String diagnosisingImage) {
        this.diagnosisingImage = diagnosisingImage;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper method to check if result indicates pemphigus
    public boolean isPemphigus() {
        return result != null &&
                (result.equalsIgnoreCase("pemphigus") ||
                        result.equalsIgnoreCase("Pemphigus Detected") ||
                        result.toLowerCase().contains("pemphigus"));
    }

    // Format date for display (simplified)
    public String getFormattedDate() {
        if (createdAt == null) return "N/A";

        try {
            // Laravel returns: "2025-06-14T10:30:00.000000Z"
            // We want: "14/06/25"
            String[] parts = createdAt.split("T")[0].split("-");
            if (parts.length == 3) {
                String year = parts[0].substring(2); // Get last 2 digits of year
                String month = parts[1];
                String day = parts[2];
                return day + "/" + month + "/" + year;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return createdAt;
    }
}