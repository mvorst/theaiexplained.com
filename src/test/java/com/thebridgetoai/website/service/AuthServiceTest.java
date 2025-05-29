package com.thebridgetoai.website.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import com.mattvorst.shared.async.processor.TaskProcessor;
import com.mattvorst.shared.dao.SecurityDao;
import com.mattvorst.shared.dao.model.security.User;
import com.mattvorst.shared.security.constant.Source;
import com.mattvorst.shared.security.service.JwtService;
import com.thebridgetoai.website.dao.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TaskProcessor taskProcessor;

    @Mock
    private SecurityDao securityDao;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private String testEmail;
    private String testPassword;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testPassword = "password123";
        encodedPassword = "$2a$10$encoded.password.hash";

        testUser = new User();
        testUser.setUserUuid(UUID.randomUUID());
        testUser.setEncodedPassword(encodedPassword);
    }

    @Test
    void authenticateWithEmailAndPassword_ReturnsToken_WhenCredentialsAreValid() {
        String expectedToken = "jwt.token.here";
        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn(expectedToken);

        when(userDetailsService.loadUserByUsername(Source.EMAIL + "|" + testEmail.toUpperCase()))
                .thenReturn(testUser);
        when(passwordEncoder.matches(testPassword, encodedPassword)).thenReturn(true);
        when(jwtService.generateUserJwt(testUser)).thenReturn(mockJwt);

        String result = authService.authenticateWithEmailAndPassword(testEmail, testPassword);

        assertEquals(expectedToken, result);
        verify(userDetailsService).loadUserByUsername(Source.EMAIL + "|" + testEmail.toUpperCase());
        verify(passwordEncoder).matches(testPassword, encodedPassword);
        verify(jwtService).generateUserJwt(testUser);
    }

    @Test
    void authenticateWithEmailAndPassword_ThrowsBadCredentialsException_WhenUserNotFound() {
        when(userDetailsService.loadUserByUsername(Source.EMAIL + "|" + testEmail.toUpperCase()))
                .thenReturn(null);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.authenticateWithEmailAndPassword(testEmail, testPassword);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userDetailsService).loadUserByUsername(Source.EMAIL + "|" + testEmail.toUpperCase());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateUserJwt(any(User.class));
    }

    @Test
    void authenticateWithEmailAndPassword_ThrowsBadCredentialsException_WhenPasswordIsIncorrect() {
        when(userDetailsService.loadUserByUsername(Source.EMAIL + "|" + testEmail.toUpperCase()))
                .thenReturn(testUser);
        when(passwordEncoder.matches(testPassword, encodedPassword)).thenReturn(false);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.authenticateWithEmailAndPassword(testEmail, testPassword);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userDetailsService).loadUserByUsername(Source.EMAIL + "|" + testEmail.toUpperCase());
        verify(passwordEncoder).matches(testPassword, encodedPassword);
        verify(jwtService, never()).generateUserJwt(any(User.class));
    }

    @Test
    void authenticateWithEmailAndPassword_UppercasesEmail() {
        String expectedToken = "jwt.token.here";
        String lowercaseEmail = "test@example.com";
        String expectedUsername = Source.EMAIL + "|" + lowercaseEmail.toUpperCase();
        
        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn(expectedToken);

        when(userDetailsService.loadUserByUsername(expectedUsername))
                .thenReturn(testUser);
        when(passwordEncoder.matches(testPassword, encodedPassword)).thenReturn(true);
        when(jwtService.generateUserJwt(testUser)).thenReturn(mockJwt);

        String result = authService.authenticateWithEmailAndPassword(lowercaseEmail, testPassword);

        assertEquals(expectedToken, result);
        verify(userDetailsService).loadUserByUsername(expectedUsername);
    }

    @Test
    void authenticateWithEmailAndPassword_ThrowsBadCredentialsException_WhenUserDetailsServiceReturnsNonUserObject() {
        when(userDetailsService.loadUserByUsername(Source.EMAIL + "|" + testEmail.toUpperCase()))
                .thenReturn(null);

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.authenticateWithEmailAndPassword(testEmail, testPassword);
        });

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userDetailsService).loadUserByUsername(Source.EMAIL + "|" + testEmail.toUpperCase());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateUserJwt(any(User.class));
    }
}