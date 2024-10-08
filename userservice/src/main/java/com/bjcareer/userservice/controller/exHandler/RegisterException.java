package com.bjcareer.userservice.controller.exHandler;

import com.bjcareer.userservice.controller.RegisterController;
import com.bjcareer.userservice.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(assignableTypes = {RegisterController.class})
public class RegisterException {
    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleDatabaseOperationException(DatabaseOperationException e) {
        return buildErrorResponse("서버와 접속이 불안정합니다. 잠시 후 다시 시도해주세요", HttpStatus.SERVICE_UNAVAILABLE, e);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleRedisLockAcquisitionException(RedisLockAcquisitionException e) {
        return buildErrorResponse("서버의 사용자가 많습니다, 잠시 후 다시 시도해주세요", HttpStatus.TOO_MANY_REQUESTS, e);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleTelegramCommunicationException(TelegramCommunicationException e) {
        return buildErrorResponse("텔레그렘 메세지 전송에 실패했습니다. 다시 시도해주세요", HttpStatus.SERVICE_UNAVAILABLE, e);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return buildErrorResponse("이미 존재하는 사용자입니다", HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException e) {
        return buildErrorResponse("찾고자 하는 사용자가 없습니다", HttpStatus.NOT_FOUND, e);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status, Exception e) {
        log.error("Error: {}", e.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        return new ResponseEntity<>(response, status);
    }
}
