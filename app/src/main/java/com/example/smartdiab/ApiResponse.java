package com.example.smartdiab;

public class ApiResponse {

    private String meal;
    private int carbs;
    private String verdict;
    private String ai_analysis;

    public String getMeal() {
        return meal;
    }

    public int getCarbs() {
        return carbs;
    }

    public String getVerdict() {
        return verdict;
    }

    public String getAi_analysis() {
        return ai_analysis;
    }
}