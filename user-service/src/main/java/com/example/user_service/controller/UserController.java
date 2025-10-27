package com.example.user_service.controller;

import com.example.user_service.dto.UserResponse;
import com.example.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            UserResponse user = userService.getUserByUsername(username);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String username = jwt.getClaimAsString("cognito:username");
            
            if (username == null) {
                username = jwt.getClaimAsString("preferred_username");
            }
            
            UserResponse user = userService.getUserByUsername(username);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Unable to get user info: " + e.getMessage()
            ));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> updates,
            Authentication authentication) {
        try {
            // Verificar que el usuario está actualizando su propio perfil
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String username = jwt.getClaimAsString("cognito:username");
            if (username == null) {
                username = jwt.getClaimAsString("preferred_username");
            }
            
            UserResponse currentUser = userService.getUserByUsername(username);
            if (!currentUser.getId().equals(id)) {
                return ResponseEntity.status(403).body(Map.of(
                    "error", "You can only update your own profile"
                ));
            }
            
            UserResponse updatedUser = userService.updateUser(
                id,
                updates.get("bio"),
                updates.get("profilePicture")
            );
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
}
