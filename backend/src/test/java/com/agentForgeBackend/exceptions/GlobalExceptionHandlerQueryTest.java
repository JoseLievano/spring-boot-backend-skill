package com.agentForgeBackend.exceptions;

import com.agentForgeBackend.shared.query.PageableRequest;
import com.agentForgeBackend.shared.tools.ErrorHTTPRes;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerQueryTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void invalidQueryRequestMapsToBadRequestShape() {
        ResponseEntity<ErrorHTTPRes> response = handler.handleInvalidQueryRequestException(
                new InvalidQueryRequestException("Unknown filter field 'secret'"),
                webRequest("/admin/list"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Invalid Query Request", response.getBody().getError());
        assertEquals("Unknown filter field 'secret'", response.getBody().getMessage());
        assertEquals("/admin/list", response.getBody().getPath());
    }

    @Test
    void methodArgumentNotValidMapsFieldViolationsToBadRequestShape() throws Exception {
        MethodArgumentNotValidException exception = methodArgumentNotValidException();

        ResponseEntity<ErrorHTTPRes> response = handler.handleMethodArgumentNotValidException(
                exception,
                webRequest("/client/list"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation Failed", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("size: must be less than or equal to 100"));
        assertEquals("/client/list", response.getBody().getPath());
    }

    @Test
    void malformedRequestBodyMapsToBadRequestShape() {
        ResponseEntity<ErrorHTTPRes> response = handler.handleHttpMessageNotReadableException(
                new HttpMessageNotReadableException("Invalid enum value", new MockHttpInputMessage(new byte[0])),
                webRequest("/admin/list"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Malformed Request", response.getBody().getError());
        assertEquals("Malformed request body.", response.getBody().getMessage());
        assertEquals("/admin/list", response.getBody().getPath());
    }

    private static ServletWebRequest webRequest(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return new ServletWebRequest(request);
    }

    private static MethodArgumentNotValidException methodArgumentNotValidException() throws Exception {
        PageableRequest target = new PageableRequest();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "pageableRequest");
        bindingResult.addError(new FieldError(
                "pageableRequest",
                "size",
                101,
                false,
                null,
                null,
                "must be less than or equal to 100"));

        Method method = SampleController.class.getDeclaredMethod("list", PageableRequest.class);
        return new MethodArgumentNotValidException(new MethodParameter(method, 0), bindingResult);
    }

    private static final class SampleController {
        @SuppressWarnings("unused")
        void list(PageableRequest request) {
        }
    }
}
