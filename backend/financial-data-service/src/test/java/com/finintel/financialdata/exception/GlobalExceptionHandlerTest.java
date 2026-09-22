package com.finintel.financialdata.exception;

import com.finintel.financialdata.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFound_shouldReturn404ErrorResponse() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException(
                        "Company not found: AAPL"
                );

        ErrorResponse response =
                handler.handleResourceNotFound(exception);

        assertNotNull(response);
        assertNotNull(response.timestamp());

        assertEquals(
                404,
                response.status()
        );

        assertEquals(
                "Not Found",
                response.error()
        );

        assertEquals(
                "Company not found: AAPL",
                response.message()
        );
    }

    @Test
    void handleDuplicateResource_shouldReturn409ErrorResponse() {

        DuplicateResourceException exception =
                new DuplicateResourceException(
                        "Company already exists: AAPL"
                );

        ErrorResponse response =
                handler.handleDuplicateResource(exception);

        assertNotNull(response);
        assertNotNull(response.timestamp());

        assertEquals(
                409,
                response.status()
        );

        assertEquals(
                "Conflict",
                response.error()
        );

        assertEquals(
                "Company already exists: AAPL",
                response.message()
        );
    }

    @Test
    void handleValidationException_shouldReturn400WithFirstFieldError() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        FieldError fieldError =
                new FieldError(
                        "createCompanyRequest",
                        "ticker",
                        "",
                        false,
                        null,
                        null,
                        "must not be blank"
                );

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(fieldError));

        ErrorResponse response =
                handler.handleValidationException(exception);

        assertNotNull(response);
        assertNotNull(response.timestamp());

        assertEquals(
                400,
                response.status()
        );

        assertEquals(
                "Bad Request",
                response.error()
        );

        assertEquals(
                "ticker: must not be blank",
                response.message()
        );
    }

    @Test
    void handleValidationException_whenNoFieldErrors_shouldUseFallbackMessage() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult =
                mock(BindingResult.class);

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of());

        ErrorResponse response =
                handler.handleValidationException(exception);

        assertNotNull(response);
        assertNotNull(response.timestamp());

        assertEquals(
                400,
                response.status()
        );

        assertEquals(
                "Bad Request",
                response.error()
        );

        assertEquals(
                "Validation failed",
                response.message()
        );
    }
}