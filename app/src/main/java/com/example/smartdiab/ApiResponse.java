package com.example.smartdiab;

import java.util.List;

public class ApiResponse {

    public List<Result> results;

    public static class Result {
        public String title;
    }
}