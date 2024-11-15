package com.example.petdoc_app;

public class Disease {
    private int id;
    private String name;
    private String predictionResult;
    private String timestamp;

    // Constructor
    public Disease(int id, String name, String predictionResult, String timestamp) {
        this.id = id;
        this.name = name;
        this.predictionResult = predictionResult;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPredictionResult() {
        return predictionResult;
    }

    public void setPredictionResult(String predictionResult) {
        this.predictionResult = predictionResult;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
