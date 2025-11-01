package com.example.patholens.modules;

import com.google.gson.annotations.SerializedName;

public class PatientRequest {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("patient_name")
    private String patientName;

    @SerializedName("age")
    private String age;

    @SerializedName("gender")
    private String gender;

    @SerializedName("contact_number")
    private String contactNumber;

    @SerializedName("result")
    private String result;

    @SerializedName("confidence")
    private double confidence;

    // Constructor
    public PatientRequest(String userId, String patientName, String age,
                          String gender, String contactNumber, String result, double confidence) {
        this.userId = userId;
        this.patientName = patientName;
        this.age = age;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.result = result;
        this.confidence = confidence;
    }

    // Getters and Setters
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

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
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
}