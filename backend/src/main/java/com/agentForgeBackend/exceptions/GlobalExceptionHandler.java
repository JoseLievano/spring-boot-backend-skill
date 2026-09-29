package com.agentForgeBackend.exceptions;

import com.agentForgeBackend.shared.tools.ErrorHTTPRes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidQueryRequestException.class)
    public ResponseEntity<ErrorHTTPRes> handleInvalidQueryRequestException(
            InvalidQueryRequestException ex, WebRequest request) {

        ErrorHTTPRes errorRes = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Query Request",
                ex.getMessage(),
                request);

        return new ResponseEntity<>(errorRes, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorHTTPRes> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorHTTPRes errorRes = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                message.isBlank() ? ex.getMessage() : message,
                request);

        return new ResponseEntity<>(errorRes, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorHTTPRes> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {

        ErrorHTTPRes errorRes = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed Request",
                "Malformed request body.",
                request);

        return new ResponseEntity<>(errorRes, HttpStatus.BAD_REQUEST);
    }

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

    private ErrorHTTPRes buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            WebRequest request) {

        return ErrorHTTPRes.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }
}
