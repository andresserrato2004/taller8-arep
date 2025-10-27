package com.example.user_service.dto;

import com.example.user_service.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String bio;
    private String profilePicture;
    private LocalDateTime createdAt;
    
    public static UserResponse fromUser(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getBio(),
            user.getProfilePicture(),
            user.getCreatedAt()
        );
    }
}
