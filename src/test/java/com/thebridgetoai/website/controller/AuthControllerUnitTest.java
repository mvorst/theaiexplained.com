package com.thebridgetoai.website.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import com.mattvorst.shared.security.model.user.UserLoginRequest;
import com.thebridgetoai.website.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private UserLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new UserLoginRequest();
        loginRequest.setUsername("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void userLogin_ReturnsToken_WhenCredentialsAreValid() {
        String expectedToken = "jwt.token.here";
        when(authService.authenticateWithEmailAndPassword("test@example.com", "password123"))
                .thenReturn(expectedToken);

        ResponseEntity<?> result = authController.userLogin(loginRequest);

        assertEquals(200, result.getStatusCode().value());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals(expectedToken, body.get("token"));
        verify(authService).authenticateWithEmailAndPassword("test@example.com", "password123");
    }

    @Test
    void userLogin_ReturnsUnauthorized_WhenCredentialsAreInvalid() {
        when(authService.authenticateWithEmailAndPassword("test@example.com", "password123"))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        ResponseEntity<?> result = authController.userLogin(loginRequest);

        assertEquals(401, result.getStatusCode().value());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Invalid credentials", body.get("error"));
        verify(authService).authenticateWithEmailAndPassword("test@example.com", "password123");
    }

    @Test
    void userLogin_HandlesNullPassword() {
        loginRequest.setPassword(null);

        when(authService.authenticateWithEmailAndPassword("test@example.com", null))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        ResponseEntity<?> result = authController.userLogin(loginRequest);

        assertEquals(401, result.getStatusCode().value());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Invalid credentials", body.get("error"));
        verify(authService).authenticateWithEmailAndPassword("test@example.com", null);
    }

    @Test
    void userLogin_HandlesEmptyUsername() {
        loginRequest.setUsername("");

        when(authService.authenticateWithEmailAndPassword("", "password123"))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        ResponseEntity<?> result = authController.userLogin(loginRequest);

        assertEquals(401, result.getStatusCode().value());
        Map<String, String> body = (Map<String, String>) result.getBody();
        assertEquals("Invalid credentials", body.get("error"));
        verify(authService).authenticateWithEmailAndPassword("", "password123");
    }
}