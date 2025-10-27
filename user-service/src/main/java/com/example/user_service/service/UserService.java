package com.example.user_service.service;

import com.example.user_service.dto.AuthenticationResponse;
import com.example.user_service.dto.UserLoginRequest;
import com.example.user_service.dto.UserRegistrationRequest;
import com.example.user_service.dto.UserResponse;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final CognitoService cognitoService;
    
    public UserService(UserRepository userRepository, CognitoService cognitoService) {
        this.userRepository = userRepository;
        this.cognitoService = cognitoService;
    }
    
    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        // Validar que el usuario no exista
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Registrar en Cognito
        String cognitoUserId = cognitoService.registerUser(request);
        
        // Crear usuario en la base de datos
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setCognitoUserId(cognitoUserId);
        
        User savedUser = userRepository.save(user);
        return UserResponse.fromUser(savedUser);
    }
    
    public AuthenticationResponse login(UserLoginRequest request) {
        // Verificar que el usuario existe en nuestra BD
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Autenticar con Cognito
        return cognitoService.authenticateUser(request);
    }
    
    @Transactional
    public void confirmUser(String username, String confirmationCode) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        cognitoService.confirmUser(username, confirmationCode);
    }
    
    public AuthenticationResponse refreshToken(String refreshToken, String username) {
        // Verificar que el usuario existe
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return cognitoService.refreshToken(refreshToken, username);
    }
    
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.fromUser(user);
    }
    
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return UserResponse.fromUser(user);
    }
    
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(UserResponse::fromUser)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public UserResponse updateUser(Long id, String bio, String profilePicture) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (bio != null) {
            user.setBio(bio);
        }
        if (profilePicture != null) {
            user.setProfilePicture(profilePicture);
        }
        
        User updatedUser = userRepository.save(user);
        return UserResponse.fromUser(updatedUser);
    }
}
