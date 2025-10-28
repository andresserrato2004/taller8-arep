package com.example.stream.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private List<PostResponse> posts;
}
