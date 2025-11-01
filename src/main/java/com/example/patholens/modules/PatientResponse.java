package com.example.patholens.modules;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

@JsonAdapter(PatientResponse.PatientResponseDeserializer.class)
public class PatientResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    // This will hold either single Patient or List<Patient>
    private Patient singlePatient;
    private List<Patient> dataList;

    // For old API - list of patients (alternative field)
    @SerializedName("patients")
    private List<Patient> patients;

    // For new API - count field
    @SerializedName("count")
    private Integer count;

    // Main class Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Old API - get single patient data
    public Patient getSinglePatient() {
        return singlePatient;
    }

    public void setSinglePatient(Patient singlePatient) {
        this.singlePatient = singlePatient;
    }

    // New API - get list of patients from "data" field
    public List<Patient> getDataList() {
        return dataList;
    }

    public void setDataList(List<Patient> dataList) {
        this.dataList = dataList;
    }

    // Old API - get list of patients from "patients" field
    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(List<Patient> patients) {
        this.patients = patients;
    }

    // New API - get count
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    // Helper method to get patient list regardless of API version
    public List<Patient> getPatientList() {
        if (dataList != null && !dataList.isEmpty()) {
            return dataList; // New API
        } else if (patients != null && !patients.isEmpty()) {
            return patients; // Old API
        }
        return null;
    }

    // Compatibility method for screens using getData()
    public List<Patient> getData() {
        return getPatientList();
    }

    // Custom Deserializer to handle dynamic "data" field
    public static class PatientResponseDeserializer implements JsonDeserializer<PatientResponse> {
        @Override
        public PatientResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            JsonObject jsonObject = json.getAsJsonObject();
            PatientResponse response = new PatientResponse();

            // Deserialize status and message
            if (jsonObject.has("status")) {
                response.status = jsonObject.get("status").getAsString();
            }
            if (jsonObject.has("message")) {
                response.message = jsonObject.get("message").getAsString();
            }

            // Deserialize count (new API)
            if (jsonObject.has("count")) {
                response.count = jsonObject.get("count").getAsInt();
            }

            // Deserialize patients field (old API)
            if (jsonObject.has("patients")) {
                Type patientListType = new TypeToken<List<Patient>>() {}.getType();
                response.patients = context.deserialize(jsonObject.get("patients"), patientListType);
            }

            // Deserialize data field (can be object or array)
            if (jsonObject.has("data") && !jsonObject.get("data").isJsonNull()) {
                JsonElement dataElement = jsonObject.get("data");

                if (dataElement.isJsonArray()) {
                    // New API - data is an array
                    Type patientListType = new TypeToken<List<Patient>>() {}.getType();
                    response.dataList = context.deserialize(dataElement, patientListType);
                } else if (dataElement.isJsonObject()) {
                    // Old API - data is a single object
                    response.singlePatient = context.deserialize(dataElement, Patient.class);
                }
            }

            return response;
        }
    }
}