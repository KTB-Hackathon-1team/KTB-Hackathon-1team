package com.ktb.hackathon.dto.response;

public record CommonResponse<T>(
    String message,
    T data
) {
    public static <T> CommonResponse<T> of(String message, T data) {
        return new CommonResponse<>(message, data);
    }
}
