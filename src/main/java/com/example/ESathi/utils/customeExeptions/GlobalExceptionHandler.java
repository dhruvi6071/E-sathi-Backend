package com.example.ESathi.utils.customeExeptions;

import com.example.ESathi.utils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //General ExceptionHandler
    @ExceptionHandler(BaseException.class)
    public ApiResponse<Void> BaseExceptionHandler(BaseException e)
    {
        return  ApiResponse.error(
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // Handle Exception when No Data found in Database
    @ExceptionHandler(ResourceNotFoundException.class)
    public ApiResponse<Void> handleResourceNotFoundException(ResourceNotFoundException e)
    {
        return ApiResponse.error(
                e.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    // Name not match with Database Stored name
    @ExceptionHandler(NameNotMatchException.class)
    public ApiResponse<Void> nameNotMatchExceptionHandler(NameNotMatchException e)
    {
        return  ApiResponse.error(
                e.getMessage(),
                HttpStatus.NOT_FOUND
        );
    }

    // Handle wrong username/password
    @ExceptionHandler(BadCredentialsException.class)
    public ApiResponse<Void> handleBadCredentials(BadCredentialsException e) {
        return ApiResponse.error(
                "Invalid email or password. Please try again.",
                HttpStatus.UNAUTHORIZED
        );
    }

    // Handle access denied (forbidden resources)
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDenied(AccessDeniedException e) {
        return ApiResponse.error(
                "You don’t have permission to perform this action.",
                HttpStatus.FORBIDDEN
        );
    }


}


