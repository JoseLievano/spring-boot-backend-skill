package com.authServer.exceptions;

import com.authServer.shared.tools.ErrorHTTPRes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidInsertDetails.class)
    public ResponseEntity<ErrorHTTPRes> handleInvalidInsertDetails(
            InvalidInsertDetails ex, WebRequest request) {

        ErrorHTTPRes errorMsgHTTP = ErrorHTTPRes.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Details")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorMsgHTTP, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ItemAlreadyExist.class)
    public ResponseEntity<Object> handleItemAlreadyExist(
            ItemAlreadyExist ex, WebRequest request) {

        ErrorHTTPRes errorRes = ErrorHTTPRes.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.CONFLICT.value())
                .error("Conflict")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorRes, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<Object> handleItemNotFoundException(
            ItemNotFoundException ex, WebRequest request) {

        ErrorHTTPRes errorRes = ErrorHTTPRes.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorRes, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidDeleteOperation.class)
    public ResponseEntity<Object> handleInvalidDeleteOperation(
            InvalidDeleteOperation ex, WebRequest request) {

        ErrorHTTPRes errorRes = ErrorHTTPRes.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorRes, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {

        ErrorHTTPRes errorRes = ErrorHTTPRes.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Unauthorized")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(errorRes, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorHTTPRes> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {

        ErrorHTTPRes errorRes = ErrorHTTPRes.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorRes, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorHTTPRes> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        ErrorHTTPRes errorRes = ErrorHTTPRes.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();

        return new ResponseEntity<>(errorRes, HttpStatus.BAD_REQUEST);
    }
}