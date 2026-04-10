package com.example.board.dto;

public record ApiResponse<T>(
        int code,
        String status,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, "SUCCESS", message, data);
    }
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, "SUCCESS", message, data);
    }
    public static <T> ApiResponse<T> error(int code, String status, String message) {
        return new ApiResponse<>(code, status, message,null);
    }
}
