package com.example.posts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String content;
    private String userId;
    private String username;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer commentCount;
}
