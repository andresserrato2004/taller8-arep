package com.example.user_service.controller;

import com.example.user_service.dto.*;
import com.example.user_service.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationRequest request) {
        try {
            UserResponse user = userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "User registered successfully. Please check your email to confirm.",
                "user", user
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    // Paso 1: Verificar si el usuario existe
    @PostMapping("/login/check")
    public ResponseEntity<?> checkUser(@RequestBody UserIdentifierRequest request) {
        try {
            boolean exists = userService.checkUserExists(request.getIdentifier());
            if (exists) {
                return ResponseEntity.ok(Map.of(
                    "exists", true,
                    "message", "User found. Please enter your password.",
                    "identifier", request.getIdentifier()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "exists", false,
                    "error", "User not found"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    // Paso 2: Autenticar con contraseña
    @PostMapping("/login/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody PasswordRequest request) {
        try {
            AuthenticationResponse response = userService.loginWithPassword(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    // Login tradicional (mantener para compatibilidad)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {
        try {
            AuthenticationResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/verify")
    public ResponseEntity<?> confirmUser(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String confirmationCode = request.get("confirmationCode");
            userService.confirmUser(username, confirmationCode);
            return ResponseEntity.ok(Map.of(
                "message", "User confirmed successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            String username = request.get("username");
            AuthenticationResponse response = userService.refreshToken(refreshToken, username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
}
