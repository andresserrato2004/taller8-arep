package com.example.posts.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 140)
    private String content;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private Integer likeCount;
    
    @Column
    private Integer commentCount;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (likeCount == null) {
            likeCount = 0;
        }
        if (commentCount == null) {
            commentCount = 0;
        }
    }
}
