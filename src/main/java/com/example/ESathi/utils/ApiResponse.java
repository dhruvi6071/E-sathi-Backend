package com.example.ESathi.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> extends ResponseEntity<ApiResponse.ApiResponseBody<T>> {

    private ApiResponse(ApiResponseBody<T> body, HttpStatus status)
    {
        super(body,status);
    }


    //Inner class for response Body

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiResponseBody<T>
    {
        private LocalDateTime timestamp;
        private String status;
        private String message;
        private String code;
        private T data;
    }

    //factory Method for success
    public static <T> ApiResponse<T> success(T data, HttpStatus status)
    {
        ApiResponseBody<T> body = ApiResponseBody.<T>builder()
                .timestamp(LocalDateTime.now())
                .status("SUCCESS")
                .data(data)
                .build();
        return new ApiResponse<>(body , status);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, HttpStatus.OK);
    }

    // Factory methods for error responses
    public static <T> ApiResponse<T> error(String message, String code, HttpStatus status) {
        ApiResponseBody<T> body = ApiResponseBody.<T>builder()
                .timestamp(LocalDateTime.now())
                .status("ERROR")
                .message(message)
                .code(code)
                .build();
        return new ApiResponse<>(body, status);
    }

    public static <T> ApiResponse<T> error(String message, HttpStatus status) {
        ApiResponseBody<T> body = ApiResponseBody.<T>builder()
                .timestamp(LocalDateTime.now())
                .status("ERROR")
                .message(message)
                .build();
        return new ApiResponse<>(body, status);
    }

    public static <T> ApiResponse<T> error(String message, String code) {
        return error(message, code, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
